package com.celements.struct.table;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.struct.StructDataService;
import com.celements.velocity.VelocityService;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class TableRowPresentationTypeTest extends AbstractComponentTest {

  private TableRowPresentationType presentationType;
  private XWikiDocument doc;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IWebUtilsService.class,
        IPageTypeResolverRole.class, ILuceneSearchService.class, VelocityService.class,
        StructDataService.class);
    presentationType = (TableRowPresentationType) Utils.getComponent(IPresentationTypeRole.class,
        TableRowPresentationType.NAME);
    doc = new XWikiDocument(new DocumentReference("xwikidb", "layoutspace", "tabledoc"));
    getContext().setDoc(doc);
  }

  @Test
  public void test_resolvePossibleTableNames_tableName() {
    expect(getMock(StructDataService.class).getStructLayoutSpaceRef(same(doc)))
        .andReturn(Optional.absent());
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals(Arrays.asList("tableName", ""), presentationType.resolvePossibleTableNames(doc));
    verifyDefault();
  }

  @Test
  public void test_resolvePossibleTableNames_layoutspace() {
    expect(getMock(StructDataService.class).getStructLayoutSpaceRef(same(doc)))
        .andReturn(Optional.of(doc.getDocumentReference().getLastSpaceReference()));
    expectAbsentPageTypeRef();

    replayDefault();
    assertEquals(Arrays.asList("layoutspace", ""), presentationType.resolvePossibleTableNames(doc));
    verifyDefault();
  }

  @Test
  public void test_getVelocityContextModifier() throws Exception {
    ColumnConfig col = getDummyTableConfig().getColumns().get(0);
    VelocityContext vContext = new VelocityContext();
    Document apiDocMock = createMockAndAddToDefault(Document.class);

    expect(getMock(IModelAccessFacade.class).getApiDocument(same(doc))).andReturn(apiDocMock);

    replayDefault();
    assertSame(vContext, presentationType.getVelocityContextModifier(doc, col).apply(vContext));
    verifyDefault();

    assertSame(col, vContext.get("colcfg"));
    assertSame(apiDocMock, vContext.get("rowdoc"));
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
