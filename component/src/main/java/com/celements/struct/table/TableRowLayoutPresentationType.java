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

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.pagelayout.LayoutServiceRole;

@Component(TableRowLayoutPresentationType.NAME)
public class TableRowLayoutPresentationType extends AbstractTableRowPresentationType {

  public static final String NAME = AbstractTableRowPresentationType.NAME + "-layout";

  @Requirement
  private LayoutServiceRole layoutService;

  @Override
  protected void writeRowContent(ICellWriter writer, DocumentReference rowDocRef,
      TableConfig tableCfg) {
    // TODO set rowDocRef as contextDoc if rowDocRef isn't $doc already
    SpaceReference layout = tableCfg.isHeaderMode()
        ? tableCfg.getHeaderLayout()
        : tableCfg.getRowLayout();
    if (layout != null) {
      // TODO add attributes to subcells somehow ?
      // attributes.addCssClasses(CSS_CLASS + "_cell");
      // attributes.addCssClasses("cell_" + colCfg.getNumber());
      // attributes.addCssClasses(colCfg.getName());
      // attributes.addCssClasses(colCfg.getCssClasses());
      writer.appendContent(layoutService.renderPageLayout(layout));
    } else if (tableCfg.isHeaderMode()) {
      // TODO render default (object?) header
      writer.appendContent(resolveTitleFromDictionary(modelAccess.getOrCreateDocument(
          tableCfg.getDocumentReference()), NAME));
    }
  }

}
