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

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TableObjPresentationType.NAME)
public class TableObjPresentationType extends AbstractTablePresentationType {

  public static final String NAME = ITablePresentationType.NAME + "-obj";

  @Requirement
  private StructuredDataEditorService editorService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private Execution execution;

  @Override
  protected void writeHeader(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    try {
      // template in header should have negative objNb
      execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, -1);
      super.writeHeader(writer, tableDocRef, tableCfg);
    } finally {
      execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, null);
    }
  }

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    if (context.getCurrentDoc().isPresent()) {
      try {
        XWikiDocument tableDoc = modelAccess.getOrCreateDocument(tableDocRef);
        XWikiDocument onDoc = context.getCurrentDoc().get();
        editorService.streamXObjectsForCell(tableDoc, onDoc).forEach(obj -> {
          execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, obj.getNumber());
          getRowPresentationType(tableCfg).writeNodeContent(writer,
              onDoc.getDocumentReference(), tableCfg);
        });
      } finally {
        execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, null);
      }
    }
  }

}
