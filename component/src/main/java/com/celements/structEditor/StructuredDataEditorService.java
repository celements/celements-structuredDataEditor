package com.celements.structEditor;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;

@ComponentRole
public interface StructuredDataEditorService {

  @NotNull
  public String getPrettyName(@NotNull DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  public DocumentReference getCellClassDocRef(DocumentReference cellDocRef)
      throws DocumentNotExistsException;

}
