package com.celements.structEditor;

import org.xwiki.component.annotation.Component;

import com.celements.struct.StructBasePageType;

@Component(StructuredDataInlineEditPageType.NAME)
public class StructuredDataInlineEditPageType extends StructBasePageType {

  public static final String NAME = "StructuredDataInlineEdit";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getViewRenderTemplate() {
    return "RichTextView"; // 'default'
  }

  @Override
  public String getEditRenderTemplate() {
    return "StructuredDataEditorCheckUnsavedView";
  }

  @Override
  public boolean useInlineEditorMode() {
    return true;
  }

}
