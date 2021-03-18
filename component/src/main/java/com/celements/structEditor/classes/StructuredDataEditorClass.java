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
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.ref.DocumentReferenceField;

@Singleton
@Component(StructuredDataEditorClass.CLASS_DEF_HINT)
public class StructuredDataEditorClass extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructEditFieldClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<DocumentReference> FIELD_EDIT_FIELD_CLASS = new DocumentReferenceField.Builder(
      CLASS_REF, "edit_field_class_fullname").prettyName("Class Reference").build();

  public static final ClassField<String> FIELD_EDIT_FIELD_NAME = new StringField.Builder(
      CLASS_REF, "edit_field_name").prettyName("Property Name").build();

  public static final ClassField<Boolean> FIELD_MULTILINGUAL = new BooleanField.Builder(
      CLASS_REF, "multilingual").build();

  public static final ClassField<String> FIELD_COMPUTED_OBJ_NB = new LargeStringField.Builder(
      CLASS_REF, "computed_obj_nb").prettyName("Computed Object Number").build();

  public StructuredDataEditorClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
