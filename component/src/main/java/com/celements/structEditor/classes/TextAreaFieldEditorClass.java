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
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TextAreaFieldEditorClass.CLASS_DEF_HINT)
public class TextAreaFieldEditorClass extends AbstractClassDefinition implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "TextAreaFieldEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<Integer> FIELD_ROWS = new IntField.Builder(
      CLASS_REF, "textarea_field_rows").prettyName("Rows").build();

  public static final ClassField<Integer> FIELD_COLS = new IntField.Builder(
      CLASS_REF, "textarea_field_cols").prettyName("Columns").build();

  public static final ClassField<String> FIELD_VALUE = new LargeStringField.Builder(
      CLASS_REF, "textarea_field_value").rows(7).prettyName("Executional Code").build();

  public static final ClassField<Boolean> FIELD_IS_RICHTEXT = new BooleanField.Builder(
      CLASS_REF, "textarea_field_is_richtext").prettyName("Is RichText").displayType("yesno")
          .build();

  public TextAreaFieldEditorClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
