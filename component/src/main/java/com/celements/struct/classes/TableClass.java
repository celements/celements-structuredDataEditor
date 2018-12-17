package com.celements.struct.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.DisplayType;
import com.celements.model.classes.fields.list.StringListField;
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TableClass.CLASS_DEF_HINT)
public class TableClass extends AbstractClassDefinition implements StructDataClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructTableClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> FIELD_QUERY = new LargeStringField.Builder(CLASS_DEF_HINT,
      "query").build();

  public static final ClassField<List<String>> FIELD_SORT_FIELDS = new StringListField.Builder<>(
      CLASS_DEF_HINT, "sort_fields").multiSelect(true).separator(",").displayType(
          DisplayType.input).size(30).build();

  public static final ClassField<Integer> FIELD_RESULT_LIMIT = new IntField.Builder(CLASS_DEF_HINT,
      "result_limit").build();

  public static final ClassField<String> FIELD_CSS_ID = new StringField.Builder(CLASS_DEF_HINT,
      "css_id").build();

  public static final ClassField<List<String>> FIELD_CSS_CLASSES = new StringListField.Builder<>(
      CLASS_DEF_HINT, "css_classes").multiSelect(true).separator(", ").displayType(
          DisplayType.input).size(30).build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
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
