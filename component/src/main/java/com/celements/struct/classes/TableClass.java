package com.celements.struct.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.StringListField;
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TableClass.CLASS_DEF_HINT)
public class TableClass extends AbstractClassDefinition implements StructDataClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructTableClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> FIELD_QUERY = new StringField.Builder(CLASS_DEF_HINT,
      "query").build();

  public static final ClassField<List<String>> FIELD_SORT_FIELDS = new StringListField.Builder<>(
      CLASS_DEF_HINT, "sort_fields").multiSelect(true).separator(",").build();

  public static final ClassField<Integer> FIELD_RESULT_LIMIT = new IntField.Builder(CLASS_DEF_HINT,
      "result_limit").build();

  public static final ClassField<List<String>> FIELD_TABLE_CSS_CLASSES = new StringListField.Builder<>(
      CLASS_DEF_HINT, "table_css_classes").multiSelect(true).separator(",").build();

  public static final ClassField<List<String>> FIELD_ROW_CSS_CLASSES = new StringListField.Builder<>(
      CLASS_DEF_HINT, "row_css_classes").multiSelect(true).separator(",").build();

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
