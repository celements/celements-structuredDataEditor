package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.ref.DocumentReferenceField;

@Singleton
@Component(StructuredDataEditorClass.CLASS_DEF_HINT)
public class StructuredDataEditorClass extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructEditFieldClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<DocumentReference> FIELD_EDIT_FIELD_CLASS = new DocumentReferenceField.Builder(
      CLASS_DEF_HINT, "edit_field_class_fullname").prettyName("Class Reference").build();

  public static final ClassField<String> FIELD_EDIT_FIELD_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "edit_field_name").prettyName("Property Name").build();

  public static final ClassField<String> FIELD_COMPUTED_OBJ_NB = new LargeStringField.Builder(
      CLASS_DEF_HINT, "select_tag_computed_obj_nb").prettyName("Computed Object Number").build();

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
