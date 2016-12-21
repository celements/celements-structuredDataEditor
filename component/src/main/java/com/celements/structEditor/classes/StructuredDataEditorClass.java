package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(StructuredDataEditorClass.CLASS_DEF_HINT)
public class StructuredDataEditorClass extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructEditFieldClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> FIELD_EDIT_FIELD_CLASS_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "edit_field_class_fullname").prettyName("Class FullName").build();
  public static final ClassField<String> FIELD_EDIT_FIELD_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "edit_field_name").prettyName("Property Name").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

  @Override
  protected String getClassSpaceName() {
    return SPACE_NAME;
  }

  @Override
  protected String getClassDocName() {
    return DOC_NAME;
  }

}
