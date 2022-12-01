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

import java.util.Comparator;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.model.context.Contextualiser;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.web.comparators.BaseObjectComparator;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(TableObjPresentationType.NAME)
public class TableObjPresentationType extends AbstractTablePresentationType {

  public static final String NAME = ITablePresentationType.NAME + "-obj";

  @Requirement
  private StructuredDataEditorService editorService;

  @Override
  protected void writeHeader(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    // template in header should have negative objNb
    new Contextualiser()
        .withExecContext(EXEC_CTX_KEY_OBJ_NB, -1)
        .execute(() -> super.writeHeader(writer, tableDocRef, tableCfg));
  }

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    if (context.getCurrentDoc().isPresent()) {
      IPresentationTypeRole<TableConfig> presentationType = getRowPresentationType(tableCfg);
      XWikiDocument tableDoc = modelAccess.getOrCreateDocument(tableDocRef);
      XWikiDocument onDoc = context.getCurrentDoc().get();
      Stream<BaseObject> objs = editorService.streamXObjectsForCell(tableDoc, onDoc);
      Comparator<BaseObject> comp = BaseObjectComparator
          .create(tableCfg.getSortFields()).orElse(null);
      ((comp != null) ? objs.sorted(comp) : objs).forEach(obj -> new Contextualiser()
          .withExecContext(EXEC_CTX_KEY_OBJ_NB, obj.getNumber())
          .execute(() -> presentationType.writeNodeContent(writer,
              onDoc.getDocumentReference(), tableCfg)));
    }
  }

}
