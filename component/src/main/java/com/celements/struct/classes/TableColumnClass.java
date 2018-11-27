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
@Component(TableColumnClass.CLASS_DEF_HINT)
public class TableColumnClass extends AbstractClassDefinition implements StructDataClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructTableColumnClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> FIELD_TITLE = new StringField.Builder(CLASS_DEF_HINT,
      "title").build();

  public static final ClassField<String> FIELD_CONTENT = new StringField.Builder(CLASS_DEF_HINT,
      "content").build();

  public static final ClassField<Integer> FIELD_ORDER = new IntField.Builder(CLASS_DEF_HINT,
      "order").build();

  public static final ClassField<List<String>> FIELD_CSS_CLASSES = new StringListField.Builder<>(
      CLASS_DEF_HINT, "css_classes").multiSelect(true).separator(",").build();

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
