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

  /**
   * evaluates the given text as velocity script. the velocity context is cloned beforehand, thus
   * variable changes within have a local scope.
   */
  @NotNull
  String evaluateVelocityText(@Nullable String text) throws XWikiVelocityException;

  /**
   * evaluates the given text as velocity script with contextDoc as the '$doc'. the velocity context
   * is cloned beforehand, thus variable changes within have a local scope.
   */
  @NotNull
  String evaluateVelocityTextWithContextDoc(@NotNull XWikiDocument contextDoc,
      @Nullable String text) throws NoAccessRightsException, XWikiVelocityException;

  @NotNull
  Optional<TableConfig> loadTableConfig(@NotNull XWikiDocument doc);

}
