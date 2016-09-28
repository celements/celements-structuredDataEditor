package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;

@Component(TextAreaFieldPageType.INPUT_FIELD_PAGETYPE_NAME)
public class TextAreaFieldPageType extends AbstractStructFieldPageType {

  public static final String INPUT_FIELD_PAGETYPE_NAME = "TextAreaField";

  static final String VIEW_TEMPLATE_NAME = "TextAreaFieldView";

  @Requirement(TextAreaFieldEditorClass.CLASS_DEF_HINT)
  private StructEditorClass textAreaFieldEditorClass;

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
