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
@Component(OptionTagEditorClass.CLASS_DEF_HINT)
public class OptionTagEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "OptionTagEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static final ClassField<String> FIELD_VALUE = new StringField.Builder(CLASS_DEF_HINT,
      "option_tag_value").prettyName("value").build();
  public static final ClassField<String> FIELD_LABEL = new StringField.Builder(CLASS_DEF_HINT,
      "option_tag_label").prettyName("label").build();
  public static final ClassField<Boolean> FIELD_SELECTED = new BooleanField.Builder(CLASS_DEF_HINT,
      "option_tag_is_selected").prettyName("Is Selected").build();
  public static final ClassField<Boolean> FIELD_DISABLED = new BooleanField.Builder(CLASS_DEF_HINT,
      "option_tag_is_disabled").prettyName("Is Disabled").build();

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
