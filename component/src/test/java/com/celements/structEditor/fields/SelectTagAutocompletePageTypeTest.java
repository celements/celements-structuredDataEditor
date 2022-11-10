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
import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.cells.classes.CellClass;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.ModelAccessStrategy;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.struct.SelectTagServiceRole;
import com.celements.struct.edit.autocomplete.AutocompleteRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class SelectTagAutocompletePageTypeTest extends AbstractComponentTest {

  private SelectTagAutocompletePageType pageType;
  private SelectTagServiceRole selectTagSrvMock;
  private StructuredDataEditorService structDataEditorSrvMock;
  private AutocompleteRole autocompleteMock;

  @Before
  public void setUp_OptionTagPageTypeTest() throws Exception {
    registerComponentMock(ModelAccessStrategy.class);
    selectTagSrvMock = registerComponentMock(SelectTagServiceRole.class);
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
    autocompleteMock = createMockAndAddToDefault(AutocompleteRole.class);
    pageType = (SelectTagAutocompletePageType) Utils.getComponent(IJavaPageTypeRole.class,
        SelectTagAutocompletePageType.PAGETYPE_NAME);
  }

  @Test
  public void test_defaultTagName_present() {
    replayDefault();
    assertTrue(pageType.defaultTagName().isPresent());
    verifyDefault();
  }

  @Test
  public void test_defaultTagName_name() {
    replayDefault();
    assertEquals("select", pageType.defaultTagName().get());
    verifyDefault();
  }

  @Test
  public void test_getViewTemplateName() {
    replayDefault();
    assertEquals("SelectTagAutocompleteView", pageType.getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals("SelectTagAutocomplete", pageType.getName());
    verifyDefault();
  }

  @Test
  public void test_collectAttributes() throws Exception {
    XWikiDocument currentDoc = new XWikiDocument(new DocumentReference(
        getContext().getDatabase(), "Content", "Current"));
    getContext().setDoc(currentDoc);
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    XWikiDocument cellDoc = expectDoc(new DocumentReference(
        getContext().getDatabase(), "Layout", "Cell"));
    BaseObject obj = createObj(cellDoc, CLASS_REF);
    obj.setIntValue(FIELD_AUTOCOMPLETE_IS_MULTISELECT.getName(), 1);
    obj.setStringValue(FIELD_AUTOCOMPLETE_SEPARATOR.getName(), "|");
    obj = createObj(cellDoc, CellClass.CLASS_REF);
    obj.setStringValue(CellClass.FIELD_CSS_CLASSES.getName(), "css");
    expect(structDataEditorSrvMock.getAttributeName(cellDoc, currentDoc))
        .andReturn(Optional.of("Space.Class_0_field"));
    expect(structDataEditorSrvMock.getAttributeName(cellDoc, null))
        .andReturn(Optional.of("Space.Class_field"));
    expect(structDataEditorSrvMock.getCellValueAsString(cellDoc, currentDoc))
        .andReturn(Optional.of("Space.Class"));
    expect(selectTagSrvMock.getTypeImpl(cellDoc.getDocumentReference()))
        .andReturn(Optional.of(autocompleteMock));
    expect(autocompleteMock.getName()).andReturn("impl").anyTimes();

    replayDefault();
    pageType.collectAttributes(attributes, cellDoc.getDocumentReference());
    verifyDefault();

    assertEquals(8, attributes.build().size());
    assertAttribute(attributes, "class", "structAutocomplete impl");
    assertAttribute(attributes, "name", "Space.Class_0_field");
    assertAttribute(attributes, "multiple", "multiple");
    assertAttribute(attributes, "data-class-field", "Space.Class_field");
    assertAttribute(attributes, "data-autocomplete-type", "impl");
    assertAttribute(attributes, "data-autocomplete-css", "css");
    assertAttribute(attributes, "data-separator", "|");
    assertAttribute(attributes, "data-value", "Space.Class");

  }

  private static final XWikiDocument expectDoc(DocumentReference docRef) {
    XWikiDocument doc = new XWikiDocument(docRef);
    doc.setNew(false);
    expect(getMock(ModelAccessStrategy.class).getDocument(doc.getDocumentReference(), ""))
        .andReturn(doc);
    return doc;
  }

  private static BaseObject createObj(XWikiDocument doc, ClassReference classRef) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    doc.addXObject(obj);
    return obj;
  }

  private static void assertAttribute(AttributeBuilder attributes, String name, String value) {
    assertEquals(name, value, attributes.build().stream()
        .filter(attr -> attr.getName().equals(name))
        .map(attr -> attr.getValue().or(""))
        .findFirst().orElse(""));
  }

}
