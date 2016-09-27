package com.celements.structEditor.classes;

import java.util.Arrays;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.StaticListField;

@Singleton
@Component(DateTimePickerEditorClass.CLASS_DEF_HINT)
public class DateTimePickerEditorClass extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "DateTimePickerEditorClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final StaticListField DATETIMEPICKER_TYPE = new StaticListField.Builder(
      CLASS_DEF_HINT, "datetimepicker_type").prettyName("type").values(Arrays.asList("Date Picker",
          "Date Time Picker", "Time Picker")).build();
  public static final ClassField<String> DATETIMEPICKER_FORMAT = new StringField.Builder(
      CLASS_DEF_HINT, "datetimepicker_format").prettyName("format").build();
  public static final ClassField<String> DATETIMEPICKER_ATTRIBUTES = new StringField.Builder(
      CLASS_DEF_HINT, "datetimepicker_attributes").prettyName("Picker Attributes").build();

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
