package com.celements.struct.table;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.VelocityManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class TableRowPresentationTypeTest extends AbstractComponentTest {

  private TableRowPresentationType presentationType;
  private XWikiDocument doc;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IWebUtilsService.class,
        IPageTypeResolverRole.class, ILuceneSearchService.class, VelocityManager.class);
    presentationType = (TableRowPresentationType) Utils.getComponent(IPresentationTypeRole.class,
        TableRowPresentationType.NAME);
    doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "tabledoc"));
    getContext().setDoc(doc);
  }

  @Test
  public void test_resolveMacroName() {
    TableConfig table = getDummyTableConfig();
    ColumnConfig col = table.getColumns().get(0);
    col.setName("name");
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals("celStruct/table/tableName/col_name.vm", presentationType.resolveMacroName(col));
    verifyDefault();
  }

  @Test
  public void test_resolveMacroName_tableName_fallback_1() {
    TableConfig table = getDummyTableConfig();
    table.setCssId("tableName");
    ColumnConfig col = table.getColumns().get(0);
    col.setName("name");
    expectAbsentPageTypeRef();

    replayDefault();
    assertEquals("celStruct/table/tableName/col_name.vm", presentationType.resolveMacroName(col));
    verifyDefault();
  }

  @Test
  public void test_tableName_resolveMacroName_fallback_2() {
    TableConfig table = getDummyTableConfig();
    ColumnConfig col = table.getColumns().get(0);
    col.setName("name");
    expectAbsentPageTypeRef();

    replayDefault();
    assertEquals("celStruct/table/tabledoc/col_name.vm", presentationType.resolveMacroName(col));
    verifyDefault();
  }

  @Test
  public void test_resolveMacroName_colName_nonWhiteSpace() {
    TableConfig table = getDummyTableConfig();
    ColumnConfig col = table.getColumns().get(0);
    col.setName("the col-name");
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals("celStruct/table/tableName/col_the_col_name.vm", presentationType.resolveMacroName(
        col));
    verifyDefault();
  }

  @Test
  public void test_resolveMacroName_colName_fallback_1() {
    TableConfig table = getDummyTableConfig();
    ColumnConfig col = table.getColumns().get(0);
    col.setTitle("the col-name");
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals("celStruct/table/tableName/col_the_col_name.vm", presentationType.resolveMacroName(
        col));
    verifyDefault();
  }

  @Test
  public void test_resolveMacroName_colName_fallback_2() {
    TableConfig table = getDummyTableConfig();
    ColumnConfig col = table.getColumns().get(0);
    col.setOrder(5);
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals("celStruct/table/tableName/col_5.vm", presentationType.resolveMacroName(col));
    verifyDefault();
  }

  @Test
  public void test_resolveMacroName_colName_fallback_3() {
    TableConfig table = getDummyTableConfig();
    ColumnConfig col = table.getColumns().get(0);
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals("celStruct/table/tableName/col_2.vm", presentationType.resolveMacroName(col));
    verifyDefault();
  }

  private TableConfig getDummyTableConfig() {
    TableConfig table = new TableConfig();
    table.setDocumentReference(doc.getDocumentReference());
    ColumnConfig col = new ColumnConfig();
    col.setNumber(2);
    table.setColumns(Arrays.asList(col));
    return table;
  }

  private void expectAbsentPageTypeRef() {
    expect(getMock(IPageTypeResolverRole.class).resolvePageTypeReference(same(doc))).andReturn(
        Optional.<PageTypeReference>absent());
  }

  private void expectPageTypeRef(String configName) {
    expect(getMock(IPageTypeResolverRole.class).resolvePageTypeReference(same(doc))).andReturn(
        Optional.fromNullable(new PageTypeReference(configName, null,
            Collections.<String>emptyList())));
  }

}
