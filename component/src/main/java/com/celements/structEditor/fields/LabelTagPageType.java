package com.celements.structEditor.fields;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(LabelTagPageType.PAGETYPE_NAME)
public class LabelTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "LabelTag";

  static final String VIEW_TEMPLATE_NAME = "LabelTagView";

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
    return Optional.of("label");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addCssClasses("structuredDataEditorLabel");
      attrBuilder.addNonEmptyAttribute("for", getStructDataEditorService().getAttributeName(cellDoc,
          modelContext.getCurrentDoc().orNull()).orElse(""));
    } catch (DocumentNotExistsException | IllegalStateException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
