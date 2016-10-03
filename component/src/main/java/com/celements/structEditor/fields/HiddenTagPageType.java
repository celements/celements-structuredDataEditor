package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.HiddenTagEditorClass.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;

@Component(HiddenTagPageType.PAGETYPE_NAME)
public class HiddenTagPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(HiddenTagPageType.class);

  public static final String PAGETYPE_NAME = "HiddenTag";

  static final String VIEW_TEMPLATE_NAME = "HiddenTagView";

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
    attrBuilder.addNonEmptyAttribute("type", "hidden");
    try {
      attrBuilder.addNonEmptyAttribute("name", getNotEmptyString(cellDocRef, FIELD_NAME).or(
          cellDocRef.getName()));
      attrBuilder.addNonEmptyAttribute("value", getNotEmptyString(cellDocRef, FIELD_VALUE).or(""));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
