package com.celements.structEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.util.ModelUtils;
import com.celements.model.util.References;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.xpn.xwiki.api.PropertyClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.PropertyInterface;

@Component("structuredDataEditor")
public class StructuredDataEditorScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(StructuredDataEditorScriptService.class);

  @Requirement(StructuredDataEditorClass.CLASS_DEF_HINT)
  StructEditorClass structuredDataEditorClass;

  @Requirement(TextAreaFieldEditorClass.CLASS_DEF_HINT)
  StructEditorClass textAreaFieldEditorClass;

  @Requirement
  IPageTypeResolverRole ptResolver;

  @Requirement
  IWebUtilsService webUtils;

  @Requirement
  ModelUtils modelUtils;

  @Requirement
  IModelAccessFacade modelAccess;

  public String getPrettyName(DocumentReference cellDocRef) {
    String prettyName = "";
    try {
      if (cellDocRef != null) {
        XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
        String dictKey = getDictionaryKey(cellDoc);
        prettyName = webUtils.getAdminMessageTool().get(dictKey);
        if (dictKey.equals(prettyName)) {
          prettyName = getXClassPrettyName(cellDoc);
        }
      }
      LOGGER.info("resolved prettyName '{}' for cell '{}'", prettyName, cellDocRef);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return prettyName;
  }

  String getDictionaryKey(XWikiDocument cellDoc) {
    String ret = new String();
    List<String> keyParts = new ArrayList<>();
    keyParts.add(resolveFormPrefix(cellDoc));
    keyParts.add(getCellClassName(cellDoc));
    keyParts.add(getCellFieldName(cellDoc));
    ret = Joiner.on('_').skipNulls().join(keyParts);
    return ret;
  }

  String resolveFormPrefix(XWikiDocument doc) {
    String prefix = null;
    try {
      while ((prefix == null) && (doc.getParentReference() != null)) {
        doc = modelAccess.getDocument(doc.getParentReference());
        PageTypeReference ptRef = ptResolver.getPageTypeRefForDoc(doc);
        if ((ptRef != null) && ptRef.getConfigName().equals(FormFieldPageType.PAGETYPE_NAME)) {
          prefix = modelAccess.getProperty(doc, FormFieldEditorClass.FORM_FIELD_PREFIX);
        }
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent '{}' on doc '{}' doesn't exist", doc.getParentReference(), doc, exc);
    }
    return prefix;
  }

  private String getXClassPrettyName(XWikiDocument cellDoc) {
    String prettyName = "";
    WikiReference wikiRef = References.extractRef(cellDoc.getDocumentReference(),
        WikiReference.class).get();
    DocumentReference classRef = modelUtils.resolveRef(getCellClassName(cellDoc),
        DocumentReference.class, wikiRef);
    try {
      PropertyInterface property = modelAccess.getDocument(classRef).getXClass().get(
          getCellFieldName(cellDoc));
      if ((property != null) && (property instanceof PropertyClass)) {
        prettyName = ((PropertyClass) property).getPrettyName();
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("configured class on cell '{}' doesn't exist", cellDoc, exc);
    }
    return prettyName;
  }

  private String getCellClassName(XWikiDocument cellDoc) {
    return Strings.emptyToNull(modelAccess.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME));
  }

  private String getCellFieldName(XWikiDocument cellDoc) {
    return Strings.emptyToNull(modelAccess.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME));
  }

  public Map<String, Integer> getRowsAndColsFromTextarea(DocumentReference cellDocRef) {
    Map<String, Integer> retMap = new HashMap<>();
    BaseObject textAreaFieldConfig;
    DocumentReference textAreaFieldClassRef = textAreaFieldEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    try {
      textAreaFieldConfig = modelAccess.getXObject(cellDocRef, textAreaFieldClassRef);
      retMap.put("rows", textAreaFieldConfig.getIntValue("textarea_field_rows"));
      retMap.put("cols", textAreaFieldConfig.getIntValue("textarea_field_cols"));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", textAreaFieldClassRef, exc);
    }
    return retMap;
  }
}
