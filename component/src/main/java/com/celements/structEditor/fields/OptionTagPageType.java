package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

@Component(OptionTagPageType.INPUT_FIELD_PAGETYPE_NAME)
public class OptionTagPageType extends AbstractStructFieldPageType {

  public static final String INPUT_FIELD_PAGETYPE_NAME = "OptionTag";

  static final String VIEW_TEMPLATE_NAME = "OptionTagView";

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
