package com.celements.structEditor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.structEditor.fields.SelectTagPageType;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

@Component
public class DefaultStructuredDataEditorService implements StructuredDataEditorService {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultStructuredDataEditorService.class);

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
          Optional<BaseObject> obj = getRequestXObjectInStructEditor(cellDoc, onDoc);
          nameParts.add(Integer.toString(obj.isPresent() ? obj.get().getNumber() : -1));
        }
      }
      nameParts.add(fieldName.get());
    }
    String name = Joiner.on('_').join(nameParts);
    LOGGER.info("getAttributeName: '{}' for cell '{}', onDoc '{}'", name, cellDoc, onDoc);
    return Optional.fromNullable(Strings.emptyToNull(name));
  }

  @Override
  public Optional<String> getPrettyName(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    String prettyName = "";
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    String dictKey = Joiner.on('_').skipNulls().join(resolveFormPrefix(cellDoc).orNull(),
        getAttributeNameInternal(cellDoc, null).orNull());
    LOGGER.debug("getPrettyName: dictKey '{}' for cell '{}'", dictKey, cellDoc);
    prettyName = webUtils.getAdminMessageTool().get(dictKey);
    if (dictKey.equals(prettyName)) {
      Optional<String> xClassPrettyName = getXClassPrettyName(cellDoc);
      if (xClassPrettyName.isPresent()) {
        prettyName = xClassPrettyName.get();
      }
    }
    LOGGER.info("getPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    return Optional.fromNullable(prettyName);
  }

  @Override
  public Optional<String> getDateFormatFromField(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    Optional<PropertyClass> field = getCellPropertyClass(cellDoc);
    if (field.isPresent() && (field.get() instanceof DateClass)) {
      DateClass dateField = (DateClass) field.get();
      return Optional.fromNullable(dateField.getDateFormat());
    }
    return Optional.absent();
  }

  Optional<String> resolveFormPrefix(XWikiDocument cellDoc) {
    Optional<String> prefix = Optional.absent();
    try {
      Optional<XWikiDocument> formDoc = findParentCell(cellDoc, FormFieldPageType.PAGETYPE_NAME);
      if (formDoc.isPresent()) {
        prefix = modelAccess.getFieldValue(formDoc.get(), FormFieldEditorClass.FIELD_PREFIX);
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
    return Optional.fromNullable(prettyName);
  }

  @Override
  public Optional<ClassReference> getCellClassRef(XWikiDocument cellDoc) {
    Optional<DocumentReference> classDocRef = modelAccess.getFieldValue(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS);
    if (classDocRef.isPresent()) {
      return Optional.of(new ClassReference(classDocRef.get()));
    } else {
      return Optional.absent();
    }
  }

  @Override
  public Optional<PropertyClass> getCellPropertyClass(XWikiDocument cellDoc) {
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    if (classRef.isPresent() && fieldName.isPresent()) {
      try {
        XWikiDocument xClassDoc = modelAccess.getDocument(classRef.get().getDocRef(
            cellDoc.getDocumentReference().getWikiReference()));
        return Optional.fromNullable((PropertyClass) xClassDoc.getXClass().get(fieldName.get()));
      } catch (DocumentNotExistsException exc) {
        LOGGER.warn("configured class '{}' on cell '{}' doesn't exist", classRef, cellDoc, exc);
      }
    } else {
      LOGGER.debug("class and field not configured for cell '{}'", cellDoc);
    }
    return Optional.absent();
  }

  @Override
  public Optional<String> getCellValueAsString(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    Object value = getCellValue(cellDocRef, onDoc);
    if (value != null) {
      return Optional.of(value.toString());
    }
    return Optional.absent();
  }

  @Override
  public Optional<Date> getCellDateValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException {
    Object value = getCellValue(cellDocRef, onDoc);
    if (value instanceof Date) {
      return Optional.of((Date) value);
    }
    return Optional.absent();
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
      Optional<BaseObject> obj = getRequestXObjectInStructEditor(cellDoc, onDoc);
      if (obj.isPresent()) {
        value = modelAccess.getProperty(obj.get(), fieldName.get());
      } else if (fieldName.get().equals("title")) {
        value = Strings.emptyToNull(onDoc.getTitle().trim());
      } else if (fieldName.get().equals("content")) {
        value = Strings.emptyToNull(onDoc.getContent().trim());
      }
    }
    return value;
  }

  @Override
  public Optional<DocumentReference> getSelectCellDocRef(DocumentReference cellDocRef) {
    DocumentReference selectCellDocRef = null;
    try {
      Optional<XWikiDocument> selectCellDoc = findParentCell(modelAccess.getDocument(cellDocRef),
          SelectTagPageType.PAGETYPE_NAME);
      if (selectCellDoc.isPresent()) {
        selectCellDocRef = selectCellDoc.get().getDocumentReference();
      }
      LOGGER.debug("getSelectCellDocRef: '{}' for cell '{}'", selectCellDocRef, cellDocRef);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent on doc '{}' doesn't exist", cellDocRef, exc);
    }
    return Optional.fromNullable(selectCellDocRef);
  }

  // TODO this method can be considered utility, move to another service
  private Optional<XWikiDocument> findParentCell(XWikiDocument cellDoc, String ptName)
      throws DocumentNotExistsException {
    while (cellDoc.getParentReference() != null) {
      cellDoc = modelAccess.getDocument(cellDoc.getParentReference());
      PageTypeReference ptRef = ptResolver.getPageTypeRefForDoc(cellDoc);
      if ((ptRef != null) && ptRef.getConfigName().equals(ptName)) {
        return Optional.of(cellDoc);
      }
    }
    return Optional.absent();
  }

  @Override
  public Optional<BaseObject> getXObjectInStructEditor(XWikiDocument cellDoc, XWikiDocument onDoc) {
    return getRequestXObjectInStructEditor(cellDoc, onDoc);
  }

  private Optional<BaseObject> getRequestXObjectInStructEditor(XWikiDocument cellDoc,
      XWikiDocument onDoc) {
    int objNb = 0;
    try {
      objNb = Integer.parseInt(context.getRequestParameter("objNb").or("0"));
    } catch (NumberFormatException nfe) {
      LOGGER.debug("unable to parse objNb from request: {}", nfe.getMessage());
    }
    return getXObjectInStructEditor(cellDoc, onDoc, objNb);
  }

  private Optional<BaseObject> getXObjectInStructEditor(XWikiDocument cellDoc, XWikiDocument onDoc,
      int objNb) {
    Optional<BaseObject> ret = Optional.absent();
    Optional<ClassReference> classRef = getCellClassRef(cellDoc);
    if (classRef.isPresent()) {
      ret = XWikiObjectFetcher.on(onDoc).filter(classRef.get()).filter(objNb).first();
    }
    LOGGER.info("getXObjectInStructEditor - for cellDoc '{}', onDoc '{}', class '{}', objNb '{}': "
        + "{}", cellDoc, onDoc, classRef.orNull(), objNb, ret.orNull());
    return ret;
  }

  private Optional<String> getCellFieldName(XWikiDocument cellDoc) {
    return modelAccess.getFieldValue(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME);
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
