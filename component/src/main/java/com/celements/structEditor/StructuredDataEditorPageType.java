package com.celements.structEditor;

import org.xwiki.component.annotation.Component;

import com.celements.struct.StructBasePageType;

@Component(StructuredDataEditorPageType.NAME)
public class StructuredDataEditorPageType extends StructBasePageType {

  public static final String NAME = "StructuredDataEditor";

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
    return "PresentationEdit";
  }

}
