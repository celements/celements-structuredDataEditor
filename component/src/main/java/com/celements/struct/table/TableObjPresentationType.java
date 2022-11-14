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
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TableObjPresentationType.NAME)
public class TableObjPresentationType extends TablePresentationType {

  public static final String NAME = AbstractTablePresentationType.NAME + "-obj";

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    if (context.getCurrentDoc().isPresent()) {
      try {
        XWikiDocument tableDoc = modelAccess.getOrCreateDocument(tableDocRef);
        XWikiDocument onDoc = context.getCurrentDoc().get();
        editorService.streamXObjectsForCell(tableDoc, onDoc).forEach(obj -> {
          writeObjectRow(writer, onDoc.getDocumentReference(), tableCfg, obj.getNumber());
        });
        if (isEditAction()) {
          writer.openLevel("template");
          writeObjectRow(writer, onDoc.getDocumentReference(), tableCfg, -1);
          writer.closeLevel(); // template
        }
      } finally {
        execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, null);
      }
    }
  }

  private void writeObjectRow(ICellWriter writer, DocumentReference docRef,
      TableConfig tableCfg, int objNb) {
    execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, objNb);
    getRowPresentationType(tableCfg).writeNodeContent(writer, docRef, tableCfg);
    if (isEditAction()) {
      writeDeleteLink(writer);
    }
  }

}
