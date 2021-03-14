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
import com.celements.model.classes.fields.StringField;

@Singleton
@Component(OptionTagEditorClass.CLASS_DEF_HINT)
public class OptionTagEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "OptionTagEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_VALUE = new StringField.Builder(
      CLASS_REF, "option_tag_value").prettyName("value").build();

  public static final ClassField<String> FIELD_LABEL = new StringField.Builder(
      CLASS_REF, "option_tag_label").prettyName("label").build();

  public static final ClassField<Boolean> FIELD_SELECTED = new BooleanField.Builder(
      CLASS_REF, "option_tag_is_selected").prettyName("Is Selected").build();

  public static final ClassField<Boolean> FIELD_DISABLED = new BooleanField.Builder(
      CLASS_REF, "option_tag_is_disabled").prettyName("Is Disabled").build();

  public OptionTagEditorClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
