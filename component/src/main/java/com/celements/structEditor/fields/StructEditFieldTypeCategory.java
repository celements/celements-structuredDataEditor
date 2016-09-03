package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

import com.celements.pagetype.category.AbstractPageTypeCategory;

@Component(StructEditFieldTypeCategory.STRUCT_EDIT_FIELD_TYPE_CATEGORY)
public class StructEditFieldTypeCategory extends AbstractPageTypeCategory {

  public static final String STRUCT_EDIT_FIELD_TYPE_CATEGORY = "structEditFieldTypeCategory";

  @Override
  public String getTypeName() {
    return "structfieldtype";
  }

}
