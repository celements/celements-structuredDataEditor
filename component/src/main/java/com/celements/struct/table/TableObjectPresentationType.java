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

import static com.celements.structEditor.StructuredDataEditorService.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TableObjectPresentationType.NAME)
public class TableObjectPresentationType extends TablePresentationType {

  public static final String NAME = AbstractTablePresentationType.NAME + "-obj";

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    if (context.getCurrentDoc().isPresent()) {
      try {
        IPresentationTypeRole<TableConfig> presentationType = getRowPresentationType(tableCfg);
        XWikiDocument tableDoc = modelAccess.getOrCreateDocument(tableDocRef);
        XWikiDocument onDoc = context.getCurrentDoc().get();
        structDataEditorService.streamXObjectsForCell(tableDoc, onDoc)
            .forEach(obj -> {
              execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, obj.getNumber());
              presentationType.writeNodeContent(writer, onDoc.getDocumentReference(), tableCfg);
            });
      } finally {
        execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, null);
      }
    }
  }

}
