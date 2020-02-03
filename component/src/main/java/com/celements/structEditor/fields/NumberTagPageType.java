package com.celements.structEditor.fields;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(NumberTagPageType.PAGETYPE_NAME)
public class NumberTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "NumberTag";

  static final String VIEW_TEMPLATE_NAME = "NumberTagView";

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
    return Optional.of("input");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addNonEmptyAttribute("type", "number");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      String name = getStructDataEditorService().getAttributeName(cellDoc,
          modelContext.getCurrentDoc().orNull()).orElse("");
      attrBuilder.addNonEmptyAttribute("name", name);
      String value = getStructDataEditorService().getCellValueAsString(cellDocRef,
          modelContext.getCurrentDoc().orNull()).orElse("0");
      attrBuilder.addNonEmptyAttribute("value", value);
    } catch (DocumentNotExistsException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
