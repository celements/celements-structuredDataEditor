package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TextAreaFieldEditorClass.CLASS_DEF_HINT)
public class TextAreaFieldEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "TextAreaFieldEditorClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final ClassField<Integer> TEXTAREA_ROWS = new IntField.Builder(CLASS_DEF_HINT,
      "textarea_field_rows").prettyName("Rows").build();
  public static final ClassField<Integer> TEXTAREA_COLS = new IntField.Builder(CLASS_DEF_HINT,
      "textarea_field_cols").prettyName("Columns").build();
  public static final ClassField<String> TEXTAREA_VALUE = new LargeStringField.Builder(
      CLASS_DEF_HINT, "textarea_field_value").rows(7).prettyName("Executional Code").build();

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
