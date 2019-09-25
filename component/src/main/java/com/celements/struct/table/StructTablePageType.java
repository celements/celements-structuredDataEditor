package com.celements.struct.table;

import org.xwiki.component.annotation.Component;

import com.celements.structEditor.StructuredDataEditorPageType;

@Component(StructTablePageType.NAME)
public class StructTablePageType extends StructuredDataEditorPageType {

  public static final String NAME = "StructTable";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    if ("edit".equals(renderMode)) {
      return ""; // TOOD render edit layout if have access
    } else {
      return VIEW_TEMPLATE_NAME;
    }
  }

}
