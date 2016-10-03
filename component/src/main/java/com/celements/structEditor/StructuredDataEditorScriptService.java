package com.celements.structEditor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("structuredDataEditor")
public class StructuredDataEditorScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(StructuredDataEditorScriptService.class);

  @Requirement
  StructuredDataEditorService service;

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  ModelContext context;

  public String getPrettyName(DocumentReference cellDocRef) {
    String prettyName = "";
    if (cellDocRef != null) {
      try {
        prettyName = service.getPrettyName(cellDocRef);
      } catch (DocumentNotExistsException exc) {
        LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
      }
    }
    return prettyName;
  }

  public Map<String, String> getTextAttributes(DocumentReference cellDocRef) {
    Map<String, String> retMap = new LinkedHashMap<>();
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      retMap.put("type", "text");
      retMap.put("name", service.getAttributeName(cellDoc));
      retMap.put("value", context.getDoc().getTemplate());
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retMap;
  }

  public Map<String, String> getTextAreaAttributes(DocumentReference cellDocRef) {
    Map<String, String> retMap = new LinkedHashMap<>();
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      retMap.put("name", service.getAttributeName(cellDoc));
      addAttributeToMap(retMap, "rows", cellDoc, TextAreaFieldEditorClass.FIELD_ROWS);
      addAttributeToMap(retMap, "cols", cellDoc, TextAreaFieldEditorClass.FIELD_COLS);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retMap;
  }

  public String getTextAreaContent(DocumentReference cellDocRef) {
    String retVal = new String();
    try {
      retVal = modelAccess.getProperty(cellDocRef, TextAreaFieldEditorClass.FIELD_VALUE);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retVal;
  }

  private void addAttributeToMap(Map<String, String> map, String attrName, XWikiDocument cellDoc,
      ClassField<?> field) {
    Object val = modelAccess.getProperty(cellDoc, field);
    if (val != null) {
      map.put(attrName, val.toString());
    }
  }
}
