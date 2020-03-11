package com.celements.structEditor;

import static com.celements.structEditor.classes.SelectTagEditorClass.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.lambda.LambdaExceptionUtil.ThrowingFunction;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.struct.SelectTagServiceRole;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.PropertyClass;

@Component("structuredDataEditor")
public class StructuredDataEditorScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      StructuredDataEditorScriptService.class);

  @Requirement
  StructuredDataEditorService service;

  @Requirement
  IModelAccessFacade modelAccess;

  @Requirement
  private SelectTagServiceRole selectTagService;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private ModelContext context;

  public String getAttributeName(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> service.getAttributeName(cellDoc, null))
        .orElse("");
  }

  public String getPrettyName(DocumentReference cellDocRef) {
    return getFromCellDocRef(cellDocRef, service::getPrettyName)
        .orElse(Optional.empty())
        .orElse("");
  }

  public Map<String, String> getTextAttributes(DocumentReference cellDocRef) {
    final Map<String, String> retMap = new LinkedHashMap<>();
    getFromCellDoc(cellDocRef, cellDoc -> {
      retMap.put("type", "text");
      addNameAttributeToMap(retMap, cellDoc);
      retMap.put("value", context.getDoc().getTemplate());
      return Optional.empty();
    });
    return retMap;
  }

  public Map<String, String> getTextAreaAttributes(DocumentReference cellDocRef) {
    final Map<String, String> retMap = new LinkedHashMap<>();
    getFromCellDoc(cellDocRef, cellDoc -> {
      addNameAttributeToMap(retMap, cellDoc);
      addAttributeToMap(retMap, "rows", cellDoc, TextAreaFieldEditorClass.FIELD_ROWS);
      addAttributeToMap(retMap, "cols", cellDoc, TextAreaFieldEditorClass.FIELD_COLS);
      addAttributeToMap(retMap, "isRichtext", cellDoc, TextAreaFieldEditorClass.FIELD_IS_RICHTEXT);
      return Optional.empty();
    });
    return retMap;
  }

  public String getTextAreaContent(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> XWikiObjectFetcher.on(cellDoc)
        .fetchField(TextAreaFieldEditorClass.FIELD_VALUE).stream().findFirst())
            .orElse("");
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
    return getFromCellDocRef(cellDocRef,
        ref -> service.getCellValueAsString(cellDocRef, context.getDoc()))
            .orElse(Optional.empty())
            .orElse("");
  }

  public List<String> getCellListValue(DocumentReference cellDocRef) {
    return getFromCellDocRef(cellDocRef, ref -> service.getCellListValue(ref, context.getDoc()))
        .orElseGet(ArrayList::new);
  }

  public com.google.common.base.Optional<com.xpn.xwiki.api.PropertyClass> getCellPropertyClass(
      DocumentReference cellDocRef) {
    Optional<PropertyClass> propClass = getFromCellDoc(cellDocRef, service::getCellPropertyClass);
    return com.google.common.base.Optional.fromJavaUtil(propClass
        .map(prop -> new com.xpn.xwiki.api.PropertyClass(prop, context.getXWikiContext())));
  }

  public Optional<ClassReference> getCellClassRef(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, service::getCellClassRef);
  }

  public Optional<String> getCellFieldName(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, service::getCellFieldName);
  }

  public boolean isSelectMultiselect(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> XWikiObjectFetcher.on(cellDoc)
        .fetchField(FIELD_IS_MULTISELECT).stream().findFirst())
            .orElse(false);
  }

  public List<String> getSelectTagAutocompleteJsPathList() {
    return service.getSelectTagAutocompleteJsPathList();
  }

  public Optional<SelectAutocompleteRole> getSelectTagAutoCompleteImpl(
      DocumentReference cellDocRef) {
    return selectTagService.getTypeImpl(cellDocRef);
  }

  private <T> Optional<T> getFromCellDocRef(DocumentReference cellDocRef,
      ThrowingFunction<DocumentReference, T, DocumentNotExistsException> func) {
    try {
      if (rightsAccess.hasAccessLevel(cellDocRef, EAccessLevel.VIEW)) {
        return Optional.ofNullable(func.apply(cellDocRef));
      }
    } catch (Exception exc) {
      LOGGER.info("getFromCellDocRef - failed for [{}]", cellDocRef, exc);
    }
    return Optional.empty();
  }

  private <T> Optional<T> getFromCellDoc(DocumentReference cellDocRef,
      Function<XWikiDocument, Optional<T>> func) {
    try {
      if (rightsAccess.hasAccessLevel(cellDocRef, EAccessLevel.VIEW)) {
        return func.apply(modelAccess.getDocument(cellDocRef));
      }
    } catch (Exception exc) {
      LOGGER.info("getFromCellDoc - failed for [{}]", cellDocRef, exc);
    }
    return Optional.empty();
  }
}
