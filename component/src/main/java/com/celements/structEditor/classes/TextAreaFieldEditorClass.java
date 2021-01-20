package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TextAreaFieldEditorClass.CLASS_DEF_HINT)
public class TextAreaFieldEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "TextAreaFieldEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<Integer> FIELD_ROWS = new IntField.Builder(
      CLASS_REF, "textarea_field_rows").prettyName("Rows").build();

  public static final ClassField<Integer> FIELD_COLS = new IntField.Builder(
      CLASS_REF, "textarea_field_cols").prettyName("Columns").build();

  public static final ClassField<String> FIELD_VALUE = new LargeStringField.Builder(
      CLASS_REF, "textarea_field_value").rows(7).prettyName("Executional Code").build();

  public static final ClassField<Boolean> FIELD_IS_RICHTEXT = new BooleanField.Builder(
      CLASS_REF, "textarea_field_is_richtext").prettyName("Is RichText").displayType("yesno")
          .build();

  public TextAreaFieldEditorClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
