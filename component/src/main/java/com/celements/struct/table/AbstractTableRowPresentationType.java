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

import static com.celements.model.util.ReferenceSerializationMode.*;

import java.util.Arrays;
import java.util.Optional;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractTableRowPresentationType extends AbstractTablePresentationType {

  public static final String NAME = AbstractTablePresentationType.NAME + "-row";

  @Override
  public String getDefaultCssClass() {
    return CSS_CLASS + "_row";
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "";
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference rowDocRef,
      TableConfig tableCfg) {
    logger.info("writeNodeContent - for [{}] with [{}]", rowDocRef, tableCfg);
    AttributeBuilder attributes = newAttributeBuilder();
    attributes.addCssClasses(Arrays.asList(getDefaultCssClass(),
        tableCfg.isHeaderMode() ? (CSS_CLASS + "_header") : ""));
    attributes.addAttribute("data-ref", modelUtils.serializeRef(rowDocRef, COMPACT_WIKI));
    writer.openLevel("li", attributes.build());
    writeRowContent(writer, rowDocRef, tableCfg);
    if (tableCfg.isHeaderMode() && isEditAction()) {
      writeCreateLink(writer);
    }
    writer.closeLevel(); // li
  }

  protected abstract void writeRowContent(ICellWriter writer,
      DocumentReference rowDocRef, TableConfig tableCfg);

  protected String resolveTitleFromDictionary(XWikiDocument cellDoc, String name) {
    String title = "";
    Optional<ClassReference> classRef = editorService.getCellClassRef(cellDoc);
    if (classRef.isPresent()) {
      String dictKey = modelUtils.serializeRef(classRef.get()) + "_" + name;
      String msg = webUtils.getAdminMessageTool().get(dictKey);
      if (!dictKey.equals(msg)) {
        title = msg;
      } else {
        logger.info("resolveTitleFromDictionary: nothing found for [{}]", dictKey);
      }
    }
    return title;
  }

}
