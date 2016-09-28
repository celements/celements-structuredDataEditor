package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(FormFieldEditorClass.CLASS_DEF_HINT)
public class FormFieldEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "FormFieldEditorClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final ClassField<String> FORM_FIELD_ACTION = new StringField.Builder(CLASS_DEF_HINT,
      "form_field_action").prettyName("Action").build();
  public static final ClassField<String> FORM_FIELD_METHOD = new StringField.Builder(CLASS_DEF_HINT,
      "form_field_method").prettyName("Method").build();
  public static final ClassField<Boolean> FORM_FIELD_SEND_DATA_ENCODED = new BooleanField.Builder(
      CLASS_DEF_HINT, "form_field_send_data_encoded").prettyName("Send Data Encoded").build();
  public static final ClassField<String> FORM_FIELD_PREFIX = new StringField.Builder(CLASS_DEF_HINT,
      "form_field_PREFIX").prettyName("Prefix").build();

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
