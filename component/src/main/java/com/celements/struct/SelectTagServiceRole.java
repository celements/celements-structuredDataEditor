package com.celements.struct;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.structEditor.SelectAutocompleteRole;

@ComponentRole
public interface SelectTagServiceRole {

  @NotNull
  Optional<SelectAutocompleteRole> getTypeImpl(@NotNull DocumentReference cellDocRef);

  /**
   * returns the DocumentReference of the SelectTag parent cell. If the <param>optionDocRef</param>
   * is provided. It must be a descendant of the searched SelectTag docRef.
   *
   * @param optionDocRef
   * @return
   */
  @NotNull
  Optional<DocumentReference> getSelectCellDocRef(@NotNull DocumentReference optionDocRef);

}
