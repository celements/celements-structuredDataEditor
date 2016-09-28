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
@Component(SelectTagEditorClass.CLASS_DEF_HINT)
public class SelectTagEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "SelectTagEditorClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final ClassField<String> SELECT_TAG_SEPARATOR = new StringField.Builder(
      CLASS_DEF_HINT, "select_tag_separator").prettyName("Separator").build();
  public static final ClassField<Boolean> SELECT_TAG_IS_BOOTSTRAP = new BooleanField.Builder(
      CLASS_DEF_HINT, "select_tag_is_bootstrap").prettyName("Is Bootstrap").build();
  public static final ClassField<Boolean> SELECT_TAG_IS_MULTISELECT = new BooleanField.Builder(
      CLASS_DEF_HINT, "select_tag_is_multiselect").prettyName("Is Multiselect").build();

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