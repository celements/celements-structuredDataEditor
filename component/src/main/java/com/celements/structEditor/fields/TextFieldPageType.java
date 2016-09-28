package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

@Component(TextFieldPageType.PAGETYPE_NAME)
public class TextFieldPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "InputField";

  static final String VIEW_TEMPLATE_NAME = "InputFieldView";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
