package com.celements.structEditor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.celements.search.lucene.LuceneSearchResult;

public interface SelectAutocompleteLuceneRole extends SelectAutocompleteRole {

  @NotNull
  LuceneSearchResult search(@Nullable String searchTerm);

}
