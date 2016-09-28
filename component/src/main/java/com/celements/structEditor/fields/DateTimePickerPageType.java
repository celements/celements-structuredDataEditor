package com.celements.structEditor.fields;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.DateTimePickerEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.objects.BaseObject;

@Component(DateTimePickerPageType.PAGETYPE_NAME)
public class DateTimePickerPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(DateTimePickerPageType.class);

  public static final String PAGETYPE_NAME = "DateTimePicker";

  static final String VIEW_TEMPLATE_NAME = "DateTimePickerView";

  @Requirement(DateTimePickerEditorClass.CLASS_DEF_HINT)
  private StructEditorClass dateTimePickerClass;

  @Requirement(StructuredDataEditorClass.CLASS_DEF_HINT)
  private StructEditorClass structuredDataEditorClasses;

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

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    BaseObject dateTimePickerConfig;
    DocumentReference structuredDataEditorClasseRef = structuredDataEditorClasses.getClassRef(
        cellDocRef.getWikiReference());
    DocumentReference hiddenClassRef = dateTimePickerClass.getClassRef(
        cellDocRef.getWikiReference());
    try {
      dateTimePickerConfig = modelAccess.getXObject(cellDocRef, hiddenClassRef);
      attrBuilder.addNonEmptyAttribute("type", "text");
      String pickerType = dateTimePickerConfig.getStringValue("datetimepicker_type");
      if (Objects.equals(pickerType, "Date Time Picker")) {
        attrBuilder.addCssClasses("cel_dateTimePicker");
      } else if (Objects.equals(pickerType, "Time Picker")) {
        attrBuilder.addCssClasses("cel_timePicker");
      } else {
        attrBuilder.addCssClasses("cel_datePicker");
      }
      String dataAttributes = new String();
      if (!Strings.isNullOrEmpty(dateTimePickerConfig.getStringValue("datetimepicker_format"))) {
        dataAttributes += "{\"format\" : \"" + dateTimePickerConfig.getStringValue(
            "datetimepicker_format") + "\"";
      }
      if (!Strings.isNullOrEmpty(dateTimePickerConfig.getStringValue(
          "datetimepicker_attributes"))) {
        if (Strings.isNullOrEmpty(dataAttributes)) {
          dataAttributes += "{";
        } else {
          dataAttributes += ",";
        }
        dataAttributes += dateTimePickerConfig.getStringValue("datetimepicker_attributes");
      }
      if (!Strings.isNullOrEmpty(dataAttributes)) {
        dataAttributes += "}";
        attrBuilder.addNonEmptyAttribute("data-pickerAttr", dataAttributes);
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", structuredDataEditorClasseRef,
          hiddenClassRef, exc);
    }
  }

}
