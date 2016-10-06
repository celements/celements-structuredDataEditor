package com.celements.structEditor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface StructuredDataEditorService {

  @NotNull
  public Optional<String> getAttributeName(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  @NotNull
  public Optional<String> getPrettyName(@NotNull DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  @NotNull
  public Optional<String> getCellValueAsString(@NotNull DocumentReference cellDocRef,
      @NotNull XWikiDocument onDoc) throws DocumentNotExistsException;

  public Optional<DocumentReference> getSelectTagDocumentReference(DocumentReference cellDocRef);

}
