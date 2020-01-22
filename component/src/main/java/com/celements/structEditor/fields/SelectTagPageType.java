package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.SelectTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(SelectTagPageType.PAGETYPE_NAME)
public class SelectTagPageType extends AbstractStructFieldPageType {

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
  public Optional<String> tagName() {
    return Optional.of("select");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, modelContext.getCurrentDoc().orNull()).orElse(""));
      if (modelAccess.getFieldValue(cellDoc, FIELD_IS_MULTISELECT).or(false)) {
        attrBuilder.addNonEmptyAttribute("multiple", "multiple");
        attrBuilder.addCssClasses("celMultiselect");
      }
      if (modelAccess.getFieldValue(cellDoc, FIELD_IS_BOOTSTRAP).or(false)) {
        attrBuilder.addCssClasses("celBootstrap");
      }
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
