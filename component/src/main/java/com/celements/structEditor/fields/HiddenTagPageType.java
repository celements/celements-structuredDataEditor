package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

@Component(HiddenTagPageType.INPUT_FIELD_PAGETYPE_NAME)
public class HiddenTagPageType extends AbstractStructFieldPageType {

  public static final String INPUT_FIELD_PAGETYPE_NAME = "HiddenTag";

  static final String VIEW_TEMPLATE_NAME = "HiddenTagView";

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
