package com.celements.structEditor;

import java.util.Date;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.PropertyClass;

@ComponentRole
public interface StructuredDataEditorService {

  @NotNull
  public Optional<String> getAttributeName(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  @NotNull
  public Optional<String> getPrettyName(@NotNull DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  @NotNull
  public Optional<PropertyClass> getCellPropertyClass(@NotNull XWikiDocument cellDoc);

  @NotNull
  public Optional<String> getCellValueAsString(@NotNull DocumentReference cellDocRef,
      @NotNull XWikiDocument onDoc) throws DocumentNotExistsException;

  public Optional<DocumentReference> getSelectCellDocRef(DocumentReference cellDocRef);

  public Optional<String> getDateFormatFromField(DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  public Optional<Date> getCellDateValue(DocumentReference cellDocRef, XWikiDocument onDoc)
      throws DocumentNotExistsException;

}
