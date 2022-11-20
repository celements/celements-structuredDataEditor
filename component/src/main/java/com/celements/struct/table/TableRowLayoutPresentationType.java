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

import java.util.Optional;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.CelConstant;
import com.xpn.xwiki.doc.XWikiDocument;

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
      Optional<SpaceReference> layout = StreamEx.of(tableCfgLayout)
          .append(Stream.of(context.getWikiRef(), CelConstant.CENTRAL_WIKI)
              .map(wiki -> new SpaceReference(tableCfgLayout.getName(), wiki)))
          .distinct()
          .filter(layoutService::canRenderLayout)
          .findFirst();
      if (layout.isPresent()) {
        inContextDoc(rowDocRef, () -> writer.appendContent(
            layoutService.renderPageLayout(layout.get())));
      } else {
        writer.appendContent("Layout " + tableCfgLayout.getName() + " not valid");
      }
    } else if (tableCfg.isHeaderMode()) {
      writeDefaultLabel(writer, tableCfg);
    } else {
      writer.appendContent("No layout defined");
    }
  }

  private void writeDefaultLabel(ICellWriter writer, TableConfig tableCfg) {
    writer.openLevel("label", new DefaultAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_label").build());
    writer.appendContent(resolveTitleFromDictionary(modelAccess.getOrCreateDocument(
        tableCfg.getDocumentReference()), NAME));
    writer.closeLevel();
  }

  private void inContextDoc(DocumentReference docRef, Runnable runnable) {
    XWikiDocument currDoc = context.getCurrentDoc().orNull();
    if ((currDoc != null) && !currDoc.getDocumentReference().equals(docRef)) {
      context.setDoc(modelAccess.getOrCreateDocument(docRef));
    }
    try {
      runnable.run();
    } finally {
      context.setDoc(currDoc);
    }
  }

}
