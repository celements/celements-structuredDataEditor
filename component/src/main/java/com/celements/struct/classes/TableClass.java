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
package com.celements.struct.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.DisplayType;
import com.celements.model.classes.fields.list.StringListField;
import com.celements.model.classes.fields.number.IntField;

@Singleton
@Component(TableClass.CLASS_DEF_HINT)
public class TableClass extends AbstractClassDefinition implements StructDataClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "StructTableClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_QUERY = new LargeStringField.Builder(
      CLASS_REF, "query").build();

  public static final ClassField<List<String>> FIELD_SORT_FIELDS = new StringListField.Builder<>(
      CLASS_REF, "sort_fields").multiSelect(true).separator(",").displayType(DisplayType.input)
          .size(30).build();

  public static final ClassField<Integer> FIELD_RESULT_LIMIT = new IntField.Builder(
      CLASS_REF, "result_limit").build();

  public static final ClassField<String> FIELD_CSS_ID = new StringField.Builder(
      CLASS_REF, "css_id").build();

  public static final ClassField<List<String>> FIELD_CSS_CLASSES = new StringListField.Builder<>(
      CLASS_REF, "css_classes").multiSelect(true).separator(", ").displayType(DisplayType.input)
          .size(30).build();

  public TableClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
