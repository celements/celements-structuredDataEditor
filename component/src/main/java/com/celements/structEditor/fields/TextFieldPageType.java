package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TextFieldPageType.PAGETYPE_NAME)
public class TextFieldPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(TextFieldPageType.class);

  public static final String PAGETYPE_NAME = "InputField";

  static final String VIEW_TEMPLATE_NAME = "InputFieldView";

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
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("type", "text");
      addNameAttribute(attrBuilder, cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
