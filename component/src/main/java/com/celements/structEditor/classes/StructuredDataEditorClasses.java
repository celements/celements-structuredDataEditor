package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(StructuredDataEditorClasses.CLASS_DEF_HINT)
public class StructuredDataEditorClasses extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructEditFieldClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final ClassField<String> FIELD_EDIT_FIELD_CLASS_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "edit_field_class_fullname").build();
  public static final ClassField<String> FIELD_EDIT_FIELD_NAME = new StringField.Builder(
      CLASS_DEF_HINT, "edit_field_name").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

  @Override
  protected EntityReference getRelativeClassRef() {
    return new EntityReference(DOC_NAME, EntityType.DOCUMENT, new EntityReference(SPACE_NAME,
        EntityType.SPACE));
  }

}
