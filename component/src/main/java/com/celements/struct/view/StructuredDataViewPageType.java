package com.celements.struct.view;

import org.xwiki.component.annotation.Component;

import com.celements.struct.StructBasePageType;

@Component(StructuredDataViewPageType.NAME)
public class StructuredDataViewPageType extends StructBasePageType {

  public static final String NAME = "StructuredDataView";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getViewRenderTemplate() {
    return "StructuredDataEditorView";
  }

  @Override
  public String getEditRenderTemplate() {
    return ""; // TODO render table config layout
  }

}
