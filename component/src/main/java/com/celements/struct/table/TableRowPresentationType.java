package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
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
    LOGGER.info("writeNodeContent - for [{}] with [{}]", docRef, tableCfg);
    AttributeBuilder attributes = newAttributeBuilder();
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addAttribute("data-ref", modelUtils.serializeRef(docRef, COMPACT_WIKI));
    writer.openLevel("li", attributes.build());
    for (ColumnConfig colCfg : tableCfg.getColumns()) {
      writeTableCell(writer, docRef, colCfg);
    }
    if (tableCfg.getColumns().isEmpty()) {
      writer.appendContent("no columns defined");
    }
    writer.closeLevel();
  }

  private void writeTableCell(ICellWriter writer, DocumentReference docRef, ColumnConfig colCfg) {
    try {
      XWikiDocument doc = modelAccess.getDocument(docRef);
      AttributeBuilder attributes = newAttributeBuilder();
      attributes.addCssClasses(CSS_CLASS + "_cell_" + colCfg.getNumber());
      attributes.addCssClasses(colCfg.getCssClasses());
      writer.openLevel(attributes.build());
      String content;
      try {
        content = structDataService.evaluateVelocityTextWithContextDoc(doc, colCfg.getContent());
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
