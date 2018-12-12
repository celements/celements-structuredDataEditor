package com.celements.structEditor;

import java.util.Date;
import java.util.List;

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
  Optional<String> getAttributeName(@NotNull XWikiDocument cellDoc, @Nullable XWikiDocument onDoc);

  @NotNull
  Optional<String> getPrettyName(@NotNull DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  @NotNull
  Optional<PropertyClass> getCellPropertyClass(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<String> getCellValueAsString(@NotNull DocumentReference cellDocRef,
      @NotNull XWikiDocument onDoc) throws DocumentNotExistsException;

  Optional<DocumentReference> getSelectCellDocRef(DocumentReference cellDocRef);

  Optional<String> getDateFormatFromField(DocumentReference cellDocRef)
      throws DocumentNotExistsException;

  @NotNull
  Optional<Date> getCellDateValue(@NotNull DocumentReference cellDocRef,
      @NotNull XWikiDocument onDoc) throws DocumentNotExistsException;

  @NotNull
  List<String> getCellListValue(@NotNull DocumentReference cellDocRef, @NotNull XWikiDocument onDoc)
      throws DocumentNotExistsException;

  @NotNull
  List<String> getSelectTagAutocompleteJsPathList();

  boolean hasEditField(@NotNull XWikiDocument cellDoc);

}
