/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

@Component(TableDocPresentationType.NAME)
public class TableDocPresentationType extends TablePresentationType {

  public static final String NAME = AbstractTablePresentationType.NAME + "-doc";

  @Requirement
  private ILuceneSearchService searchService;

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    try {
      IPresentationTypeRole<TableConfig> presentationType = getRowPresentationType(tableCfg);
      for (DocumentReference resultDocRef : executeTableQuery(tableCfg)) {
        presentationType.writeNodeContent(writer, resultDocRef, tableCfg);
      }
    } catch (XWikiVelocityException | LuceneSearchException exc) {
      logger.warn("writeTableContent - failed for [{}]", tableCfg, exc);
      writer.appendContent("search failed: " + exc.getMessage());
    }
  }

  private List<DocumentReference> executeTableQuery(TableConfig tableCfg)
      throws XWikiVelocityException, LuceneSearchException {
    int offset = firstNonNull(Ints.tryParse(context.getRequestParameter("offset").or("")), 0);
    String query = velocityService.evaluateVelocityText(tableCfg.getQuery());
    LuceneSearchResult result = searchService.search(query, tableCfg.getSortFields(),
        ImmutableList.of());
    logger.debug("executeTableQuery - [{}]", result);
    return result.getResults(offset, tableCfg.getResultLimit(), DocumentReference.class);
  }

}
