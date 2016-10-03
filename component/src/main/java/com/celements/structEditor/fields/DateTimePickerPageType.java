package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.DateTimePickerEditorClass.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.DateTimePickerEditorClass.Type;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(DateTimePickerPageType.PAGETYPE_NAME)
public class DateTimePickerPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(DateTimePickerPageType.class);

  public static final String PAGETYPE_NAME = "DateTimePicker";

  static final String VIEW_TEMPLATE_NAME = "DateTimePickerView";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.of("input");
  }

  private static final Map<Type, String> PICKER_TYPE_CSS_CLASS_MAP = ImmutableMap.of(
      Type.DATE_PICKER, "cel_datePicker", Type.TIME_PICKER, "cel_timePicker", Type.DATE_TIME_PICKER,
      "cel_dateTimePicker");

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addNonEmptyAttribute("type", "text");
    try {
      attrBuilder.addNonEmptyAttribute("value", getStructDataEditorService().getCellValueAsString(
          cellDocRef));
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("type", "text");
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc));
      List<Type> typeList = getFieldValue(cellDocRef, FIELD_TYPE).or(Collections.<Type>emptyList());
      Type pickerType = Iterables.getFirst(typeList, Type.DATE_PICKER);
      attrBuilder.addCssClasses(PICKER_TYPE_CSS_CLASS_MAP.get(pickerType));
      Optional<String> format = getNotEmptyString(cellDocRef, FIELD_FORMAT);
      List<String> dataValueList = new ArrayList<>();
      if (format.isPresent()) {
        String formatStr = new StringBuilder("\"format\" : \"").append(format.get()).append(
            "\"").toString();
        dataValueList.add(formatStr);
      }
      Optional<String> attributes = getNotEmptyString(cellDocRef, FIELD_ATTRIBUTES);
      if (attributes.isPresent()) {
        dataValueList.add(attributes.get());
      }
      String dataAttr = Joiner.on(',').skipNulls().join(dataValueList);
      if (!dataAttr.isEmpty()) {
        attrBuilder.addNonEmptyAttribute("data-pickerAttr", new StringBuilder("{").append(
            dataAttr).append("}").toString());
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
