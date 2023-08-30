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

import static com.celements.cells.CellRenderStrategy.*;

import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.context.Contextualiser;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.CelConstant;

import one.util.streamex.StreamEx;

@Component(TableRowLayoutPresentationType.NAME)
public class TableRowLayoutPresentationType extends AbstractTableRowPresentationType {

  public static final String NAME = AbstractTableRowPresentationType.NAME + "-layout";

  @Requirement
  private LayoutServiceRole layoutService;

  @Override
  protected void writeRowContent(ICellWriter writer, DocumentReference rowDocRef,
      TableConfig tableCfg) {
    final SpaceReference tableCfgLayout = tableCfg.isHeaderMode()
        ? tableCfg.getHeaderLayout()
        : tableCfg.getRowLayout();
    if (tableCfgLayout != null) {
      // TODO add attributes to subcells somehow ?
      // attributes.addCssClasses(CSS_CLASS + "_cell");
      // attributes.addCssClasses("cell_" + colCfg.getNumber());
      // attributes.addCssClasses(colCfg.getName());
      // attributes.addCssClasses(colCfg.getCssClasses());
      writer.appendContent(StreamEx.of(tableCfgLayout)
          .append(Stream.of(context.getWikiRef(), CelConstant.CENTRAL_WIKI)
              .map(wiki -> new SpaceReference(tableCfgLayout.getName(), wiki)))
          .distinct()
          .filter(layoutService::canRenderLayout)
          .findFirst()
          .map(layout -> renderRowLayout(rowDocRef, layout))
          .orElseGet(() -> "Layout " + tableCfgLayout.getName() + " not valid"));
    } else if (tableCfg.isHeaderMode()) {
      writeDefaultLabel(writer, tableCfg);
    } else {
      writer.appendContent("No layout defined");
    }
  }

  private String renderRowLayout(DocumentReference rowDocRef, SpaceReference layout) {
    return new Contextualiser()
        .withDoc(context.getDocument()
            .filter(doc -> doc.getDocumentReference().equals(rowDocRef))
            .orElseGet(() -> modelAccess.getOrCreateDocument(rowDocRef)))
        // flags the cells as rendered repetitively, meaning e.g. id attributes should be omitted
        .withExecContext(EXEC_CTX_KEY_REPETITIVE, true)
        .execute(() -> layoutService.renderPageLayout(layout));
  }

  private void writeDefaultLabel(ICellWriter writer, TableConfig tableCfg) {
    writer.openLevel("label", new DefaultAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_label").build());
    writer.appendContent(resolveTitleFromDictionary(modelAccess.getOrCreateDocument(
        tableCfg.getDocumentReference()), NAME));
    writer.closeLevel();
  }

}
