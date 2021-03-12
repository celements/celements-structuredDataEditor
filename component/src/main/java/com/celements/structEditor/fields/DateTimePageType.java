package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.DateTimePickerEditorClass.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.DateTimePickerEditorClass.Type;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
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
    return Optional.of("cel-date-time");
  }

  private static final Map<Type, String> PICKER_TYPE_CSS_CLASS_MAP = ImmutableMap.of(
      Type.DATE_PICKER, "cel_datePicker", Type.TIME_PICKER, "cel_timePicker", Type.DATE_TIME_PICKER,
      "cel_dateTimePicker");

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
          value = new SimpleDateFormat(dateFormat.get()).format(cellValue.get());
        }
        attrBuilder.addNonEmptyAttribute("value", value);
      }
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("type", "text");
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, modelContext.getCurrentDoc().orNull()).orElse(""));
      List<Type> typeList = modelAccess.getFieldValue(cellDocRef, FIELD_TYPE)
          .or(Collections.<Type>emptyList());
      Type pickerType = Iterables.getFirst(typeList, Type.DATE_PICKER);
      attrBuilder.addCssClasses(PICKER_TYPE_CSS_CLASS_MAP.get(pickerType));
      List<String> dataValueList = new ArrayList<>();
      modelAccess.getFieldValue(cellDocRef, FIELD_FORMAT).toJavaUtil()
          .map(format -> "\"format\" : \"" + format + "\"")
          .ifPresent(dataValueList::add);
      modelAccess.getFieldValue(cellDocRef, FIELD_ATTRIBUTES).toJavaUtil()
          .ifPresent(dataValueList::add);
      String dataAttr = Joiner.on(',').skipNulls().join(dataValueList);
      if (!dataAttr.isEmpty()) {
        attrBuilder.addNonEmptyAttribute("data-pickerAttr", new StringBuilder("{").append(
            dataAttr).append("}").toString());
      }
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
