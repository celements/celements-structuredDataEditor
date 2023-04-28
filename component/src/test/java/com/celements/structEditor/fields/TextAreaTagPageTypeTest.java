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
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class TextAreaTagPageTypeTest extends AbstractComponentTest {

  private TextAreaTagPageType pageType;
  private StructuredDataEditorService structDataEditorSrvMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
    pageType = (TextAreaTagPageType) Utils.getComponent(IJavaPageTypeRole.class,
        TextAreaTagPageType.PAGETYPE_NAME);
    getContext().setDoc(new XWikiDocument(new DocumentReference(
        getContext().getDatabase(), "Content", "Current")));
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals("TextAreaTag", pageType.getName());
    verifyDefault();
  }

  @Test
  public void test_getViewTemplateName() {
    replayDefault();
    assertEquals("TextAreaTagView", pageType.getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void test_tagName() {
    replayDefault();
    Optional<String> name = pageType.tagName();
    verifyDefault();
    assertTrue(name.isPresent());
    assertEquals("textarea", name.get());
  }

  @Test
  public void test_collectAttributes() throws Exception {
    XWikiDocument cellDoc = expectDoc(new DocumentReference(
        getContext().getDatabase(), "Layout", "Cell"));
    expect(structDataEditorSrvMock.getAttributeName(same(cellDoc), same(getContext().getDoc())))
        .andReturn(Optional.of("Space.Class_1_field"));
    BaseObject textAreaObj = new BaseObject();
    textAreaObj.setXClassReference(TextAreaFieldEditorClass.CLASS_REF);
    cellDoc.addXObject(textAreaObj);
    textAreaObj.setIntValue(TextAreaFieldEditorClass.FIELD_IS_RICHTEXT.getName(), 1);
    textAreaObj.setIntValue(TextAreaFieldEditorClass.FIELD_ROWS.getName(), 6);
    textAreaObj.setIntValue(TextAreaFieldEditorClass.FIELD_COLS.getName(), 13);
    AttributeBuilder attributes = new DefaultAttributeBuilder();

    replayDefault();
    pageType.collectAttributes(attributes, cellDoc.getDocumentReference());
    verifyDefault();

    assertEquals(4, attributes.build().size());
    assertAttribute(attributes, "class", "structEditTextArea mceEditor tinyMCE tinyMCEV4");
    assertAttribute(attributes, "name", "Space.Class_1_field");
    assertAttribute(attributes, "rows", "6");
    assertAttribute(attributes, "cols", "13");
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
    assertAttribute(attributes, "class", "structEditTextArea");
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
