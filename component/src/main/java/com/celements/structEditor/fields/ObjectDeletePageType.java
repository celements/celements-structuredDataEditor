package com.celements.structEditor.fields;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;

@Component(ObjectDeletePageType.PAGETYPE_NAME)
public class ObjectDeletePageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "ObjectDelete";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return PAGETYPE_NAME + "View";
  }

  @Override
  public Optional<String> tagName() {
    return Optional.of("a");
  }

  @Override
  public void collectAttributes(final AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("struct_object_delete");
    attrBuilder.addNonEmptyAttribute("href", "#");
  }

}
