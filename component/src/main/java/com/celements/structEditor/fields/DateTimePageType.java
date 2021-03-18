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
package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.DateTimePickerEditorClass.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.common.date.DateFormat;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.classes.DateTimePickerEditorClass.Type;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(DateTimePageType.PAGETYPE_NAME)
public class DateTimePageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "DateTime";

  static final String VIEW_TEMPLATE_NAME = "DateTimeView";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  public Optional<String> tagName() {
    return Optional.of("cel-input-date-time");
  }

  private static final Map<Type, String> PICKER_TYPE_CSS_CLASS_MAP = ImmutableMap.of(
      Type.DATE_PICKER, "cel_datePicker",
      Type.TIME_PICKER, "cel_timePicker",
      Type.DATE_TIME_PICKER, "cel_dateTimePicker");

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addNonEmptyAttribute("type", "text");
    try {
      Optional<Date> cellValue = getStructDataEditorService().getCellDateValue(cellDocRef,
          modelContext.getCurrentDoc().orNull());
      if (cellValue.isPresent()) {
        Optional<String> dateFormat = getStructDataEditorService().getDateFormatFromField(
            cellDocRef);
        String value = null;
        if (dateFormat.isPresent()) {
          value = DateFormat.formatter(dateFormat.get()).apply(cellValue.get().toInstant());
        }
        attrBuilder.addNonEmptyAttribute("value", value);
      }
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("type", "text");
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, modelContext.getCurrentDoc().orNull()).orElse(""));
      Type pickerType = XWikiObjectFetcher.on(cellDoc)
          .fetchField(FIELD_TYPE)
          .stream().flatMap(List::stream)
          .findFirst().orElse(Type.DATE_PICKER);
      attrBuilder.addCssClasses(PICKER_TYPE_CSS_CLASS_MAP.get(pickerType));
      List<String> dataValueList = new ArrayList<>();
      XWikiObjectFetcher.on(cellDoc).fetchField(FIELD_FORMAT)
          .stream().findFirst()
          .map(format -> "\"format\" : \"" + format + "\"")
          .ifPresent(dataValueList::add);
      XWikiObjectFetcher.on(cellDoc).fetchField(FIELD_ATTRIBUTES)
          .stream().findFirst()
          .ifPresent(dataValueList::add);
      String dataAttr = Joiner.on(',').skipNulls().join(dataValueList);
      if (!dataAttr.isEmpty()) {
        attrBuilder.addNonEmptyAttribute("data-pickerAttr", "{" + dataAttr + "}");
      }
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
