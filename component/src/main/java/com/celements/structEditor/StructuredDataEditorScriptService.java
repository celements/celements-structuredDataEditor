package com.celements.structEditor;

import static com.celements.structEditor.classes.SelectTagEditorClass.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.celements.struct.SelectTagServiceRole;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.PropertyClass;

@Component("structuredDataEditor")
public class StructuredDataEditorScriptService implements ScriptService {

  private final static Logger LOGGER = LoggerFactory.getLogger(
      StructuredDataEditorScriptService.class);

  @Requirement
  StructuredDataEditorService service;

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  private SelectTagServiceRole selectTagService;

  @Requirement
  static ModelContext context;

  private static final Function<PropertyClass, com.xpn.xwiki.api.PropertyClass> PROPCLASS_TO_API = new Function<PropertyClass, com.xpn.xwiki.api.PropertyClass>() {

    @Override
    public com.xpn.xwiki.api.PropertyClass apply(PropertyClass propClass) {
      return new com.xpn.xwiki.api.PropertyClass(propClass, context.getXWikiContext());
    }
  };

  public String getAttributeName(DocumentReference cellDocRef) {
    String ret = "";
    if (cellDocRef != null) {
      try {
        XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
        ret = service.getAttributeName(cellDoc, null).or(ret);
      } catch (DocumentNotExistsException exc) {
        LOGGER.info("cell doesn't exist '{}'", cellDocRef, exc);
      }
    }
    return ret;
  }

  public String getPrettyName(DocumentReference cellDocRef) {
    String prettyName = "";
    if (cellDocRef != null) {
      try {
        prettyName = service.getPrettyName(cellDocRef).or("");
      } catch (DocumentNotExistsException exc) {
        LOGGER.info("cell doesn't exist '{}'", cellDocRef, exc);
      }
    }
    return prettyName;
  }

  public Map<String, String> getTextAttributes(DocumentReference cellDocRef) {
    Map<String, String> retMap = new LinkedHashMap<>();
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      retMap.put("type", "text");
      addNameAttributeToMap(retMap, cellDoc);
      retMap.put("value", context.getDoc().getTemplate());
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retMap;
  }

  public Map<String, String> getTextAreaAttributes(DocumentReference cellDocRef) {
    Map<String, String> retMap = new LinkedHashMap<>();
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      addNameAttributeToMap(retMap, cellDoc);
      addAttributeToMap(retMap, "rows", cellDoc, TextAreaFieldEditorClass.FIELD_ROWS);
      addAttributeToMap(retMap, "cols", cellDoc, TextAreaFieldEditorClass.FIELD_COLS);
      addAttributeToMap(retMap, "isRichtext", cellDoc, TextAreaFieldEditorClass.FIELD_IS_RICHTEXT);
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Properties for docRef {} does not exist {}", cellDocRef, exc);
    }
    return retMap;
  }

  public String getTextAreaContent(DocumentReference cellDocRef) {
    String retVal = new String();
    try {
      retVal = modelAccess.getProperty(cellDocRef, TextAreaFieldEditorClass.FIELD_VALUE);
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("Properties for docRef {} does not exist {}", cellDocRef, exc);
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

  private void addNameAttributeToMap(Map<String, String> map, XWikiDocument cellDoc) {
    Optional<String> val = service.getAttributeName(cellDoc, context.getDoc());
    if (val.isPresent()) {
      map.put("name", val.get());
    }
  }

  public String getCellValueAsString(DocumentReference cellDocRef) {
    String ret = "";
    try {
      ret = service.getCellValueAsString(cellDocRef, context.getDoc()).or("");
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return ret;
  }

  public List<String> getCellListValue(DocumentReference cellDocRef) {
    List<String> ret = Collections.emptyList();
    try {
      ret = service.getCellListValue(cellDocRef, context.getDoc());
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return ret;
  }

  public Optional<com.xpn.xwiki.api.PropertyClass> getCellPropertyClass(
      DocumentReference cellDocRef) {
    Optional<PropertyClass> propClass;
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      propClass = service.getCellPropertyClass(cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.info("cell doesn't exist '{}'", cellDocRef, exc);
      propClass = Optional.absent();
    }
    return propClass.transform(PROPCLASS_TO_API);
  }

  public boolean isSelectMultiselect(DocumentReference cellDocRef) {
    boolean isMultiselect = false;
    try {
      isMultiselect = modelAccess.getFieldValue(cellDocRef, FIELD_IS_MULTISELECT).or(false);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return isMultiselect;
  }

  public List<String> getSelectTagAutocompleteJsPathList() {
    return service.getSelectTagAutocompleteJsPathList();
  }

  public java.util.Optional<SelectAutocompleteRole> getSelectTagAutoCompleteImpl(
      DocumentReference cellDocRef) {
    return selectTagService.getTypeImpl(cellDocRef);
  }
}
