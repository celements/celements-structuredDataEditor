package com.celements.structEditor.fields;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.classes.CellClass;
import com.celements.model.object.xwiki.XWikiObjectFetcher;

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
    attrBuilder.addCssClasses("structuredDataEditorLabel");
    XWikiObjectFetcher.on(modelAccess.getOrCreateDocument(cellDocRef))
        .fetchField(CellClass.FIELD_ID_NAME)
        .stream().findFirst().ifPresent(id -> {
          attrBuilder.addId(id + "_Label");
          attrBuilder.addNonEmptyAttribute("for", id);
        });
  }

}
