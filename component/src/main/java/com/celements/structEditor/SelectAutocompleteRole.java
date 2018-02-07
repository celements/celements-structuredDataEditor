package com.celements.structEditor;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface SelectAutocompleteRole {

  public @NotNull String getName();

  public @NotNull String getCssClass();

  public @NotNull String getJsFilePath();
}
