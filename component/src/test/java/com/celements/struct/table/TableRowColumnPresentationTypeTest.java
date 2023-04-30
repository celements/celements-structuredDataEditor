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
import static java.util.stream.Collectors.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class TableRowColumnPresentationTypeTest extends AbstractComponentTest {

  private TableRowColumnPresentationType presentationType;
  private XWikiDocument doc;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IWebUtilsService.class,
        IPageTypeResolverRole.class, ILuceneSearchService.class, VelocityService.class,
        StructDataService.class);
    presentationType = (TableRowColumnPresentationType) Utils.getComponent(
        IPresentationTypeRole.class,
        TableRowColumnPresentationType.NAME);
    doc = new XWikiDocument(new DocumentReference("xwikidb", "layoutspace", "tabledoc"));
    getContext().setDoc(doc);
  }

  @Test
  public void test_resolvePossibleTableNames_tableName() {
    expect(getMock(StructDataService.class).getStructLayoutSpaceRef(same(doc)))
        .andReturn(Optional.empty());
    expectPageTypeRef("tableName");

    replayDefault();
    assertEquals(Arrays.asList("tableName", ""), presentationType.resolvePossibleTableNames(doc)
        .collect(toList()));
    verifyDefault();
  }

  @Test
  public void test_resolvePossibleTableNames_layoutspace() {
    expect(getMock(StructDataService.class).getStructLayoutSpaceRef(same(doc)))
        .andReturn(Optional.of(doc.getDocumentReference().getLastSpaceReference()));
    expectAbsentPageTypeRef();

    replayDefault();
    assertEquals(Arrays.asList("layoutspace", ""), presentationType.resolvePossibleTableNames(doc)
        .collect(toList()));
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
        com.google.common.base.Optional.<PageTypeReference>absent());
  }

  private void expectPageTypeRef(String configName) {
    expect(getMock(IPageTypeResolverRole.class).resolvePageTypeReference(same(doc))).andReturn(
        com.google.common.base.Optional.fromNullable(new PageTypeReference(configName, null,
            Collections.<String>emptyList())));
  }

}
