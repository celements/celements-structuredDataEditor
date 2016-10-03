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
  public String getAttributeName(XWikiDocument cellDoc) {
    return getAttributeName(cellDoc, true);
  }

  private String getAttributeName(XWikiDocument cellDoc, boolean withObjNb) {
    List<String> nameParts = new ArrayList<>();
    String className = getCellClassName(cellDoc);
    if (!Strings.isNullOrEmpty(className)) {
      nameParts.add(className);
      if (withObjNb) {
        nameParts.add(Integer.toString(getObjNumber(cellDoc, className)));
      }
    }
    nameParts.add(Strings.emptyToNull(getCellFieldName(cellDoc)));
    String name = Joiner.on('_').skipNulls().join(nameParts);
    LOGGER.info("getAttributeName: '{}' for cell '{}' withObjNb '{}'", name, cellDoc, withObjNb);
    return name;
  }

  private int getObjNumber(XWikiDocument cellDoc, String className) {
    DocumentReference classRef = modelUtils.resolveRef(className, DocumentReference.class,
        cellDoc.getDocumentReference().getWikiReference());
    BaseObject obj = modelAccess.getXObject(cellDoc, classRef);
    return obj != null ? obj.getNumber() : -1;
  }

  @Override
  public String getPrettyName(DocumentReference cellDocRef) throws DocumentNotExistsException {
    String prettyName = "";
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    String dictKey = Joiner.on('_').skipNulls().join(Strings.emptyToNull(resolveFormPrefix(
        cellDoc)), Strings.emptyToNull(getAttributeName(cellDoc, false)));
    LOGGER.debug("getPrettyName: dictKey '{}' for cell '{}'", dictKey, cellDoc);
    prettyName = webUtils.getAdminMessageTool().get(dictKey);
    if (dictKey.equals(prettyName)) {
      prettyName = getXClassPrettyName(cellDoc);
      if (prettyName.isEmpty()) {
        prettyName = dictKey;
      }
    }
    LOGGER.info("getPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    return prettyName;
  }

  String resolveFormPrefix(XWikiDocument cellDoc) {
    String prefix = null;
    XWikiDocument doc = cellDoc;
    try {
      while ((prefix == null) && (doc.getParentReference() != null)) {
        doc = modelAccess.getDocument(doc.getParentReference());
        PageTypeReference ptRef = ptResolver.getPageTypeRefForDoc(doc);
        if ((ptRef != null) && ptRef.getConfigName().equals(FormFieldPageType.PAGETYPE_NAME)) {
          prefix = modelAccess.getProperty(doc, FormFieldEditorClass.FIELD_PREFIX);
        }
      }
      LOGGER.debug("resolveFormPrefix: '{}' for cell '{}'", prefix, cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent '{}' on doc '{}' doesn't exist", doc.getParentReference(), doc, exc);
    }
    return prefix;
  }

  String getXClassPrettyName(XWikiDocument cellDoc) {
    String prettyName = "";
    try {
      DocumentReference classRef = getCellClassDocRef(cellDoc);
      PropertyClass property = (PropertyClass) modelAccess.getDocument(classRef).getXClass().get(
          getCellFieldName(cellDoc));
      if (property != null) {
        prettyName = property.getPrettyName();
      }
      LOGGER.debug("getXClassPrettyName: '{}' for cell '{}' and class '{}'", prettyName, cellDoc,
          classRef);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("configured class on cell '{}' doesn't exist", cellDoc, exc);
    }
    return prettyName;
  }

  @Override
  public DocumentReference getCellClassDocRef(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    return getCellClassDocRef(modelAccess.getDocument(cellDocRef));
  }

  @Override
  public String getCellValueAsString(DocumentReference cellDocRef)
      throws DocumentNotExistsException {
    XWikiDocument doc = modelContext.getDoc();
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    DocumentReference docRef = doc.getDocumentReference();
    BaseObject baseObj = modelAccess.getXObject(docRef,
        getStructDataEditorService().getCellClassDocRef(cellDocRef));
    return baseObj.getStringValue(getCellFieldName(cellDoc));
  }

  private DocumentReference getCellClassDocRef(XWikiDocument cellDoc) {
    return modelUtils.resolveRef(getCellClassName(cellDoc), DocumentReference.class,
        cellDoc.getDocumentReference());
  }

  private String getCellClassName(XWikiDocument cellDoc) {
    return modelAccess.getProperty(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME);
  }

  private String getCellFieldName(XWikiDocument cellDoc) {
    return modelAccess.getProperty(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME);
  }

  private StructuredDataEditorService getStructDataEditorService() {
    return Utils.getComponent(StructuredDataEditorService.class);
  }

}
