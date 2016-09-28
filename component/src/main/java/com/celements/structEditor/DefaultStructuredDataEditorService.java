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
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.xpn.xwiki.api.PropertyClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.PropertyInterface;

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

  @Override
  public String getPrettyName(DocumentReference cellDocRef) throws DocumentNotExistsException {
    String prettyName = "";
    XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
    String dictKey = getDictionaryKey(cellDoc);
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

  String getDictionaryKey(XWikiDocument cellDoc) {
    List<String> keyParts = new ArrayList<>();
    keyParts.add(Strings.emptyToNull(resolveFormPrefix(cellDoc)));
    keyParts.add(Strings.emptyToNull(getCellClassName(cellDoc)));
    keyParts.add(Strings.emptyToNull(getCellFieldName(cellDoc)));
    String dictKey = Joiner.on('_').skipNulls().join(keyParts);
    LOGGER.debug("getDictionaryKey: '{}' for cell '{}'", dictKey, cellDoc);
    return dictKey;
  }

  String resolveFormPrefix(XWikiDocument cellDoc) {
    String prefix = null;
    XWikiDocument doc = cellDoc;
    try {
      while ((prefix == null) && (doc.getParentReference() != null)) {
        doc = modelAccess.getDocument(doc.getParentReference());
        PageTypeReference ptRef = ptResolver.getPageTypeRefForDoc(doc);
        if ((ptRef != null) && ptRef.getConfigName().equals(FormFieldPageType.PAGETYPE_NAME)) {
          prefix = modelAccess.getProperty(doc, FormFieldEditorClass.FORM_FIELD_PREFIX);
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
      DocumentReference classRef = modelUtils.resolveRef(getCellClassName(cellDoc),
          DocumentReference.class, cellDoc.getDocumentReference());
      PropertyInterface property = modelAccess.getDocument(classRef).getXClass().get(
          getCellFieldName(cellDoc));
      if ((property != null) && (property instanceof PropertyClass)) {
        prettyName = ((PropertyClass) property).getPrettyName();
      }
      LOGGER.debug("getXClassPrettyName: '{}' for cell '{}'", prettyName, cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("configured class on cell '{}' doesn't exist", cellDoc, exc);
    }
    return prettyName;
  }

  private String getCellClassName(XWikiDocument cellDoc) {
    return modelAccess.getProperty(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME);
  }

  private String getCellFieldName(XWikiDocument cellDoc) {
    return modelAccess.getProperty(cellDoc, StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME);
  }

}
