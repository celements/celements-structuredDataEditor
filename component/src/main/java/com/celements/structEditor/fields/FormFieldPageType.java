package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.FormFieldEditorClass.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(FormFieldPageType.PAGETYPE_NAME)
public class FormFieldPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(FormFieldPageType.class);

  public static final String PAGETYPE_NAME = "FormField";

  static final String VIEW_TEMPLATE_NAME = "FormFieldView";

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
    return Optional.of("form");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("celAddValidationToForm inactive");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      if (modelAccess.getFieldValue(cellDoc, FIELD_SEND_DATA_ENCODED).or(true)) {
        attrBuilder.addNonEmptyAttribute("enctype", "multipart/form-data");
      }
      attrBuilder.addNonEmptyAttribute("action", getVelocityFieldValue(cellDoc, FIELD_ACTION).or(
          "?"));
      attrBuilder.addNonEmptyAttribute("method", modelAccess.getFieldValue(cellDoc,
          FIELD_METHOD).or("post"));
    } catch (DocumentNotExistsException | XWikiVelocityException exc) {
      LOGGER.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
