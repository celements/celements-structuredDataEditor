package com.celements.structEditor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

@Component
public class DefaultStructuredDataEditorService implements StructuredDataEditorService {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultStructuredDataEditorService.class);

  @Requirement
  IPageTypeResolverRole ptResolver;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  ModelUtils modelUtils;

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  ModelContext modelContext;

  @Override
  public Optional<String> getAttributeName(XWikiDocument cellDoc) {
    return getAttributeName(cellDoc, true);
  }

  private Optional<String> getAttributeName(XWikiDocument cellDoc, boolean withObjNb) {
    List<String> nameParts = new ArrayList<>();
    Optional<DocumentReference> classRef = getCellClassDocRef(cellDoc);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    if (fieldName.isPresent()) {
      if (classRef.isPresent()) {
        nameParts.add(modelUtils.serializeRefLocal(classRef.get()));
        if (withObjNb) {
          BaseObject obj = modelAccess.getXObject(cellDoc, classRef.get());
          nameParts.add(Integer.toString(obj != null ? obj.getNumber() : -1));
        }
      }
      nameParts.add(getCellFieldName(cellDoc).get());
    }
    String name = Joiner.on('_').join(nameParts);
    LOGGER.info("getAttributeName: '{}' for cell '{}' withObjNb '{}'", name, cellDoc, withObjNb);
    return Optional.fromNullable(Strings.emptyToNull(name));
  }

  @Override
  public Optional<String> getPrettyName(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    String prettyName = "";
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    String dictKey = Joiner.on('_').skipNulls().join(resolveFormPrefix(cellDoc), getAttributeName(
        cellDoc, false).orNull());
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

  String resolveFormPrefix(XWikiDocument cellDoc) {
    String prefix = null;
    XWikiDocument doc = cellDoc;
    try {
      while ((prefix == null) && (doc.getParentReference() != null)) {
        doc = modelAccess.getDocument(doc.getParentReference());
        PageTypeReference ptRef = ptResolver.getPageTypeRefForDoc(doc);
        if ((ptRef != null) && ptRef.getConfigName().equals(FormFieldPageType.PAGETYPE_NAME)) {
          prefix = Strings.emptyToNull(modelAccess.getProperty(doc,
              FormFieldEditorClass.FIELD_PREFIX));
        }
      }
      LOGGER.debug("resolveFormPrefix: '{}' for cell '{}'", prefix, cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent '{}' on doc '{}' doesn't exist", doc.getParentReference(), doc, exc);
    }
    return prefix;
  }

  Optional<String> getXClassPrettyName(XWikiDocument cellDoc) {
    String prettyName = null;
    Optional<DocumentReference> classRef = getCellClassDocRef(cellDoc);
    Optional<String> fieldName = getCellFieldName(cellDoc);
    if (classRef.isPresent() && fieldName.isPresent()) {
      try {
        PropertyClass property = (PropertyClass) modelAccess.getDocument(
            classRef.get()).getXClass().get(fieldName.get());
        if (property != null) {
          prettyName = Strings.emptyToNull(property.getPrettyName());
        }
        LOGGER.debug("getXClassPrettyName: '{}' for cell '{}' and class '{}'", prettyName, cellDoc,
            classRef);
      } catch (DocumentNotExistsException exc) {
        LOGGER.warn("configured class '{}' on cell '{}' doesn't exist", classRef, cellDoc, exc);
      }
    } else {
      LOGGER.debug("class and field not configured for cell '{}'", cellDoc);
    }
    return Optional.fromNullable(prettyName);
  }

  @Override
  public Optional<DocumentReference> getCellClassDocRef(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    return getCellClassDocRef(modelAccess.getDocument(cellDocRef));
  }

  @Override
  public String getCellValueAsString(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    String retVal = new String();
    XWikiDocument doc = modelContext.getDoc();
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    DocumentReference docRef = doc.getDocumentReference();
    Optional<DocumentReference> cellClassDocRef = getStructDataEditorService().getCellClassDocRef(
        cellDocRef);
    Optional<String> celFieldName = getCellFieldName(cellDoc);
    if (cellClassDocRef.isPresent() && celFieldName.isPresent()) {
      BaseObject baseObj = modelAccess.getXObject(docRef, cellClassDocRef.get());
      retVal = baseObj.getStringValue(getCellFieldName(cellDoc).get());
    }
    return retVal;
  }

  private Optional<DocumentReference> getCellClassDocRef(XWikiDocument cellDoc) {
    String className = modelAccess.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME);
    if (!Strings.isNullOrEmpty(className)) {
      return Optional.of(modelUtils.resolveRef(className, DocumentReference.class,
          cellDoc.getDocumentReference()));
    }
    return Optional.absent();
  }

  private Optional<String> getCellFieldName(XWikiDocument cellDoc) {
    String fieldName = Strings.emptyToNull(modelAccess.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME));
    return Optional.fromNullable(fieldName);
  }

  private StructuredDataEditorService getStructDataEditorService() {
    return Utils.getComponent(StructuredDataEditorService.class);
  }

}
