package com.celements.structEditor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;

@Component("structuredDataEditor")
public class StructuredDataEditorScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(StructuredDataEditorScriptService.class);

  @Requirement
  StructuredDataEditorService service;

  @Requirement
  IModelAccessFacade modelAccess;

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

  public Map<String, Integer> getRowsAndColsFromTextarea(DocumentReference cellDocRef) {
    Map<String, Integer> retMap = new HashMap<>();
    try {
      retMap.put("rows", modelAccess.getProperty(cellDocRef,
          TextAreaFieldEditorClass.TEXTAREA_FIELD_ROWS));
      retMap.put("cols", modelAccess.getProperty(cellDocRef,
          TextAreaFieldEditorClass.TEXTAREA_FIELD_COLS));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retMap;
  }

  public String getValueFromTextArea(DocumentReference cellDocRef) {
    String retVal = new String();
    try {
      retVal = modelAccess.getProperty(cellDocRef, TextAreaFieldEditorClass.TEXTAREA_FIELD_VALUE);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retVal;
  }
}
