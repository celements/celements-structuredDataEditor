/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.structEditor;

import static java.util.stream.Collectors.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.MoreOptional;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.struct.SelectTagServiceRole;
import com.celements.struct.edit.autocomplete.AutocompleteRole;
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

  @Requirement
  private ComponentManager componentManager;

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
    return getFromCellDoc(cellDocRef, service::getPrettyName)
        .orElse("");
  }

  public Map<String, String> getTextAttributes(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> {
      Map<String, String> retMap = new LinkedHashMap<>();
      retMap.put("type", "text");
      addNameAttributeToMap(retMap, cellDoc);
      retMap.put("value", context.getCurrentDoc().toJavaUtil()
          .map(XWikiDocument::getTemplate).orElse(""));
      return Optional.of(retMap);
    }).orElseGet(LinkedHashMap::new);
  }

  public Map<String, String> getTextAreaAttributes(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> {
      Map<String, String> retMap = new LinkedHashMap<>();
      addNameAttributeToMap(retMap, cellDoc);
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc);
      fetcher.fetchField(TextAreaFieldEditorClass.FIELD_ROWS).stream()
          .forEach(val -> retMap.put("rows", val.toString()));
      fetcher.fetchField(TextAreaFieldEditorClass.FIELD_COLS).stream()
          .forEach(val -> retMap.put("cols", val.toString()));
      fetcher.fetchField(TextAreaFieldEditorClass.FIELD_IS_RICHTEXT).stream()
          .forEach(val -> retMap.put("isRichtext", val.toString()));
      return Optional.of(retMap);
    }).orElseGet(LinkedHashMap::new);
  }

  public String getTextAreaContent(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> XWikiObjectFetcher.on(cellDoc)
        .fetchField(TextAreaFieldEditorClass.FIELD_VALUE).stream().findFirst())
            .orElse("");
  }

  private void addNameAttributeToMap(Map<String, String> map, XWikiDocument cellDoc) {
    service.getAttributeName(cellDoc, context.getCurrentDoc().orNull())
        .ifPresent(val -> map.put("name", val));
  }

  public List<com.xpn.xwiki.api.Object> getObjectsForCell(DocumentReference cellDocRef) {
    return streamFromCellDoc(cellDocRef, cellDoc -> service
        .streamXObjectsForCell(cellDoc, context.getCurrentDoc().orNull()))
            .map(o -> new com.xpn.xwiki.api.Object(o, context.getXWikiContext()))
            .collect(toList());
  }

  public String getCellValueAsString(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> service
        .getCellValueAsString(cellDoc, context.getCurrentDoc().orNull()))
            .orElse("");
  }

  public String getCellValueFromRequest(DocumentReference cellDocRef) {
    String name = getAttributeNameForCurrentDoc(cellDocRef);
    return context.getRequestParameter(name).toJavaUtil().orElse("");
  }

  public List<String> getCellListValue(DocumentReference cellDocRef) {
    return streamFromCellDoc(cellDocRef, cellDoc -> service
        .getCellListValue(cellDoc, context.getCurrentDoc().orNull()).stream())
            .collect(toList());
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

  public Optional<AutocompleteRole> getSelectTagAutoCompleteImpl(
      DocumentReference cellDocRef) {
    return selectTagService.getTypeImpl(cellDocRef);
  }

  public Optional<AutocompleteRole> getSelectTagAutoCompleteImpl(String type) {
    try {
      return Optional.of(componentManager.lookup(AutocompleteRole.class, type));
    } catch (ComponentLookupException exc) {
      return Optional.empty();
    }
  }

  public boolean isMultilingual(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> Optional.of(service.isMultilingual(cellDoc)))
        .orElse(false);
  }

  public Optional<String> getLangNameAttributeForCurrentDoc(DocumentReference cellDocRef) {
    return getFromCellDoc(cellDocRef, cellDoc -> context.getCurrentDoc().toJavaUtil()
        .flatMap(onDoc -> service.getLangNameAttribute(cellDoc, onDoc)));
  }

  private <T> Optional<T> getFromCellDoc(DocumentReference cellDocRef,
      Function<XWikiDocument, Optional<T>> func) {
    return streamFromCellDoc(cellDocRef, func.andThen(MoreOptional::stream))
        .findFirst();
  }

  private <T> Stream<T> streamFromCellDoc(DocumentReference cellDocRef,
      Function<XWikiDocument, Stream<T>> func) {
    try {
      if (rightsAccess.hasAccessLevel(cellDocRef, EAccessLevel.VIEW)) {
        return func.apply(modelAccess.getDocument(cellDocRef));
      }
    } catch (Exception exc) {
      LOGGER.info("getFromCellDoc - failed for [{}]", cellDocRef, exc);
    }
    return Stream.empty();
  }
}
