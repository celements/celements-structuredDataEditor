/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.structEditor;

import static com.celements.common.MoreObjectsCel.*;
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
import org.xwiki.context.Execution;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.ClassIdentity;
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
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

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
  private SelectTagServiceRole selectTagService;

  @Requirement
  private StructUtilServiceRole structUtils;

  @Requirement
  protected VelocityService velocityService;

  @Override
  public Optional<String> getAttributeName(XWikiDocument cellDoc, XWikiDocument onDoc) {
    List<String> nameParts = new ArrayList<>();
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    if (fieldName.isPresent()) {
      if (classRef.isPresent()) {
        nameParts.add(modelUtils.serializeRef(classRef.get()));
        if (onDoc != null) {
          int objNb = getStructXObjectNumber(cellDoc, onDoc)
              .filter(nb -> isXObjectNumberNewOrExists(cellDoc, onDoc, nb))
              .orElseGet(() -> getFallbackXObjectNumber(cellDoc, onDoc));
          nameParts.add(Integer.toString(objNb));
        }
      }
      nameParts.add(fieldName.get());
    }
    String name = Joiner.on('_').join(nameParts);
    LOGGER.info("getAttributeName: '{}' for cell '{}', onDoc '{}'", name, cellDoc, onDoc);
    return asOptional(name);
  }

  private boolean isXObjectNumberNewOrExists(XWikiDocument cellDoc, XWikiDocument onDoc,
      Integer nb) {
    return (nb < 0) || newXObjFetcher(cellDoc, onDoc).filter(nb).exists();
  }

  private int getFallbackXObjectNumber(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.of(cellDoc)
        .filter(not(this::isMultilingual))
        .flatMap(doc -> newXObjFetcher(doc, onDoc).stream()
            .findFirst())
        .map(BaseObject::getNumber)
        .orElse(-1);
  }

  @Override
  public Optional<String> getPrettyName(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    String prettyName = "";
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    String dictKey = Joiner.on('_').skipNulls().join(
        resolveFormPrefix(cellDoc).orElse(null),
        getAttributeName(cellDoc, null).orElse(null),
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
  public Optional<BaseClass> getCellXClass(XWikiDocument cellDoc) {
    return getCellClassRef(cellDoc)
        .map(classRef -> classRef.getDocRef(cellDoc.getDocumentReference().getWikiReference()))
        .map(modelAccess::getOrCreateDocument)
        .filter(not(XWikiDocument::isNew))
        .map(XWikiDocument::getXClass);
  }

  @Override
  public Optional<PropertyClass> getCellPropertyClass(XWikiDocument cellDoc) {
    return getCellXClass(cellDoc)
        .flatMap(xClass -> getCellFieldName(cellDoc)
            .map(xClass::get))
        .flatMap(prop -> tryCast(prop, PropertyClass.class));
  }

  private Optional<ClassField<?>> getCellClassField(XWikiDocument cellDoc) {
    return getCellClassRef(cellDoc)
        .flatMap(ClassIdentity::getClassDefinition)
        .flatMap(classDef -> classDef.getField(getCellFieldName(cellDoc).orElse("")));
  }

  @Override
  public Optional<String> getCellValueAsString(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    return getCellValueAsString(modelAccess.getDocument(cellDocRef), onDoc);
  }

  @Override
  public Optional<String> getCellValueAsString(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.ofNullable(getCellValue(cellDoc, onDoc))
        .flatMap(value -> trySerializeForCustomClassField(cellDoc, value))
        .map(Objects::toString).filter(not(String::isEmpty));
  }

  @SuppressWarnings("unchecked")
  private Optional<?> trySerializeForCustomClassField(XWikiDocument cellDoc, Object value) {
    if (value instanceof String) {
      return Optional.of(value);
    }
    ClassField<?> field = getCellClassField(cellDoc).orElse(null);
    try {
      if (field instanceof CustomClassField) {
        return ((CustomClassField<Object>) field).serialize(value);
      } else {
        return Optional.ofNullable(value);
      }
    } catch (ClassCastException cce) {
      LOGGER.warn("trySerializeForCustomClassField: unable to cast [{}] for [{}] on [{}]",
          value, field, cellDoc.getDocumentReference());
      return Optional.empty();
    }
  }

  @Override
  public Optional<Date> getCellDateValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    Object value = getCellValue(modelAccess.getDocument(cellDocRef), onDoc);
    if (value instanceof Date) {
      return Optional.of((Date) value);
    }
    return Optional.empty();
  }

  @Override
  public List<String> getCellListValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    List<String> ret = new ArrayList<>();
    Object value = getCellValue(modelAccess.getDocument(cellDocRef), onDoc);
    if (value instanceof List) {
      for (Object elem : (List<?>) value) {
        ret.add(elem != null ? elem.toString() : "");
      }
    }
    return ret;
  }

  private Object getCellValue(XWikiDocument cellDoc, XWikiDocument onDoc) {
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
    String value = null;
    if (onDoc != null) {
      try {
        onDoc = onDoc.getTranslatedDocument(context.getXWikiContext());
      } catch (XWikiException exc) {
        // is actually never thrown in #getTranslatedDocument
        LOGGER.error("getTranslatedValue - [{}]", onDoc, exc);
      }
      value = Strings.emptyToNull(valueGetter.apply(onDoc).trim());
    }
    return value;
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
      XWikiObjectFetcher fetcher = newXObjFetcher(cellDoc, onDoc);
      getStructXObjectNumber(cellDoc, onDoc).ifPresent(fetcher::filter);
      ret = fetcher.stream().findFirst();
    }
    LOGGER.info("getXObjectInStructEditor - for cellDoc '{}', onDoc '{}', class '{}', objNb '{}': "
        + "{}", cellDoc, onDoc, classRef.orElse(null), ret.map(BaseObject::getNumber).orElse(null),
        ret.orElse(null));
    return ret;
  }

  private Optional<Integer> getStructXObjectNumber(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Stream.<Supplier<Optional<Integer>>>of(
        () -> getNumberFromRequest(),
        () -> getNumberFromExecutionContext(),
        () -> getNumberFromComputedField(cellDoc),
        () -> getNumberForMultilingual(cellDoc, onDoc))
        .map(Supplier::get).filter(Optional::isPresent).map(Optional::get)
        .peek(nb -> LOGGER.debug("getStructXObjectNumber: got [{}] for [{}]", nb, cellDoc))
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

  private Optional<Integer> getNumberForMultilingual(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.of(cellDoc)
        .filter(this::isMultilingual)
        .flatMap(doc -> newXObjFetcher(doc, onDoc)
            .filter(this::isOfRequestOrDefaultLang)
            .stream().findFirst())
        .map(BaseObject::getNumber);
  }

  private boolean isOfRequestOrDefaultLang(BaseObject xObj) {
    String xObjLang = getLangDependent(name -> Strings.emptyToNull(xObj.getStringValue(name)))
        .orElse("");
    return context.getLanguage()
        .orElseGet(() -> context.getDefaultLanguage(xObj.getDocumentReference()))
        .equals(xObjLang);
  }

  private <T> Optional<T> getLangDependent(Function<String, T> func) {
    return ClassDefinition.LANG_FIELD_NAMES.stream()
        .map(func)
        .filter(Objects::nonNull)
        .findFirst();
  }

  @Override
  public boolean hasEditField(XWikiDocument cellDoc) {
    return getCellFieldName(cellDoc).isPresent();
  }

  @Override
  public boolean isMultilingual(XWikiDocument cellDoc) {
    return modelAccess.getFieldValue(cellDoc, FIELD_MULTILINGUAL).toJavaUtil()
        .orElse(false);
  }

  @Override
  public Optional<String> getLangNameAttribute(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.of(cellDoc)
        .filter(this::isMultilingual)
        .flatMap(this::getCellXClass)
        .flatMap(xClass -> getLangDependent(xClass::get))
        .flatMap(prop -> getAttributeName(cellDoc, onDoc)
            .flatMap(attrName -> getCellFieldName(cellDoc)
                .map(fieldName -> attrName.replace(fieldName, prop.getName()))));
  }

  private XWikiObjectFetcher newXObjFetcher(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return Optional.ofNullable(onDoc)
        .map(XWikiObjectFetcher::on)
        .flatMap(fetcher -> getCellClassRef(cellDoc)
            .map(fetcher::filter))
        .orElseGet(XWikiObjectFetcher::empty);
  }

  private static Optional<String> asOptional(String str) {
    return Optional.ofNullable(Strings.emptyToNull(str.trim()));
  }

}
