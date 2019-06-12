package com.celements.struct;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface StructUtilServiceRole {

  @NotNull
  Optional<XWikiDocument> findParentCell(@NotNull XWikiDocument cellDoc, @Nullable String ptName)
      throws DocumentNotExistsException;

}
