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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.velocity.VelocityContextModifier;
import com.celements.velocity.VelocityService;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

public class TablePresentationTypeTest extends AbstractComponentTest {

  private TablePresentationType presentationType;
  private XWikiDocument tableDoc;

  private XWikiMessageTool msgToolMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IWebUtilsService.class,
        IPageTypeResolverRole.class, ILuceneSearchService.class, VelocityService.class);
    presentationType = (TablePresentationType) Utils.getComponent(IPresentationTypeRole.class,
        TablePresentationType.NAME);
    tableDoc = new XWikiDocument(new DocumentReference("xwikidb", "space", "tabledoc"));
    msgToolMock = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(getMock(IWebUtilsService.class).getAdminMessageTool()).andReturn(msgToolMock).anyTimes();
    getContext().setDoc(tableDoc);
  }

  @Test
  public void test_writeNodeContent_noData() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig();

    expectLuceneSearch(table, Collections.<DocumentReference>emptyList(), 0);
    expectNoData(table);

    replayDefault();
    presentationType.writeNodeContent(writer, tableDoc.getDocumentReference(), table);
    verifyDefault();

    String expHtml = loadFile("table_dummy_empty.html").replaceAll("  |\t|\n", "");
    assertEquals(expHtml, writer.getAsString());
  }

  @Test
  public void test_writeNodeContent() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig();
    XWikiDocument dataDoc1 = new XWikiDocument(new DocumentReference("wiki", "data", "doc1"));
    XWikiDocument dataDoc2 = new XWikiDocument(new DocumentReference("wiki", "data", "doc2"));
    List<DocumentReference> result = Arrays.asList(dataDoc1.getDocumentReference(),
        dataDoc2.getDocumentReference());

    expectLuceneSearch(table, result, 0);
    expectTableRender(table, Arrays.asList(dataDoc1, dataDoc2));

    replayDefault();
    presentationType.writeNodeContent(writer, tableDoc.getDocumentReference(), table);
    verifyDefault();

    String expHtml = loadFile("table_dummy.html").replaceAll("  |\t|\n", "");
    assertEquals(expHtml, writer.getAsString());
  }

  private String loadFile(String file) throws IOException, URISyntaxException {
    Path path = Paths.get(getClass().getClassLoader().getResource(file).toURI());
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  private void expectTableRender(TableConfig table, List<XWikiDocument> docs) throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(tableDoc.getDocumentReference()))
        .andReturn(tableDoc).atLeastOnce();
    for (XWikiDocument dataDoc : docs) {
      expect(getMock(IModelAccessFacade.class).getDocument(dataDoc.getDocumentReference()))
          .andReturn(dataDoc).atLeastOnce();
    }
    for (ColumnConfig col : table.getColumns()) {
      expect(getMock(VelocityService.class).evaluateVelocityText(same(tableDoc), eq(col.getTitle()),
          anyObject(VelocityContextModifier.class))).andReturn(col.getTitle());
      for (XWikiDocument dataDoc : docs) {
        expect(
            getMock(VelocityService.class).evaluateVelocityText(same(dataDoc), eq(col.getContent()),
                anyObject(VelocityContextModifier.class))).andReturn(col.getContent());
      }
    }
  }

  @Test
  public void test_writeNodeContent_offset() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig();
    int offset = 123;

    expectLuceneSearch(table, Collections.<DocumentReference>emptyList(), offset);
    expectNoData(table);
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getContext().getRequest().get("offset")).andReturn(Integer.toString(offset));

    replayDefault();
    presentationType.writeNodeContent(writer, tableDoc.getDocumentReference(), table);
    verifyDefault();
  }

  private TableConfig getDummyTableConfig() {
    TableConfig table = new TableConfig();
    table.setQuery("anyQuery");
    table.setSortFields(Arrays.asList("x", "y"));
    table.setResultLimit(500);
    table.setCssId("tId");
    table.setCssClasses(Arrays.asList("t1", "t2"));
    ColumnConfig colA = new ColumnConfig();
    colA.setNumber(1);
    colA.setName("A name");
    colA.setTitle("A");
    colA.setContent("asdf");
    colA.setOrder(10);
    colA.setCssClasses(Arrays.asList("cA1", "cA2"));
    ColumnConfig colB = new ColumnConfig();
    colB.setNumber(2);
    colB.setName("B name");
    colB.setTitle("B");
    colB.setContent("fdsa");
    colB.setOrder(5);
    colB.setCssClasses(Arrays.asList("cB1", "cB2"));
    table.setColumns(Arrays.asList(colA, colB));
    return table;
  }

  private void expectLuceneSearch(TableConfig table, List<DocumentReference> result, int offset)
      throws LuceneSearchException, XWikiVelocityException {
    String queryEvaluated = table.getQuery() + "Evaluated";
    expect(getMock(VelocityService.class).evaluateVelocityText(table.getQuery()))
        .andReturn(queryEvaluated);
    LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
    expect(getMock(ILuceneSearchService.class).search(queryEvaluated, table.getSortFields(),
        ImmutableList.<String>of())).andReturn(resultMock);
    expect(resultMock.getResults(offset, table.getResultLimit(),
        DocumentReference.class)).andReturn(result);
  }

  private void expectNoData(TableConfig table) throws Exception {
    expectTableRender(table, Arrays.asList());
    String value = "no data";
    expect(msgToolMock.get("struct_table_nodata")).andReturn(value);
    expect(getMock(VelocityService.class).evaluateVelocityText(same(tableDoc), eq(value),
        anyObject(VelocityContextModifier.class))).andReturn(value);
  }

}
