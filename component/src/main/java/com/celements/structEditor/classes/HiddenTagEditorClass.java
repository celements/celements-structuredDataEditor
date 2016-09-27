package com.celements.structEditor.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(HiddenTagEditorClass.CLASS_DEF_HINT)
public class HiddenTagEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "HiddenTagEditorClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;

  public static final ClassField<String> HIDDEN_TAG_NAME = new StringField.Builder(CLASS_DEF_HINT,
      "hidden_tag_name").prettyName("name").build();
  public static final ClassField<String> HIDDEN_TAG_VALUE = new StringField.Builder(CLASS_DEF_HINT,
      "hidden_tag_value").prettyName("value").build();

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
