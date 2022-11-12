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

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.google.common.collect.ImmutableList;

public abstract class TablePresentationType extends AbstractTablePresentationType {

  @Requirement(TableRowColumnPresentationType.NAME)
  protected IPresentationTypeRole<TableConfig> rowColumnPresentationType;

  @Requirement(TableRowLayoutPresentationType.NAME)
  protected IPresentationTypeRole<TableConfig> rowLayoutPresentationType;

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
    AttributeBuilder attributes = newAttributeBuilder();
    attributes.addId(tableCfg.getCssId());
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addCssClasses(tableCfg.getCssClasses());
    writer.openLevel("div", attributes.build());
    writeHeader(writer, tableDocRef, tableCfg);
    writer.openLevel("div", newAttributeBuilder().addCssClasses(CSS_CLASS + "_scroll").build());
    writer.openLevel("ul", newAttributeBuilder().addCssClasses(CSS_CLASS + "_data").build());
    writeTableContent(writer, tableDocRef, tableCfg);
    if (!writer.hasLevelContent()) {
      writeEmptyRow(writer, tableDocRef, tableCfg);
    }
    writer.closeLevel(); // ul
    writer.closeLevel(); // div scroll
    writer.closeLevel(); // div main
  }

  protected abstract void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg);

  private void writeHeader(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    logger.debug("writeHeader - for [{}]", tableCfg);
    writer.openLevel("ul", newAttributeBuilder().addCssClasses(CSS_CLASS + "_header").build());
    tableCfg.setHeaderMode(true);
    getRowPresentationType(tableCfg).writeNodeContent(writer, tableDocRef, tableCfg);
    tableCfg.setHeaderMode(false);
    writer.closeLevel();
  }

  private void writeEmptyRow(ICellWriter writer, DocumentReference tableDocRef,
      TableConfig tableCfg) {
    TableConfig emptyTableCfg = new TableConfig();
    ColumnConfig emptyColCfg = new ColumnConfig();
    emptyColCfg.setName("empty");
    emptyColCfg.setCssClasses(ImmutableList.of("row_span"));
    emptyColCfg.setContent(webUtils.getAdminMessageTool().get(getEmptyDictionaryKey()));
    emptyTableCfg.setColumns(ImmutableList.of(emptyColCfg));
    getRowPresentationType(tableCfg).writeNodeContent(writer, tableDocRef, emptyTableCfg);
  }

  protected IPresentationTypeRole<TableConfig> getRowPresentationType(TableConfig tableCfg) {
    return tableCfg.getColumns().isEmpty()
        ? rowLayoutPresentationType
        : rowColumnPresentationType;
  }

}
