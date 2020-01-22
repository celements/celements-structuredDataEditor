package com.celements.struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.struct.table.TableConfig;
import com.celements.struct.table.TablePresentationType;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("structData")
public class StructDataScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StructDataScriptService.class);

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
      LOGGER.debug("renderTable - [{}]", cellDocRef);
      XWikiDocument doc = modelAccess.getOrCreateDocument(cellDocRef);
      Optional<TableConfig> tableCfg = service.loadTableConfig(doc);
      if (tableCfg.isPresent()) {
        tablePresentationType.writeNodeContent(writer, cellDocRef, tableCfg.get());
      } else {
        writer.openLevel(new DefaultAttributeBuilder().addCssClasses(
            tablePresentationType.getDefaultCssClass()).build());
        writer.appendContent("no valid table config found on: " + cellDocRef);
        writer.closeLevel();
      }
    }
    return writer.getAsString();
  }

  public List<String> getJavaScriptFiles(DocumentReference configDocRef) {
    List<String> jsFiles = new ArrayList<>();
    if (rightsAccess.hasAccessLevel(configDocRef, EAccessLevel.VIEW)) {
      try {
        modelAccess.getXObjects(configDocRef, new ClassReference("JavaScript",
            "ExternalFiles").getDocRef(configDocRef.getWikiReference())).stream()
            .map(xObj -> xObj.getStringValue("filepath"))
            .filter(file -> !file.trim().isEmpty())
            .forEach(jsFiles::add);
      } catch (DocumentNotExistsException exc) {
        LOGGER.info("getLayoutJavaScriptFiles - [{}]", configDocRef, exc);
      }
    }
    return jsFiles;
  }

}
