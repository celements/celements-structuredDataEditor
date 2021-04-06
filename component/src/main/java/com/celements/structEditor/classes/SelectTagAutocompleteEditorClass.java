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

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.ComponentListField;
import com.celements.struct.edit.autocomplete.AutocompleteRole;

@Singleton
@Component(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
public class SelectTagAutocompleteEditorClass extends AbstractClassDefinition
    implements StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "SelectTagAutocompleteEditorClass";
  public static final String CLASS_FN = SPACE_NAME + "." + DOC_NAME;
  public static final String CLASS_DEF_HINT = CLASS_FN;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<String> FIELD_AUTOCOMPLETE_SEPARATOR = new StringField.Builder(
      CLASS_REF, "select_tag_autocomplete_separator").prettyName("Separator").build();

  public static final ClassField<List<AutocompleteRole>> FIELD_AUTOCOMPLETE_TYPE = new ComponentListField.Builder<>(
      CLASS_REF, "select_tag_autocomplete_type", AutocompleteRole.class)
          .multiSelect(false).separator("|").prettyName("Autocomplete Type").build();

  public static final ClassField<Boolean> FIELD_AUTOCOMPLETE_IS_MULTISELECT = new BooleanField.Builder(
      CLASS_REF, "select_tag_autocomplete_is_multiselect").prettyName("Is Multiselect").build();

  /**
   * Velocity interpreted field to render the display name (no HTML) for the search results.
   * The velocity context provides the result reference as "resultDocRef".
   */
  public static final ClassField<String> FIELD_RESULT_NAME = new LargeStringField.Builder(
      CLASS_REF, "result_name").build();

  /**
   * Velocity interpreted field to render the HTML for the search results.
   * The velocity context provides the result reference as "resultDocRef".
   */
  public static final ClassField<String> FIELD_RESULT_HTML = new LargeStringField.Builder(
      CLASS_REF, "result_html").build();

  public SelectTagAutocompleteEditorClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
