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

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(SelectTagEditorClass.CLASS_DEF_HINT)
public class SelectTagEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "SelectTagEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_SEPARATOR = new StringField.Builder(
      CLASS_REF, "select_tag_separator").prettyName("Separator").build();

  public static final ClassField<Boolean> FIELD_IS_BOOTSTRAP = new BooleanField.Builder(
      CLASS_REF, "select_tag_is_bootstrap").prettyName("Is Bootstrap").build();

  public static final ClassField<Boolean> FIELD_IS_MULTISELECT = new BooleanField.Builder(
      CLASS_REF, "select_tag_is_multiselect").prettyName("Is Multiselect").build();

  public static final ClassField<String> FIELD_BOOTSTRAP_CONFIG = new LargeStringField.Builder(
      CLASS_REF, "select_tag_bootstrap_config").prettyName("Bootstrap Config JSON").build();

  public SelectTagEditorClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
