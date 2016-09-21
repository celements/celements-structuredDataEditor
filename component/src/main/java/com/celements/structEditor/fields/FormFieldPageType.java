package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.context.ModelContext;
import com.google.common.base.Optional;

@Component(FormFieldPageType.FORM_FIELD_PAGETYPE_NAME)
public class FormFieldPageType extends AbstractStructFieldPageType {

  public static final String FORM_FIELD_PAGETYPE_NAME = "FormField";

  static final String VIEW_TEMPLATE_NAME = "FormFieldView";

  @Requirement
  ModelContext modelContext;

  @Override
  public String getName() {
    return FORM_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.of("form");
  }

  @Override
  public void getAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addNonEmptyAttribute("method", "post");
    attrBuilder.addNonEmptyAttribute("action", "?");
  }

}
