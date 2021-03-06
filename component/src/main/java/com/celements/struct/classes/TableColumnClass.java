package com.celements.struct.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.DisplayType;
import com.celements.model.classes.fields.list.StringListField;
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TableColumnClass.CLASS_DEF_HINT)
public class TableColumnClass extends AbstractClassDefinition implements StructDataClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructTableColumnClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_NAME = new StringField.Builder(CLASS_DEF_HINT,
      "name").build();

  public static final ClassField<String> FIELD_TITLE = new StringField.Builder(CLASS_DEF_HINT,
      "title").build();

  public static final ClassField<String> FIELD_CONTENT = new LargeStringField.Builder(
      CLASS_DEF_HINT, "content").build();

  public static final ClassField<Integer> FIELD_ORDER = new IntField.Builder(CLASS_DEF_HINT,
      "order").build();

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
