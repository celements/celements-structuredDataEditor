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
import static java.util.Comparator.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.marshalling.ReferenceMarshaller;
import com.celements.model.context.Contextualiser;
import com.celements.model.field.StringFieldAccessor;
import com.celements.model.field.XObjectStringFieldAccessor;
import com.celements.model.util.ReferenceSerializationMode;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.web.comparators.XDocumentFieldComparator;
import com.celements.web.comparators.XDocumentFieldComparator.SortField;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import one.util.streamex.StreamEx;

@Component(TableObjLinkPresentationType.NAME)
public class TableObjLinkPresentationType extends TableObjPresentationType {

  public static final String NAME = ITablePresentationType.NAME + "-objlink";

  private static final String CTX_PREFIX = EXEC_CTX_KEY + ".source";
  private static final List<String> LINK_FIELDS = ImmutableList.of("reference", "ref", "link");

  @Requirement(XObjectStringFieldAccessor.NAME)
  protected StringFieldAccessor<BaseObject> xObjStrFieldAccessor;

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    if (context.getCurrentDoc().isPresent()) {
      IPresentationTypeRole<TableConfig> presentationType = getRowPresentationType(tableCfg);
      XWikiDocument tableDoc = modelAccess.getOrCreateDocument(tableDocRef);
      XWikiDocument onDoc = context.getCurrentDoc().get();
      StreamEx.of(editorService.streamXObjectsForCell(tableDoc, onDoc))
          .mapPartial(this::buildObjLinkRow)
          .sorted(getRowComparator(tableCfg))
          .forEach(row -> new Contextualiser()
              .withExecContext(CTX_PREFIX + EXEC_CTX_KEY_DOC_SUFFIX, onDoc)
              .withExecContext(CTX_PREFIX + EXEC_CTX_KEY_OBJ_NB_SUFFIX, row.linkObj.getNumber())
              .execute(() -> presentationType.writeNodeContent(writer, row.getDocRef(), tableCfg)));
    }
  }

  private Optional<ObjLinkRow> buildObjLinkRow(BaseObject obj) {
    return StreamEx.of(LINK_FIELDS)
        .mapPartial(field -> xObjStrFieldAccessor.get(obj, field))
        .findFirst()
        .map(Objects::toString)
        .map(new ReferenceMarshaller.Builder<>(DocumentReference.class)
            .serializationMode(ReferenceSerializationMode.GLOBAL)
            .baseRef(context.getCurrentDocRef().get())
            .build().getResolver())
        .map(ref -> new ObjLinkRow(obj, modelAccess.getOrCreateDocument(ref)));
  }

  private Comparator<ObjLinkRow> getRowComparator(TableConfig tableCfg) {
    Stream<SortField> sorts = StreamEx.of(tableCfg.getSortFields())
        .mapPartial(SortField::parse);
    return comparing(row -> row.linkedDoc, new XDocumentFieldComparator(sorts));
  }

  private class ObjLinkRow {

    final BaseObject linkObj;
    final XWikiDocument linkedDoc;

    ObjLinkRow(BaseObject linkObj, XWikiDocument linkedDoc) {
      this.linkObj = linkObj;
      this.linkedDoc = linkedDoc;
    }

    public DocumentReference getDocRef() {
      return linkedDoc.getDocumentReference();
    }
  }
}
