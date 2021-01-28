package com.celements.structEditor;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.sajson.JsonBuilder;
import com.celements.search.lucene.LuceneSearchResult;

@ComponentRole
public interface SelectAutocompleteRole {

  @NotNull
  String getName();

  @NotNull
  String getJsFilePath();

  @NotNull
  LuceneSearchResult search(@Nullable String searchTerm);

  @NotNull
  Optional<DocumentReference> getSelectedValue(@NotNull DocumentReference selectCellDocRef);

  @NotNull
  JsonBuilder getJsonForValue(@NotNull DocumentReference valueDocRef);

  @NotNull
  String displayNameForValue(@NotNull DocumentReference valueDocRef);

}
