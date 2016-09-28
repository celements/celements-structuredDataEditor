package com.celements.structEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClasses;
import com.xpn.xwiki.objects.BaseObject;

@Component("structuredDataEditor")
public class StructuredDataEditorScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(StructuredDataEditorScriptService.class);

  @Requirement(StructuredDataEditorClasses.CLASS_DEF_HINT)
  private StructEditorClass structuredDataEditorClass;

  @Requirement
  protected IModelAccessFacade modelAccess;

  public String getDictionaryKey(DocumentReference cellDocRef) {
    String retVal = new String();
    DocumentReference structuredDataEditorClassRef = structuredDataEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    BaseObject structuredDataEditorConfig;
    try {
      structuredDataEditorConfig = modelAccess.getXObject(cellDocRef, structuredDataEditorClassRef);
      retVal = structuredDataEditorConfig.getStringValue("edit_field_class_fullname") + "_"
          + structuredDataEditorConfig.getStringValue("edit_field_name");
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", structuredDataEditorClassRef,
          exc);
    }
    return retVal;
  }
}
