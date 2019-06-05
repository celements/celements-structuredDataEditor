package com.celements.structEditor;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface SelectAutocompleteRole {

  public @NotNull String getName();

  public @NotNull String getCssClass();

  public @NotNull String getJsFilePath();

  public @NotNull Optional<String> getSelectedValue(DocumentReference selectCellDocRef);

}
