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
package com.celements.structEditor.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NumberTagPageTypeTest extends AbstractComponentTest {

  private NumberTagPageType pageType;
  private StructuredDataEditorService structDataEditorSrvMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
    pageType = (NumberTagPageType) Utils.getComponent(IJavaPageTypeRole.class,
        NumberTagPageType.PAGETYPE_NAME);
    getContext().setDoc(new XWikiDocument(new DocumentReference(
        getContext().getDatabase(), "Content", "Current")));
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals("NumberTag", pageType.getName());
    verifyDefault();
  }

  @Test
  public void test_getViewTemplateName() {
    replayDefault();
    assertEquals("NumberTagView", pageType.getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void test_tagName() {
    replayDefault();
    Optional<String> name = pageType.tagName();
    verifyDefault();
    assertTrue(name.isPresent());
    assertEquals("input", name.get());
  }

  @Test
  public void test_collectAttributes() throws Exception {
    XWikiDocument cellDoc = expectDoc(new DocumentReference(
        getContext().getDatabase(), "Layout", "Cell"));
    expect(structDataEditorSrvMock.getAttributeName(same(cellDoc), same(getContext().getDoc())))
        .andReturn(Optional.of("Space.Class_1_field"));
    expect(structDataEditorSrvMock.getRequestOrCellValue(same(cellDoc),
        same(getContext().getDoc()))).andReturn(Optional.of("3421"));
    AttributeBuilder attributes = new DefaultAttributeBuilder();

    replayDefault();
    pageType.collectAttributes(attributes, cellDoc.getDocumentReference());
    verifyDefault();

    assertEquals(3, attributes.build().size());
    assertAttribute(attributes, "type", "number");
    assertAttribute(attributes, "name", "Space.Class_1_field");
    assertAttribute(attributes, "value", "3421");
  }

  @Test
  public void test_collectAttributes_cellDocNotExists() throws Exception {
    DocumentReference cellDocRef = new DocumentReference(
        getContext().getDatabase(), "Layout", "Cell");
    expect(getMock(IModelAccessFacade.class).getDocument(cellDocRef))
        .andThrow(new DocumentNotExistsException(cellDocRef));
    AttributeBuilder attributes = new DefaultAttributeBuilder();

    replayDefault();
    pageType.collectAttributes(attributes, cellDocRef);
    verifyDefault();

    assertEquals(1, attributes.build().size());
    assertAttribute(attributes, "type", "number");
  }

  private final XWikiDocument expectDoc(DocumentReference docRef)
      throws DocumentNotExistsException {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setNew(false);
    expect(getMock(IModelAccessFacade.class).getDocument(doc.getDocumentReference()))
        .andReturn(doc);
    return doc;
  }

  private static void assertAttribute(AttributeBuilder attributes, String name, String value) {
    assertEquals(name, value, attributes.build().stream()
        .filter(attr -> attr.getName().equals(name))
        .map(attr -> attr.getValue().orElse(""))
        .findFirst().orElse(""));
  }

}
