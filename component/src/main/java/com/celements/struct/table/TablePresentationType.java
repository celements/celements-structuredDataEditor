package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.web.service.IWebUtilsService;
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
  protected List<String> getCssClasses(TableConfig table) {
    return table.getTableCssClasses();
  }

  @Override
  public String getEmptyDictionaryKey() {
    return CSS_CLASS + "_nodata";
  }

  @Override
  protected void writeDivContent(StringBuilder outStream, DocumentReference docRef,
      TableConfig table) {
    List<DocumentReference> rows = executeTableQuery(table);
    if (rows.isEmpty()) {
      outStream.append(webUtils.getAdminMessageTool().get(getEmptyDictionaryKey()));
    } else {
      writeHeaderRow(outStream, table);
      for (DocumentReference resultDocRef : rows) {
        rowPresentationType.writeNodeContent(outStream, resultDocRef, table);
      }
    }
    outStream.append("</div>");
  }

  private List<DocumentReference> executeTableQuery(TableConfig table) {
    List<DocumentReference> resultList;
    try {
      LuceneSearchResult result = searchService.search(table.getQuery(), table.getSortFields(),
          Arrays.asList(context.getDefaultLanguage()));
      int offset = firstNonNull(Ints.tryParse(context.getRequestParameter("offset").or("")), 0);
      resultList = result.getResults(offset, table.getResultLimit(), DocumentReference.class);
    } catch (LuceneSearchException exc) {
      LOGGER.warn("executeTableQuery - failed for [{}] with result [{}]", table);
      resultList = new ArrayList<>();
    }
    return resultList;
  }

  private void writeHeaderRow(StringBuilder outStream, TableConfig table) {
    LOGGER.debug("writeHeaderRow - for [{}]", table);
    outStream.append("<div " + getCssClassHtml(Arrays.asList(CSS_CLASS + "_header")) + ">");
    for (ColumnConfig col : table.getColumns()) {
      String content;
      try {
        content = structDataService.evaluateVelocityText(col.getTitle());
      } catch (XWikiVelocityException exc) {
        LOGGER.warn("writeHeaderRow - failed for [{}]", col, exc);
        content = col.getTitle();
      }
      outStream.append(content);
    }
    outStream.append("</div>");
  }

}
