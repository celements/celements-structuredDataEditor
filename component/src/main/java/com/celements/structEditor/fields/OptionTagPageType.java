package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.OptionTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.struct.SelectTagServiceRole;

@Component(OptionTagPageType.PAGETYPE_NAME)
public class OptionTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "OptionTag";

  static final String VIEW_TEMPLATE_NAME = "OptionTagView";

  @Requirement
  private SelectTagServiceRole selectTagService;

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
    return Optional.of("option");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      Optional<DocumentReference> selectCellDocRef = selectTagService
          .getSelectCellDocRef(cellDocRef);
      if (selectCellDocRef.isPresent()) {
        Optional<String> optionValue = modelAccess.getFieldValue(cellDocRef, FIELD_VALUE)
            .toJavaUtil();
        Optional<String> cellValue = getStructDataEditorService().getCellValueAsString(
            selectCellDocRef.get(), modelContext.getCurrentDoc().orNull());
        if ((cellValue.isPresent() && cellValue.equals(optionValue)) || (!cellValue.isPresent()
            && modelAccess.getFieldValue(cellDocRef, FIELD_SELECTED).or(false))) {
          attrBuilder.addEmptyAttribute("selected");
        }
      }
      if (modelAccess.getFieldValue(cellDocRef, FIELD_DISABLED).or(false)) {
        attrBuilder.addEmptyAttribute("disabled");
      }
      attrBuilder.addNonEmptyAttribute("value", modelAccess.getFieldValue(cellDocRef,
          FIELD_VALUE).or(""));
      attrBuilder.addNonEmptyAttribute("label", modelAccess.getFieldValue(cellDocRef,
          FIELD_LABEL).or(""));
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
