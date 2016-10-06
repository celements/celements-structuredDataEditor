package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.SelectTagEditorClass.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(SelectTagPageType.PAGETYPE_NAME)
public class SelectTagPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(SelectTagPageType.class);

  public static final String PAGETYPE_NAME = "SelectTag";

  static final String VIEW_TEMPLATE_NAME = "SelectTagView";

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
    return Optional.of("select");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, modelContext.getDoc()).or(""));
      if (getFieldValue(cellDocRef, FIELD_IS_MULTISELECT).or(false)) {
        attrBuilder.addCssClasses("celMultiselect");
      }
      if (getFieldValue(cellDocRef, FIELD_IS_BOOTSTRAP).or(false)) {
        attrBuilder.addCssClasses("celBootstrap");
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
