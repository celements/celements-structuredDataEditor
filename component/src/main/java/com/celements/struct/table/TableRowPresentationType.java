package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.google.common.collect.Lists;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TableRowPresentationType.NAME)
public class TableRowPresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTableRow";

  @Override
  public String getDefaultCssClass() {
    return CSS_CLASS + "_row";
  }

  @Override
  protected List<String> getCssClasses(TableConfig table) {
    return table.getRowCssClasses();
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "";
  }

  @Override
  protected void writeDivContent(StringBuilder outStream, DocumentReference docRef,
      TableConfig table) {
    for (ColumnConfig col : table.getColumns()) {
      writeTableCell(outStream, docRef, col);
    }
    if (table.getColumns().isEmpty()) {
      outStream.append("No columns defined");
    }
  }

  private void writeTableCell(StringBuilder outStream, DocumentReference docRef, ColumnConfig col) {
    try {
      XWikiDocument doc = modelAccess.getDocument(docRef);
      String content = structDataService.evaluateVelocityText(doc, col.getContent());
      List<String> cssClasses = Lists.newArrayList("struct_table_cell");
      cssClasses.addAll(col.getCssClasses());
      outStream.append("<div " + getDataHtml(docRef) + " " + getCssClassHtml(cssClasses) + ">"
          + content + "</div>");
    } catch (DocumentNotExistsException | NoAccessRightsException | XWikiVelocityException exc) {
      LOGGER.warn("writeTableCell - failed for [{}], [{}]", docRef, col, exc);
    }
  }

  private String getDataHtml(DocumentReference docRef) {
    return "data-doc=\"" + modelUtils.serializeRef(docRef, COMPACT_WIKI) + "\"" + ">";
  }

}
