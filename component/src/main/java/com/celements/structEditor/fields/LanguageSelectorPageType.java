package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

@Component(LanguageSelectorPageType.PAGETYPE_NAME)
public class LanguageSelectorPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "LanguageSelector";

  static final String VIEW_TEMPLATE_NAME = "LanguageSelectorView";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  protected String getEditTemplateName() {
    return "";
  }

}
