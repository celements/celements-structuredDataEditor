package com.celements.struct.classes;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.ref.SpaceReferenceField;

@Singleton
@Component(StructLayoutClass.CLASS_DEF_HINT)
public class StructLayoutClass extends AbstractClassDefinition implements StructDataClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructLayoutClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<SpaceReference> FIELD_LAYOUT_SPACE = new SpaceReferenceField.Builder(
      CLASS_DEF_HINT, "layoutSpace").build();

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
