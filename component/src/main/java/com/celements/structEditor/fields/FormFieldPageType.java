package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

import com.google.common.base.Optional;

@Component(FormFieldPageType.FORM_FIELD_PAGETYPE_NAME)
public class FormFieldPageType extends AbstractStructFieldPageType {

  public static final String FORM_FIELD_PAGETYPE_NAME = "FormField";

  static final String VIEW_TEMPLATE_NAME = "FormFieldView";

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

}
