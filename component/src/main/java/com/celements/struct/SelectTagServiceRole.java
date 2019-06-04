package com.celements.struct;

import java.util.Optional;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.structEditor.SelectAutocompleteRole;

@ComponentRole
public interface SelectTagServiceRole {

  Optional<SelectAutocompleteRole> getTypeImpl(DocumentReference cellDocRef);

}
