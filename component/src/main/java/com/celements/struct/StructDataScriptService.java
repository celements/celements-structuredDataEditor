package com.celements.struct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.struct.table.TableConfig;
import com.celements.struct.table.TablePresentationType;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("structData")
public class StructDataScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(StructDataScriptService.class);

  @Requirement
  private StructDataService service;

  @Requirement(TablePresentationType.NAME)
  private IPresentationTypeRole<TableConfig> tablePresentationType;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  public String renderTable(DocumentReference cellDocRef) {
    ICellWriter writer = new DivWriter();
    if (rightsAccess.hasAccessLevel(cellDocRef, EAccessLevel.VIEW)) {
      XWikiDocument doc = modelAccess.getOrCreateDocument(cellDocRef);
      Optional<TableConfig> tableCfg = service.loadTableConfig(doc);
      if (tableCfg.isPresent()) {
        tablePresentationType.writeNodeContent(writer, context.getDoc().getDocumentReference(),
            tableCfg.get());
      } else {
        LOGGER.info("renderTable - no table config found on [{}]", cellDocRef);
      }
    }
    return writer.getAsString();
  }

}
