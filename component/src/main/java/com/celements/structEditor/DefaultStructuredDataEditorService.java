package com.celements.structEditor;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.structEditor.classes.StructuredDataEditorClass.*;
import static com.google.common.base.Predicates.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.CustomClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.struct.SelectTagServiceRole;
import com.celements.struct.StructUtilServiceRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.OptionTagEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.velocity.VelocityService;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

@Component
public class DefaultStructuredDataEditorService implements StructuredDataEditorService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(DefaultStructuredDataEditorService.class);

  @Requirement
  private Execution exec;

  @Requirement
  private IPageTypeResolverRole ptResolver;

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  @Requirement
  private List<SelectAutocompleteRole> selectAutocompleteRole;

  @Requirement
  private SelectTagServiceRole selectTagService;

  @Requirement
  private StructUtilServiceRole structUtils;

  @Requirement
  protected VelocityService velocityService;

  @Override
  public Optional<String> getAttributeName(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return getAttributeNameInternal(cellDoc, onDoc);
  }

  private Optional<String> getAttributeNameInternal(XWikiDocument cellDoc, XWikiDocument onDoc) {
    List<String> nameParts = new ArrayList<>();
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    if (fieldName.isPresent()) {
      if (classRef.isPresent()) {
        nameParts.add(modelUtils.serializeRef(classRef.get()));
        if (onDoc != null) {
          int objNb = getStructXObjectNumber(cellDoc).orElse(-1);
          if ((objNb >= 0) && !getXObject(onDoc, classRef.get(), objNb).isPresent()) {
            objNb = -1;
          }
          nameParts.add(Integer.toString(objNb));
        }
      }
      nameParts.add(fieldName.get());
    }
    String name = Joiner.on('_').join(nameParts);
    LOGGER.info("getAttributeName: '{}' for cell '{}', onDoc '{}'", name, cellDoc, onDoc);
    return Optional.ofNullable(Strings.emptyToNull(name));
  }

  @Override
  public Optional<String> getPrettyName(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    String prettyName = "";
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    String dictKey = Joiner.on('_').skipNulls().join(
        resolveFormPrefix(cellDoc).orElse(null),
        getAttributeNameInternal(cellDoc, null).orElse(null),
        getOptionTagValue(cellDoc).orElse(null));
    LOGGER.debug("getPrettyName: dictKey '{}' for cell '{}'", dictKey, cellDoc);
    prettyName = webUtils.getAdminMessageTool().get(dictKey);
    if (dictKey.equals(prettyName)) {
      Optional<String> xClassPrettyName = getXClassPrettyName(cellDoc);
      if (xClassPrettyName.isPresent()) {
        prettyName = xClassPrettyName.get();
      }
    }
    LOGGER.info("getPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    return Optional.ofNullable(prettyName);
  }

  private Optional<String> getOptionTagValue(XWikiDocument cellDoc) {
    return XWikiObjectFetcher.on(cellDoc).fetchField(OptionTagEditorClass.FIELD_VALUE)
        .stream().findFirst();
  }

  @Override
  public Optional<String> getDateFormatFromField(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    Optional<PropertyClass> field = getCellPropertyClass(cellDoc);
    if (field.isPresent() && (field.get() instanceof DateClass)) {
      DateClass dateField = (DateClass) field.get();
      return Optional.ofNullable(dateField.getDateFormat());
    }
    return Optional.empty();
  }

  Optional<String> resolveFormPrefix(XWikiDocument cellDoc) {
    Optional<String> prefix = Optional.empty();
    try {
      Optional<XWikiDocument> formDoc = structUtils.findParentCell(cellDoc,
          FormFieldPageType.PAGETYPE_NAME);
      if (formDoc.isPresent()) {
        prefix = modelAccess.getFieldValue(formDoc.get(), FormFieldEditorClass.FIELD_PREFIX)
            .toJavaUtil();
      }
      LOGGER.debug("resolveFormPrefix: '{}' for cell '{}'", prefix, cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent on doc '{}' doesn't exist", cellDoc, exc);
    }
    return prefix;
  }

  Optional<String> getXClassPrettyName(XWikiDocument cellDoc) {
    String prettyName = null;
    Optional<PropertyClass> property = getCellPropertyClass(cellDoc);
    if (property.isPresent()) {
      prettyName = Strings.emptyToNull(property.get().getPrettyName());
    }
    LOGGER.debug("getXClassPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    return Optional.ofNullable(prettyName);
  }

  @Override
  public Optional<ClassReference> getCellClassRef(XWikiDocument cellDoc) {
    return modelAccess.getFieldValue(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS)
        .toJavaUtil()
        .map(ClassReference::new);
  }

  @Override
  public Optional<String> getCellFieldName(XWikiDocument cellDoc) {
    return modelAccess.getFieldValue(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)
        .toJavaUtil();
  }

  @Override
  public Optional<PropertyClass> getCellPropertyClass(XWikiDocument cellDoc) {
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    if (classRef.isPresent() && fieldName.isPresent()) {
      try {
        XWikiDocument xClassDoc = modelAccess.getDocument(classRef.get().getDocRef(
            cellDoc.getDocumentReference().getWikiReference()));
        return Optional.ofNullable((PropertyClass) xClassDoc.getXClass().get(fieldName.get()));
      } catch (DocumentNotExistsException exc) {
        LOGGER.warn("configured class '{}' on cell '{}' doesn't exist", classRef, cellDoc, exc);
      }
    } else {
      LOGGER.debug("class and field not configured for cell '{}'", cellDoc);
    }
    return Optional.empty();
  }

  private Optional<ClassField<?>> getCellClassField(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    try {
      ClassDefinition classDef = Utils.getComponentManager().lookup(ClassDefinition.class,
          getCellClassRef(cellDoc).map(ClassReference::serialize).orElse(""));
      return classDef.getField(getCellFieldName(cellDoc).orElse("")).toJavaUtil();
    } catch (ComponentLookupException exc) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> getCellValueAsString(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    return Optional.ofNullable(getCellValue(cellDocRef, onDoc))
        .map(rethrowFunction(value -> (value instanceof String) ? value
            : trySerializeForCustomClassField(cellDocRef, value)))
        .map(Objects::toString);
  }

  @SuppressWarnings("unchecked")
  private Object trySerializeForCustomClassField(DocumentReference cellDocRef, Object value)
      throws DocumentNotExistsException {
    ClassField<?> field = getCellClassField(cellDocRef).orElse(null);
    if (field instanceof CustomClassField) {
      try {
        return ((CustomClassField<Object>) field).serialize(value);
      } catch (ClassCastException cce) {
        LOGGER.warn("trySerializeForCustomClassField: unable to cast [{}] for [{}] on [{}]",
            value, field, cellDocRef);
      }
    }
    return value;
  }

  @Override
  public Optional<Date> getCellDateValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    Object value = getCellValue(cellDocRef, onDoc);
    if (value instanceof Date) {
      return Optional.of((Date) value);
    }
    return Optional.empty();
  }

  @Override
  public List<String> getCellListValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    List<String> ret = new ArrayList<>();
    Object value = getCellValue(cellDocRef, onDoc);
    if (value instanceof List) {
      for (Object elem : (List<?>) value) {
        ret.add(elem != null ? elem.toString() : "");
      }
    }
    return ret;
  }

  private Object getCellValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    Object value = null;
    if (fieldName.isPresent()) {
      Optional<BaseObject> obj = getXObjectInStructEditor(cellDoc, onDoc);
      if (obj.isPresent()) {
        value = modelAccess.getProperty(obj.get(), fieldName.get());
      } else if (fieldName.get().equals("title")) {
        value = getTranslatedValue(onDoc, XWikiDocument::getTitle);
      } else if (fieldName.get().equals("content")) {
        value = getTranslatedValue(onDoc, XWikiDocument::getContent);
      }
    }
    return value;
  }

  private String getTranslatedValue(XWikiDocument onDoc,
      Function<XWikiDocument, String> valueGetter) {
    try {
      onDoc = onDoc.getTranslatedDocument(context.getXWikiContext());
    } catch (XWikiException exc) {
      // is actually never thrown in #getTranslatedDocument
      LOGGER.error("getTranslatedValue - [{}]", onDoc, exc);
    }
    return Strings.emptyToNull(valueGetter.apply(onDoc).trim());
  }

  @Override
  @Deprecated
  public Optional<DocumentReference> getSelectCellDocRef(DocumentReference cellDocRef) {
    return selectTagService.getSelectCellDocRef(cellDocRef);
  }

  @Override
  public Optional<BaseObject> getXObjectInStructEditor(XWikiDocument cellDoc, XWikiDocument onDoc) {
    Optional<BaseObject> ret = Optional.empty();
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    if (classRef.isPresent() && (onDoc != null)) {
      ret = getXObject(onDoc, classRef.get(), getStructXObjectNumber(cellDoc).orElse(0));
    }
    LOGGER.info("getXObjectInStructEditor - for cellDoc '{}', onDoc '{}', class '{}', objNb '{}': "
        + "{}", cellDoc, onDoc, classRef.orElse(null), ret.map(BaseObject::getNumber).orElse(null),
        ret.orElse(null));
    return ret;
  }

  private Optional<BaseObject> getXObject(XWikiDocument onDoc, ClassReference classRef, int objNb) {
    return XWikiObjectFetcher.on(onDoc).filter(classRef).filter(objNb).stream().findFirst();
  }

  private Optional<Integer> getStructXObjectNumber(XWikiDocument cellDoc) {
    return Stream.<Supplier<Optional<Integer>>>of(
        () -> getNumberFromRequest(),
        () -> getNumberFromExecutionContext(),
        () -> getNumberFromComputedField(cellDoc))
        .map(Supplier::get).filter(Optional::isPresent).map(Optional::get)
        .findFirst();
  }

  private Optional<Integer> getNumberFromRequest() {
    return Optional.ofNullable(Ints.tryParse(context.getRequestParameter("objNb").or("")));
  }

  private Optional<Integer> getNumberFromExecutionContext() {
    return Stream.of("objNb", "celements.globalvalues.cell.number")
        .map(exec.getContext()::getProperty)
        .map(Objects::toString)
        .map(Ints::tryParse)
        .filter(Objects::nonNull)
        .findFirst();
  }

  private Optional<Integer> getNumberFromComputedField(XWikiDocument cellDoc) {
    try {
      return modelAccess.getFieldValue(cellDoc, FIELD_COMPUTED_OBJ_NB).toJavaUtil()
          .map(String::trim).filter(not(String::isEmpty))
          .map(rethrowFunction(text -> velocityService.evaluateVelocityText(text)))
          .map(Ints::tryParse);
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("computeObjNb - failed for [{}]", cellDoc, exc);
      return Optional.empty();
    }
  }

  @Override
  public List<String> getSelectTagAutocompleteJsPathList() {
    List<String> roles = new ArrayList<>();
    for (SelectAutocompleteRole role : selectAutocompleteRole) {
      roles.add(role.getJsFilePath());
    }
    return roles;
  }

  @Override
  public boolean hasEditField(XWikiDocument cellDoc) {
    return getCellFieldName(cellDoc).isPresent();
  }

}
