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
package com.celements.struct;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.struct.edit.autocomplete.AutocompleteRole;

@ComponentRole
public interface SelectTagServiceRole {

  @NotNull
  Optional<AutocompleteRole> getTypeImpl(@NotNull DocumentReference cellDocRef);

  /**
   * returns the DocumentReference of the SelectTag parent cell. If the <param>cellDocRef</param>
   * is provided. It must be a descendant of the searched SelectTag docRef.
   *
   * @param optionDocRef
   * @return
   */
  @NotNull
  Optional<DocumentReference> getSelectCellDocRef(@NotNull DocumentReference cellDocRef);

}
