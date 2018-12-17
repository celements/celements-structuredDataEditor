package com.celements.struct.table;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityManager;
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
import com.celements.struct.table.ColumnConfig;
import com.celements.struct.table.TableConfig;
import com.celements.struct.table.TablePresentationType;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

public class TablePresentationTypeTest extends AbstractComponentTest {

  private TablePresentationType presentationType;
  private XWikiDocument doc;

  private XWikiMessageTool msgToolMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IWebUtilsService.class,
        IPageTypeResolverRole.class, ILuceneSearchService.class, VelocityManager.class);
    presentationType = (TablePresentationType) Utils.getComponent(IPresentationTypeRole.class,
        TablePresentationType.NAME);
    doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "tabledoc"));
    msgToolMock = createMockAndAddToDefault(XWikiMessageTool.class);
    expect(getMock(IWebUtilsService.class).getAdminMessageTool()).andReturn(msgToolMock).anyTimes();
    expect(getMock(VelocityManager.class).getVelocityContext()).andReturn(
        new VelocityContext()).anyTimes();
    expect(getMock(VelocityManager.class).getVelocityEngine()).andReturn(
        new VelocityEngineMock()).anyTimes();
    getContext().setDoc(doc);
  }

  @Test
  public void test_writeNodeContent_noData() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig();

    expectLuceneSearch(table, Collections.<DocumentReference>emptyList(), 0);
    expect(msgToolMock.get("struct_table_nodata")).andReturn("no data");

    replayDefault();
    presentationType.writeNodeContent(writer, doc.getDocumentReference(), table);
    verifyDefault();

    assertEquals("<div id=\"tId\" class=\"struct_table t1 t2\">no data</div>",
        writer.getAsString());
  }

  @Test
  public void test_writeNodeContent() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig();
    XWikiDocument dataDoc1 = new XWikiDocument(new DocumentReference("wiki", "data", "doc1"));
    XWikiDocument dataDoc2 = new XWikiDocument(new DocumentReference("wiki", "data", "doc2"));
    List<DocumentReference> result = Arrays.asList(dataDoc1.getDocumentReference(),
        dataDoc2.getDocumentReference());

    expectGetDoc(doc);
    expectGetDoc(dataDoc1);
    expectGetDoc(dataDoc2);
    expectLuceneSearch(table, result, 0);

    replayDefault();
    presentationType.writeNodeContent(writer, doc.getDocumentReference(), table);
    verifyDefault();

    String expHtml = loadFile("table_dummy.html").replaceAll("  |\t|\n", "");
    assertEquals(expHtml, writer.getAsString());
  }

  private String loadFile(String file) throws IOException, URISyntaxException {
    Path path = Paths.get(getClass().getClassLoader().getResource(file).toURI());
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  private void expectGetDoc(XWikiDocument doc) throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference())).andReturn(
        doc).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).getApiDocument(doc)).andReturn(
        createMockAndAddToDefault(Document.class)).atLeastOnce();
  }

  @Test
  public void test_writeNodeContent_offset() throws Exception {
    ICellWriter writer = new DivWriter();
    TableConfig table = getDummyTableConfig();
    int offset = 123;

    expectLuceneSearch(table, Collections.<DocumentReference>emptyList(), offset);
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getContext().getRequest().get("offset")).andReturn(Integer.toString(offset));
    expect(msgToolMock.get("struct_table_nodata")).andReturn("");

    replayDefault();
    presentationType.writeNodeContent(writer, doc.getDocumentReference(), table);
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
    colA.setTitle("A");
    colA.setContent("asdf");
    colA.setOrder(10);
    colA.setCssClasses(Arrays.asList("cA1", "cA2"));
    ColumnConfig colB = new ColumnConfig();
    colB.setNumber(2);
    colB.setTitle("B");
    colB.setContent("fdsa");
    colB.setOrder(5);
    colB.setCssClasses(Arrays.asList("cB1", "cB2"));
    table.setColumns(Arrays.asList(colA, colB));
    return table;
  }

  private void expectLuceneSearch(TableConfig table, List<DocumentReference> result, int offset)
      throws LuceneSearchException {
    LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
    expect(getMock(ILuceneSearchService.class).search(table.getQuery(), table.getSortFields(),
        ImmutableList.<String>of())).andReturn(resultMock);
    expect(resultMock.getResults(offset, table.getResultLimit(),
        DocumentReference.class)).andReturn(result);
  }

  private class VelocityEngineMock implements VelocityEngine {

    @Override
    public void initialize(Properties properties) throws XWikiVelocityException {
    }

    @Override
    public boolean evaluate(Context context, Writer out, String templateName, String source)
        throws XWikiVelocityException {
      try {
        out.append(source);
      } catch (IOException exc) {
        throw new XWikiVelocityException("test", exc);
      }
      return true;
    }

    @Override
    public boolean evaluate(Context context, Writer out, String templateName, Reader source)
        throws XWikiVelocityException {
      return false;
    }

    @Override
    public void clearMacroNamespace(String templateName) {
    }

    @Override
    public void startedUsingMacroNamespace(String namespace) {
    }

    @Override
    public void stoppedUsingMacroNamespace(String namespace) {
    }

  }

}
