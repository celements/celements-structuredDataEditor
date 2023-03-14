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
package com.celements.struct.edit.autocomplete;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.sajson.JsonBuilder;
import com.celements.search.lucene.LuceneSearchResult;

@ComponentRole
public interface AutocompleteRole {

  @NotNull
  String getName();

  @NotNull
  String getJsFilePath();

  @NotNull
  LuceneSearchResult search(@Nullable DocumentReference cellDocRef, @Nullable String searchTerm);

  @NotNull
  Optional<DocumentReference> getSelectedValue(@Nullable DocumentReference cellDocRef);

  /**
   * The returned JSON is used to link and display results client side. It must contain the
   * properties 'fullName' (=id) and 'name' (=pretty name, normally {@link #displayNameForValue}).
   * Additionally, the property 'html' may be provided to be displayed in the detail result listing.
   */
  @NotNull
  JsonBuilder getJsonForValue(@Nullable DocumentReference onDocRef,
      @Nullable DocumentReference cellDocRef);

  @NotNull
  String displayNameForValue(@Nullable DocumentReference onDocRef,
      @Nullable DocumentReference cellDocRef);

  @NotNull
  String getAddNewUrl();

}
