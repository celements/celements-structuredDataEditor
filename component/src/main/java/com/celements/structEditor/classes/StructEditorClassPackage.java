/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
