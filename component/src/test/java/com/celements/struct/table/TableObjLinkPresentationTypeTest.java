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
import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.struct.table.TableDocPresentationTypeTest.*;
import static com.celements.struct.table.TableObjLinkPresentationType.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.struct.classes.TableClass.Type;
import com.celements.velocity.VelocityService;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;

public class TableObjLinkPresentationTypeTest extends AbstractComponentTest {

  private TableObjLinkPresentationType presentationType;
  private XWikiDocument tableDoc;

  private ITablePresentationType rowPresentationTypeMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IWebUtilsService.class,
        IPageTypeResolverRole.class, VelocityService.class);
    rowPresentationTypeMock = registerComponentMock(ITablePresentationType.class,
        TableRowLayoutPresentationType.NAME);
    presentationType = (TableObjLinkPresentationType) Utils.getComponent(
        IPresentationTypeRole.class, TableObjLinkPresentationType.NAME);
    tableDoc = new XWikiDocument(new DocumentReference("xwikidb", "space", "tabledoc"));
    getContext().setDoc(tableDoc);
    expect(getMock(IModelAccessFacade.class).getDocument(tableDoc.getDocumentReference()))
        .andReturn(tableDoc).anyTimes();
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(tableDoc.getDocumentReference()))
        .andReturn(tableDoc).anyTimes();
    XWikiMessageTool msgToolMock = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(getMock(IWebUtilsService.class).getAdminMessageTool()).andReturn(msgToolMock).anyTimes();
  }

  @Test
  public void test_writeNodeContent_noData() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig(Type.OBJLINK);

    expectNoData(table, tableDoc);

    replayDefault();
    presentationType.writeNodeContent(writer, tableDoc.getDocumentReference(), table);
    verifyDefault();

    assertEquals(loadFile("table_dummy_empty.html", Type.OBJLINK), writer.getAsString());
  }

  @Test
  public void test_writeNodeContent() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig(Type.OBJLINK);
    XWikiDocument dataDoc1 = new XWikiDocument(new DocumentReference("wiki", "data", "doc1"));
    XWikiDocument dataDoc2 = new XWikiDocument(new DocumentReference("wiki", "data", "doc2"));
    List<XWikiDocument> rowDocs = ImmutableList.of(dataDoc1, dataDoc2);

    expectLinkObjs(table, rowDocs);
    expectTableRender(table, tableDoc, rowDocs);

    replayDefault();
    presentationType.writeNodeContent(writer, tableDoc.getDocumentReference(), table);
    verifyDefault();

    assertEquals(loadFile("table_dummy.html", Type.OBJLINK), writer.getAsString());
  }

  @Test
  public void test_writeNodeContent_rowLayout() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig(Type.OBJLINK);
    table.setColumns(ImmutableList.of());
    table.setRowLayout(new SpaceReference("layout", new WikiReference("db")));
    XWikiDocument dataDoc = new XWikiDocument(new DocumentReference("wiki", "data", "doc1"));

    rowPresentationTypeMock.writeNodeContent(anyObject(ICellWriter.class),
        eq(tableDoc.getDocumentReference()), same(table));
    expectLinkObjs(table, ImmutableList.of(dataDoc));
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(dataDoc.getDocumentReference()))
        .andReturn(dataDoc);
    rowPresentationTypeMock.writeNodeContent(anyObject(ICellWriter.class),
        eq(dataDoc.getDocumentReference()), same(table));
    expectLastCall().andAnswer(() -> {
      assertSame(tableDoc, getExecCtx().getProperty(CTX_PREFIX + EXEC_CTX_KEY_DOC_SUFFIX));
      assertSame(0, getExecCtx().getProperty(CTX_PREFIX + EXEC_CTX_KEY_OBJ_NB_SUFFIX));
      return writer.appendContent("asdf");
    });

    replayDefault();
    presentationType.writeNodeContent(writer, tableDoc.getDocumentReference(), table);
    verifyDefault();
  }

  private void expectLinkObjs(TableConfig table, List<XWikiDocument> linkedDocs) {
    DocumentReference linkClassDocRef = new DocumentReference(getContext().getDatabase(),
        "Classes", "LinkClass");
    for (int i = 0; i < linkedDocs.size(); i++) {
      DocumentReference docRef = linkedDocs.get(i).getDocumentReference();
      BaseObject obj = new BaseObject();
      obj.setXClassReference(linkClassDocRef);
      obj.setStringValue(LINK_FIELDS.get(i + 1),
          Utils.getComponent(ModelUtils.class).serializeRef(docRef));
      tableDoc.addXObject(obj);
    }
  }

  private ExecutionContext getExecCtx() {
    return Utils.getComponent(Execution.class).getContext();
  }
}
