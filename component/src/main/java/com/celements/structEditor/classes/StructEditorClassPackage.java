package com.celements.structEditor.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;
import com.celements.struct.classes.StructDataClass;

@Component(StructEditorClassPackage.NAME)
public class StructEditorClassPackage extends AbstractClassPackage {

  static final String NAME = "structEditor";

  @Requirement
  private List<StructDataClass> structDataClassDefs;

  @Requirement
  private List<StructEditorClass> structEditClassDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    List<ClassDefinition> classDefs = new ArrayList<>();
    classDefs.addAll(structDataClassDefs);
    classDefs.addAll(structEditClassDefs);
    return classDefs;
  }

}
