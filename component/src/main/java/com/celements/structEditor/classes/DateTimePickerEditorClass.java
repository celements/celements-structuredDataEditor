package com.celements.structEditor.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.EnumListField;

@Singleton
@Component(DateTimePickerEditorClass.CLASS_DEF_HINT)
public class DateTimePickerEditorClass extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "DateTimePickerEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public enum Type {
    DATE_PICKER, TIME_PICKER, DATE_TIME_PICKER;
  }

  public static final ClassField<List<Type>> FIELD_TYPE = new EnumListField.Builder<>(
      CLASS_DEF_HINT, "datetimepicker_type", Type.class).prettyName("type").multiSelect(
          false).build();

  public static final ClassField<String> FIELD_FORMAT = new StringField.Builder(CLASS_DEF_HINT,
      "datetimepicker_format").prettyName("format").build();

  public static final ClassField<String> FIELD_ATTRIBUTES = new StringField.Builder(CLASS_DEF_HINT,
      "datetimepicker_attributes").prettyName("Picker Attributes").build();

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
