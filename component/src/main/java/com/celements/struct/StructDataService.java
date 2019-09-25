package com.celements.struct;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.struct.table.TableConfig;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface StructDataService {

  @NotNull
  WikiReference getCentralWikiRef();

  @NotNull
  Optional<TableConfig> loadTableConfig(@NotNull XWikiDocument doc);

  @NotNull
  Optional<SpaceReference> getStructLayoutSpaceRef(@NotNull XWikiDocument doc);

}
