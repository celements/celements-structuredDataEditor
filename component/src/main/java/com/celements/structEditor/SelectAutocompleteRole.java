package com.celements.structEditor;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.sajson.JsonBuilder;

@ComponentRole
public interface SelectAutocompleteRole {

  @NotNull
  String getName();

  @NotNull
  String getCssClass();

  @NotNull
  String getJsFilePath();

  @NotNull
  Optional<DocumentReference> getSelectedValue(@NotNull DocumentReference selectCellDocRef);

  @NotNull
  JsonBuilder getJsonForValue(@NotNull DocumentReference valueDocRef);

}
