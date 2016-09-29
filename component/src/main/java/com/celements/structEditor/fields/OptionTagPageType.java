package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.OptionTagEditorClass.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

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
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      if (modelAccess.getProperty(cellDoc, FIELD_SELECTED)) {
        attrBuilder.addEmptyAttribute("selected");
      }
      if (modelAccess.getProperty(cellDoc, FIELD_DISABLED)) {
        attrBuilder.addEmptyAttribute("disabled");
      }
      attrBuilder.addNonEmptyAttribute("value", modelAccess.getProperty(cellDoc, FIELD_VALUE));
      attrBuilder.addNonEmptyAttribute("label", modelAccess.getProperty(cellDoc, FIELD_LABEL));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
