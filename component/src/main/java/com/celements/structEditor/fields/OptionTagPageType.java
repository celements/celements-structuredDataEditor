package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.OptionTagEditorClass.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;

@Component(OptionTagPageType.PAGETYPE_NAME)
public class OptionTagPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(OptionTagPageType.class);

  public static final String PAGETYPE_NAME = "OptionTag";

  static final String VIEW_TEMPLATE_NAME = "OptionTagView";

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
    return Optional.of("option");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      Optional<DocumentReference> selectCellDocRef = getStructDataEditorService().getSelectCellDocRef(
          cellDocRef);
      if (selectCellDocRef.isPresent()) {
        Optional<String> optionValue = getNotEmptyString(cellDocRef, FIELD_VALUE);
        Optional<String> cellValue = getStructDataEditorService().getCellValueAsString(
            selectCellDocRef.get(), modelContext.getDoc());
        if (cellValue.isPresent() && optionValue.isPresent() && cellValue.get().equals(
            optionValue.get())) {
          attrBuilder.addEmptyAttribute("selected");
        } else if (!cellValue.isPresent() && getFieldValue(cellDocRef, FIELD_SELECTED).or(false)) {
          attrBuilder.addEmptyAttribute("selected");
        }
      }
      if (getFieldValue(cellDocRef, FIELD_DISABLED).or(false)) {
        attrBuilder.addEmptyAttribute("disabled");
      }
      attrBuilder.addNonEmptyAttribute("value", getNotEmptyString(cellDocRef, FIELD_VALUE).or(""));
      attrBuilder.addNonEmptyAttribute("label", getNotEmptyString(cellDocRef, FIELD_LABEL).or(""));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}