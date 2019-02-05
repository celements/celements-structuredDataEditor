package com.celements.struct;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.struct.table.TableConfig;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface StructDataService {

  @NotNull
  Optional<TableConfig> loadTableConfig(@NotNull XWikiDocument doc);

}
