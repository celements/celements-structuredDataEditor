package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

@Component(TablePresentationType.NAME)
public class TablePresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTable";

  @Requirement(TableRowPresentationType.NAME)
  private IPresentationTypeRole<TableConfig> rowPresentationType;

  @Requirement
  private ILuceneSearchService searchService;

  @Override
  public String getDefaultCssClass() {
    return CSS_CLASS;
  }

  @Override
  public String getEmptyDictionaryKey() {
    return CSS_CLASS + "_nodata";
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    LOGGER.info("writeNodeContent - for [{}] with [{}]", tableDocRef, tableCfg);
    AttributeBuilder attributes = newAttributeBuilder();
    attributes.addId(tableCfg.getCssId());
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addCssClasses(tableCfg.getCssClasses());
    writer.openLevel(attributes.build());
    writeTableContent(writer, tableDocRef, tableCfg);
    writer.closeLevel();
  }

  private void writeTableContent(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    try {
      writeHeader(writer, tableDocRef, tableCfg);
      writer.openLevel(newAttributeBuilder().addCssClasses(CSS_CLASS + "_scroll").build());
      writer.openLevel("ul", newAttributeBuilder().addCssClasses(CSS_CLASS + "_data").build());
      List<DocumentReference> rows = executeTableQuery(tableCfg);
      if (!rows.isEmpty()) {
        rows.forEach(resultDocRef -> rowPresentationType.writeNodeContent(writer, resultDocRef, tableCfg));
      } else {
        writeEmptyRow(writer, tableDocRef);
      }
      writer.closeLevel(); // ul
      writer.closeLevel(); // div
    } catch (XWikiVelocityException | LuceneSearchException exc) {
      LOGGER.warn("writeTableContent - failed for [{}]", tableCfg, exc);
      writer.appendContent("search failed: " + exc.getMessage());
    }
  }

  private List<DocumentReference> executeTableQuery(TableConfig tableCfg)
      throws XWikiVelocityException, LuceneSearchException {
    int offset = firstNonNull(Ints.tryParse(context.getRequestParameter("offset").or("")), 0);
    String query = velocityService.evaluateVelocityText(tableCfg.getQuery());
    LuceneSearchResult result = searchService.search(query, tableCfg.getSortFields(), ImmutableList.of());
    LOGGER.debug("executeTableQuery - [{}]", result);
    return result.getResults(offset, tableCfg.getResultLimit(), DocumentReference.class);
  }

  private void writeHeader(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    LOGGER.debug("writeHeader - for [{}]", tableCfg);
    writer.openLevel("ul", newAttributeBuilder().addCssClasses(CSS_CLASS + "_header").build());
    tableCfg.setHeaderMode(true);
    rowPresentationType.writeNodeContent(writer, tableDocRef, tableCfg);
    tableCfg.setHeaderMode(false);
    writer.closeLevel();
  }

  private void writeEmptyRow(ICellWriter writer, DocumentReference tableDocRef) {
    TableConfig emptyTableCfg = new TableConfig();
    ColumnConfig emptyColCfg = new ColumnConfig();
    emptyColCfg.setName("empty");
    emptyColCfg.setCssClasses(ImmutableList.of("row_span"));
    emptyColCfg.setContent(webUtils.getAdminMessageTool().get(getEmptyDictionaryKey()));
    emptyTableCfg.setColumns(ImmutableList.of(emptyColCfg));
    rowPresentationType.writeNodeContent(writer, tableDocRef, emptyTableCfg);
  }

}
