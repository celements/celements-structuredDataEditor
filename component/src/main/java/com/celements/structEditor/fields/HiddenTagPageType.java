package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.HiddenTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(HiddenTagPageType.PAGETYPE_NAME)
public class HiddenTagPageType extends AbstractStructFieldPageType {

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
  public Optional<String> tagName() {
    return Optional.of("input");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addNonEmptyAttribute("type", "hidden");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      String value = "";
      String name = "";
      if (getStructDataEditorService().hasEditField(cellDoc)) {
        name = getStructDataEditorService().getAttributeName(cellDoc,
            modelContext.getCurrentDoc().orNull()).orElse("");
        value = getStructDataEditorService().getCellValueAsString(cellDocRef,
            modelContext.getCurrentDoc().orNull()).orElse("");
      } else {
        value = getVelocityFieldValue(cellDoc, FIELD_VALUE).orElse("");
        name = modelAccess.getFieldValue(cellDoc, FIELD_NAME).or(cellDocRef.getName());
      }
      attrBuilder.addNonEmptyAttribute("name", name);
      attrBuilder.addNonEmptyAttribute("value", value);
    } catch (DocumentNotExistsException | XWikiVelocityException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
