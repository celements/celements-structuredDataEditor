package com.celements.structEditor.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component(StructEditorClassPackage.NAME)
public class StructEditorClassPackage extends AbstractClassPackage {

  static final String NAME = "structEditor";

  @Requirement
  private List<StructEditorClass> classDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDefs);
  }

}
