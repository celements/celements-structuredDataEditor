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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;

public abstract class AbstractTablePresentationType implements ITablePresentationType {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Requirement(TableRowColumnPresentationType.NAME)
  protected ITablePresentationType rowColumnPresentationType;

  @Requirement(TableRowLayoutPresentationType.NAME)
  protected ITablePresentationType rowLayoutPresentationType;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected ModelContext context;

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
    logger.info("writeNodeContent - for [{}] with [{}]", tableDocRef, tableCfg);
    writer.openLevel("cel-table", new DefaultAttributeBuilder()
        .addId(tableCfg.getCssId())
        .addCssClasses(getDefaultCssClass())
        .addCssClasses(tableCfg.getCssClasses())
        .addNonEmptyAttribute("type", tableCfg.getType().name())
        .build());
    writeHeader(writer, tableDocRef, tableCfg);
    writer.openLevel("div", new DefaultAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_scroll").build());
    writer.openLevel("ul", new DefaultAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_data").build());
    writeTableContent(writer, tableDocRef, tableCfg);
    if (!writer.hasLevelContent()) {
      writeEmptyRow(writer, tableDocRef);
    }
    writer.closeLevel(); // ul
    writer.closeLevel(); // div scroll
    writer.closeLevel(); // cel-table
  }

  protected abstract void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg);

  protected void writeHeader(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    logger.debug("writeHeader - for [{}]", tableCfg);
    writer.openLevel("ul", new DefaultAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_header").build());
    tableCfg.setHeaderMode(true);
    getRowPresentationType(tableCfg).writeNodeContent(writer, tableDocRef, tableCfg);
    tableCfg.setHeaderMode(false);
    if (EDIT_ACTIONS.contains(context.getXWikiContext().getAction())) {
      writeTemplate(writer, tableDocRef, tableCfg);
    }
    writer.closeLevel(); // ul
  }

  private void writeTemplate(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    writer.openLevel("template", new DefaultAttributeBuilder()
        .addCssClasses("cel_template").build());
    getRowPresentationType(tableCfg).writeNodeContent(writer, tableDocRef, tableCfg);
    writer.closeLevel(); // template
  }

  private void writeEmptyRow(ICellWriter writer, DocumentReference tableDocRef) {
    TableConfig emptyTableCfg = new TableConfig();
    ColumnConfig emptyColCfg = new ColumnConfig();
    emptyColCfg.setName("empty");
    emptyColCfg.setCssClasses(ImmutableList.of("row_span"));
    emptyColCfg.setContent(webUtils.getAdminMessageTool().get(getEmptyDictionaryKey()));
    emptyTableCfg.setColumns(ImmutableList.of(emptyColCfg));
    getRowPresentationType(emptyTableCfg).writeNodeContent(writer, tableDocRef, emptyTableCfg);
  }

  protected ITablePresentationType getRowPresentationType(TableConfig tableCfg) {
    return tableCfg.getColumns().isEmpty()
        ? rowLayoutPresentationType
        : rowColumnPresentationType;
  }

  @Override
  public void writeNodeContent(StringBuilder writer, boolean isFirstItem, boolean isLastItem,
      DocumentReference nodeDocRef, boolean isLeaf, int numItem, TableConfig table) {
    writeNodeContent(new DivWriter(writer), nodeDocRef, table);
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

}
