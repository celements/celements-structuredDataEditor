package com.celements.structEditor;

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
import com.celements.structEditor.classes.SelectTagEditorClass;
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
    return getAttributeName(cellDocRef, null);
  }

  public String getAttributeNameForCurrentDoc(DocumentReference cellDocRef) {
    return context.getCurrentDoc().toJavaUtil()
        .map(onDoc -> getAttributeName(cellDocRef, onDoc))
        .orElse("");
  }

  private String getAttributeName(DocumentReference cellDocRef, XWikiDocument onDoc) {
    return getFromCellDoc(cellDocRef, cellDoc -> service.getAttributeName(cellDoc, onDoc))
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
      retMap.put("value", context.getCurrentDoc().toJavaUtil()
          .map(XWikiDocument::getTemplate).orElse(""));
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
    modelAccess.getFieldValue(cellDoc, field).toJavaUtil()
        .map(Object::toString)
        .ifPresent(val -> map.put(attrName, val));
  }

  private void addNameAttributeToMap(Map<String, String> map, XWikiDocument cellDoc) {
    service.getAttributeName(cellDoc, context.getCurrentDoc().orNull())
        .ifPresent(val -> map.put("name", val));
  }

  public String getCellValueAsString(DocumentReference cellDocRef) {
    return getFromCellDocRef(cellDocRef,
        ref -> service.getCellValueAsString(cellDocRef, context.getCurrentDoc().orNull()))
            .orElse(Optional.empty())
            .orElse("");
  }

  public String getCellValueFromRequest(DocumentReference cellDocRef) {
    String name = getAttributeNameForCurrentDoc(cellDocRef);
    return context.getRequestParameter(name).toJavaUtil().orElse("");
  }

  public List<String> getCellListValue(DocumentReference cellDocRef) {
    return getFromCellDocRef(cellDocRef, ref -> service.getCellListValue(ref,
        context.getCurrentDoc().orNull())).orElseGet(ArrayList::new);
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
        .fetchField(SelectTagEditorClass.FIELD_IS_MULTISELECT).stream().findFirst())
            .orElse(false);
  }

  public List<String> getSelectTagAutocompleteJsPathList() {
    return service.getSelectTagAutocompleteJsPathList();
  }

  public Optional<SelectAutocompleteRole> getSelectTagAutoCompleteImpl(
      DocumentReference cellDocRef) {
    return selectTagService.getTypeImpl(cellDocRef);
  }

  public boolean isMultilingual(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> Optional.of(service.isMultilingual(cellDoc)))
        .orElse(false);
  }

  public Optional<String> getLangNameAttributeForCurrentDoc(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> context.getCurrentDoc().toJavaUtil()
        .flatMap(onDoc -> service.getLangNameAttribute(cellDoc, onDoc)));
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
