package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.DateTimePickerEditorClass.*;

import java.util.Arrays;
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
import com.google.common.base.Strings;
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
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("type", "text");
      addNameAttribute(attrBuilder, cellDoc);
      Type pickerType = Iterables.getFirst(modelAccess.getProperty(cellDoc, FIELD_TYPE), null);
      attrBuilder.addCssClasses(PICKER_TYPE_CSS_CLASS_MAP.get(pickerType));
      String attributes = Strings.emptyToNull(modelAccess.getProperty(cellDoc, FIELD_ATTRIBUTES));
      String format = Strings.emptyToNull(modelAccess.getProperty(cellDoc, FIELD_FORMAT));
      if (format != null) {
        format = new StringBuilder("\"format\" : \"").append(format).append("\"").toString();
      }
      String dataAttr = Joiner.on(',').skipNulls().join(Arrays.asList(format, attributes));
      if (!dataAttr.isEmpty()) {
        attrBuilder.addNonEmptyAttribute("data-pickerAttr", new StringBuilder("{").append(
            dataAttr).append("}").toString());
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
