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
      Optional<String> name = modelAccess.getFieldValue(cellDoc, FIELD_NAME).toJavaUtil();
      Optional<String> value = getVelocityFieldValue(cellDoc, FIELD_VALUE);
      if (getStructDataEditorService().hasEditField(cellDoc)) {
        XWikiDocument onDoc = modelContext.getCurrentDoc().orNull();
        if (!name.isPresent()) {
          name = getStructDataEditorService().getAttributeName(cellDoc, onDoc);
        }
        if (!value.isPresent()) {
          value = getStructDataEditorService().getCellValueAsString(cellDocRef, onDoc);
        }
      }
      attrBuilder.addNonEmptyAttribute("name", name.orElse(cellDocRef.getName()));
      attrBuilder.addNonEmptyAttribute("value", value.orElse(""));
    } catch (DocumentNotExistsException | XWikiVelocityException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
