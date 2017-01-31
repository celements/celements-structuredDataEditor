package com.celements.structEditor.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.EnumListField;

@Singleton
@Component(FormFieldEditorClass.CLASS_DEF_HINT)
public class FormFieldEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "FormFieldEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public enum Method {
    POST, GET;
  }

  public static final ClassField<String> FIELD_ACTION = new StringField.Builder(CLASS_DEF_HINT,
      "form_field_action").prettyName("Action").build();
  public static final ClassField<List<Method>> FIELD_METHOD = new EnumListField.Builder<>(
      CLASS_DEF_HINT, "form_field_method", Method.class).prettyName("Method").multiSelect(
          false).build();
  public static final ClassField<Boolean> FIELD_SEND_DATA_ENCODED = new BooleanField.Builder(
      CLASS_DEF_HINT, "form_field_send_data_encoded").prettyName("Send Data Encoded").build();
  public static final ClassField<String> FIELD_PREFIX = new StringField.Builder(CLASS_DEF_HINT,
      "form_field_prefix").prettyName("Prefix").build();

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
