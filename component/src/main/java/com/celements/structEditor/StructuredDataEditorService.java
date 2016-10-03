package com.celements.structEditor;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface StructuredDataEditorService {

  @NotNull
  public Optional<String> getAttributeName(@NotNull XWikiDocument cellDoc);

  @NotNull
  public Optional<String> getPrettyName(@NotNull DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  @NotNull
  public Optional<DocumentReference> getCellClassDocRef(@NotNull DocumentReference cellDocRef)
      throws DocumentNotExistsException;

}
