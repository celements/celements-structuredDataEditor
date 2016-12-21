package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

@Component(TextAreaFieldPageType.INPUT_FIELD_PAGETYPE_NAME)
public class TextAreaFieldPageType extends AbstractStructFieldPageType {

  public static final String INPUT_FIELD_PAGETYPE_NAME = "TextAreaField";

  static final String VIEW_TEMPLATE_NAME = "TextAreaFieldView";

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
