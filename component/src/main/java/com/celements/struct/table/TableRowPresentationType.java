package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TableRowPresentationType.NAME)
public class TableRowPresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTableRow";

  @Override
  public String getDefaultCssClass() {
    return CSS_CLASS + "_row";
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "";
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef, TableConfig tableCfg) {
    LOGGER.debug("writeNodeContent - for [{}] with [{}]", docRef, tableCfg);
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addAttribute("data-doc", modelUtils.serializeRef(docRef, COMPACT_WIKI));
    writer.openLevel(attributes.build());
    for (ColumnConfig colCfg : tableCfg.getColumns()) {
      writeTableCell(writer, docRef, colCfg);
    }
    if (tableCfg.getColumns().isEmpty()) { // TODO if writer empty!
      writer.appendContent("No columns defined");
    }
    writer.closeLevel();
  }

  private void writeTableCell(ICellWriter writer, DocumentReference docRef, ColumnConfig colCfg) {
    try {
      XWikiDocument doc = modelAccess.getDocument(docRef);
      writer.openLevel(new DefaultAttributeBuilder().addCssClasses(colCfg.getCssClasses()).build());
      String content;
      try {
        content = structDataService.evaluateVelocityText(doc, colCfg.getContent());
      } catch (XWikiVelocityException exc) {
        LOGGER.warn("writeTableCell - failed for [{}]", colCfg, exc);
        content = "illegal macro: " + exc.getMessage();
      }
      writer.appendContent(content);
      writer.closeLevel();
    } catch (DocumentNotExistsException | NoAccessRightsException exc) {
      LOGGER.warn("writeTableCell - failed for [{}], [{}]", docRef, colCfg, exc);
    }
  }

}
