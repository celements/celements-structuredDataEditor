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
import static com.google.common.base.Strings.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractTableRowPresentationType implements ITablePresentationType {

  public static final String NAME = ITablePresentationType.NAME + "-row";

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Requirement
  protected StructuredDataEditorService editorService;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelUtils modelUtils;

  @Requirement
  protected ModelContext context;

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
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addAttribute("data-ref", modelUtils.serializeRef(rowDocRef, COMPACT_WIKI));
    writer.openLevel("li", attributes.build());
    writeRowContent(writer, rowDocRef, tableCfg);
    if (EDIT_ACTIONS.contains(context.getXWikiContext().getAction())) {
      if (tableCfg.isHeaderMode()) {
        writeLink(writer, "create", "halflings icon-plus");
      } else {
        writeLink(writer, "delete", "halflings icon-trash");
      }
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

  private void writeLink(ICellWriter writer, String name, String icon) {
    writer.openLevel("a", new DefaultAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_" + name)
        .addEmptyAttribute("href")
        .build());
    writer.openLevel("i", new DefaultAttributeBuilder()
        .addCssClasses("icon " + nullToEmpty(icon))
        .addAttribute("title", name)
        .build());
    writer.closeLevel(); // i
    writer.closeLevel(); // a
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
