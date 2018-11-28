package com.celements.struct.table;

import org.xwiki.component.annotation.Component;

import com.celements.structEditor.fields.AbstractStructFieldPageType;

@Component(TablePageType.PAGETYPE_NAME)
public class TablePageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "Table";

  static final String VIEW_TEMPLATE_NAME = "TableView";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
