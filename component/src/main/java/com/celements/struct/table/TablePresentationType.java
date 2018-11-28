package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

@Component(TablePresentationType.NAME)
public class TablePresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTable";

  @Requirement(TableRowPresentationType.NAME)
  private IPresentationTypeRole<TableConfig> rowPresentationType;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private IWebUtilsService webUtils;

  @Override
  public String getDefaultCssClass() {
    return CSS_CLASS;
  }

  @Override
  public String getEmptyDictionaryKey() {
    return CSS_CLASS + "_nodata";
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef, TableConfig tableCfg) {
    LOGGER.debug("writeNodeContent - for [{}] with [{}]", docRef, tableCfg);
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    attributes.addId(tableCfg.getCssId());
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addCssClasses(tableCfg.getCssClasses());
    writer.openLevel(attributes.build());
    writeTableContent(writer, tableCfg);
    writer.closeLevel();
  }

  private void writeTableContent(ICellWriter writer, TableConfig tableCfg) {
    try {
      List<DocumentReference> rows = executeTableQuery(tableCfg);
      if (!rows.isEmpty()) {
        writeHeaderRow(writer, tableCfg);
        for (DocumentReference resultDocRef : rows) {
          rowPresentationType.writeNodeContent(writer, resultDocRef, tableCfg);
        }
      } else {
        writer.appendContent(webUtils.getAdminMessageTool().get(getEmptyDictionaryKey()));
      }
    } catch (LuceneSearchException exc) {
      LOGGER.warn("writeTableContent - failed for [{}]", tableCfg, exc);
      writer.appendContent("search failed: " + exc.getMessage());
    }
  }

  private List<DocumentReference> executeTableQuery(TableConfig tableCfg)
      throws LuceneSearchException {
    int offset = firstNonNull(Ints.tryParse(context.getRequestParameter("offset").or("")), 0);
    LuceneSearchResult result = searchService.search(tableCfg.getQuery(), tableCfg.getSortFields(),
        ImmutableList.<String>of());
    return result.getResults(offset, tableCfg.getResultLimit(), DocumentReference.class);
  }

  private void writeHeaderRow(ICellWriter writer, TableConfig tableCfg) {
    LOGGER.debug("writeHeaderRow - for [{}]", tableCfg);
    writer.openLevel(new DefaultAttributeBuilder().addCssClasses(CSS_CLASS + "_header").build());
    for (ColumnConfig colCfg : tableCfg.getColumns()) {
      writer.openLevel(new DefaultAttributeBuilder().addCssClasses(colCfg.getCssClasses()).build());
      String title;
      try {
        title = structDataService.evaluateVelocityText(colCfg.getTitle());
      } catch (XWikiVelocityException exc) {
        LOGGER.warn("writeHeaderRow - failed for [{}]", colCfg, exc);
        title = "illegal macro: " + exc.getMessage();
      }
      writer.appendContent(title);
      writer.closeLevel();
    }
    writer.closeLevel();
  }

}
