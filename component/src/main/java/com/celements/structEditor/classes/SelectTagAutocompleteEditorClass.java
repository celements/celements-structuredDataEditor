package com.celements.structEditor.classes;

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.EnumListField;

@Singleton
@Component(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
public class SelectTagAutocompleteEditorClass extends AbstractClassDefinition implements
    StructEditorClass {

  public static final String SPACE_NAME = "Celements";
  public static final String DOC_NAME = "SelectTagAutocompleteEditorClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public enum AutocompleteType {
    place;
  }

  public static final ClassField<String> FIELD_SEPARATOR = new StringField.Builder(CLASS_DEF_HINT,
      "select_tag_autocomplete_separator").prettyName("Separator").build();

  public static final ClassField<List<AutocompleteType>> FIELD_AUTOCOMPLETE_TYPE = new EnumListField.Builder<>(
      CLASS_DEF_HINT, "select_tag_autocomplete_type", AutocompleteType.class).prettyName(
          "Autocomplete Type").separator("|").multiSelect(false).displayType("select").build();

  public static final ClassField<Boolean> FIELD_AUTOCOMPLETE_IS_MULTISELECT = new BooleanField.Builder(
      CLASS_DEF_HINT, "select_tag_autocomplete_is_multiselect").prettyName(
          "Is Multiselect").build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return true;
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
