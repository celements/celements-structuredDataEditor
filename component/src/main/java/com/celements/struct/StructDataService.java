package com.celements.struct;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.struct.table.TableConfig;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface StructDataService {

  @NotNull
  String evaluateVelocityTextWithContextDoc(@NotNull XWikiDocument contextDoc,
      @Nullable String text) throws NoAccessRightsException, XWikiVelocityException;

  @NotNull
  String evaluateVelocityText(@Nullable String text) throws XWikiVelocityException;

  @NotNull
  Optional<TableConfig> loadTableConfig(@NotNull XWikiDocument doc);

}
