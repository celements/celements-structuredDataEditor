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
import static com.google.common.base.Predicates.*;
import static java.util.stream.Collectors.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.ICellWriter;
import com.celements.marshalling.ReferenceMarshaller;
import com.celements.model.context.Contextualiser;
import com.celements.model.field.StringFieldAccessor;
import com.celements.model.field.XObjectStringFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ReferenceSerializationMode;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.web.comparators.BaseObjectComparator;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import one.util.streamex.StreamEx;

@Component(TableObjLinkPresentationType.NAME)
public class TableObjLinkPresentationType extends AbstractTablePresentationType {

  public static final String NAME = ITablePresentationType.NAME + "-obj-link";

  private static final List<String> LINK_FIELDS = ImmutableList.of("reference", "ref", "link");

  @Requirement
  private StructuredDataEditorService editorService;

  @Requirement(XObjectStringFieldAccessor.NAME)
  protected StringFieldAccessor<BaseObject> xObjStrFieldAccessor;

  @Requirement
  private Execution execution;

  @Override
  protected void writeHeader(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    Object prev = execution.getContext().getProperty(EXEC_CTX_KEY_OBJ_NB);
    try {
      // template in header should have negative objNb
      execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, -1);
      super.writeHeader(writer, tableDocRef, tableCfg);
    } finally {
      execution.getContext().setProperty(EXEC_CTX_KEY_OBJ_NB, prev);
    }
  }

  @Override
  protected void writeTableContent(ICellWriter writer,
      DocumentReference tableDocRef, TableConfig tableCfg) {
    if (context.getCurrentDoc().isPresent()) {
      IPresentationTypeRole<TableConfig> presentationType = getRowPresentationType(tableCfg);
      XWikiDocument tableDoc = modelAccess.getOrCreateDocument(tableDocRef);
      XWikiDocument onDoc = context.getCurrentDoc().get();
      Stream<ObjLinkRow> rows = StreamEx.of(editorService.streamXObjectsForCell(tableDoc, onDoc))
          .mapPartial(this::buildObjLinkRow);
      if (!tableCfg.getSortFields().stream().collect(joining()).trim().isEmpty()) {
        rows = rows.sorted(new ObjLinkRowComparator(tableCfg.getSortFields()));
      }
      rows.forEach(row -> new Contextualiser()
          .withExecContext(EXEC_CTX_KEY + ".source.document", onDoc)
          .withExecContext(EXEC_CTX_KEY + ".source.number", row.linkObj.getNumber())
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

  // TODO more general Doc-Sorts-comparator ?
  private static class ObjLinkRowComparator implements Comparator<ObjLinkRow> {

    private final List<ObjSort> sorts;

    public ObjLinkRowComparator(List<String> sortFields) {
      this.sorts = StreamEx.of(sortFields)
          .filter(not(String::isEmpty))
          .map(ObjSort::parse)
          .toImmutableList();
    }

    @Override
    public int compare(ObjLinkRow row1, ObjLinkRow row2) {
      for (ObjSort sort : sorts) {
        if (sort.classRef.isPresent()) {
          int cmp = BaseObjectComparator.create(sort.field, sort.asc).compare(
              fetch(row1.linkedDoc, sort.classRef.get()),
              fetch(row2.linkedDoc, sort.classRef.get()));
          if (cmp != 0) {
            return cmp;
          }
        } else {
          // TODO doc sort
        }
      }
      return 0;
    }

    private static BaseObject fetch(XWikiDocument doc, ClassReference ref) {
      return XWikiObjectFetcher.on(doc).filter(ref).stream().findFirst().orElse(null);
    }

    private static class ObjSort {

      static final String DELIM = ".";
      static final Splitter SPLITTER = Splitter.on(DELIM).omitEmptyStrings().trimResults();

      final Optional<ClassReference> classRef;
      final String field;
      final boolean asc;

      public ObjSort(ClassReference classRef, String field, boolean asc) {
        this.classRef = Optional.ofNullable(classRef);
        this.field = field;
        this.asc = asc;
      }

      // [-ClassSpace.ClassName.]fieldName
      public static ObjSort parse(String sort) {
        sort = sort.trim();
        boolean asc = !sort.startsWith("-");
        if (!asc) {
          sort = sort.replaceFirst("-", "");
        }
        ClassReference classRef;
        String field;
        try {
          classRef = new ClassReference(SPLITTER.splitToStream(sort).limit(2)
              .collect(joining(DELIM)));
          field = SPLITTER.splitToStream(sort).skip(2).collect(joining(DELIM));
        } catch (IllegalArgumentException iae) {
          classRef = null;
          field = sort;
        }
        return new ObjSort(classRef, field, asc);
      }
    }
  }
}
