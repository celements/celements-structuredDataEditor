package com.celements.structEditor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.search.lucene.LuceneSearchResult;

@ComponentRole
public interface SelectAutocompleteLuceneRole extends SelectAutocompleteRole {

  @NotNull
  LuceneSearchResult search(@Nullable String searchTerm);

}
