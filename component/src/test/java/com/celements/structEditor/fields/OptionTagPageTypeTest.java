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
import static com.celements.structEditor.classes.OptionTagEditorClass.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.struct.SelectTagServiceRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class OptionTagPageTypeTest extends AbstractComponentTest {

  private OptionTagPageType optionTagPT;
  private SelectTagServiceRole selectTagSrvMock;
  private StructuredDataEditorService structDataEditorSrvMock;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_OptionTagPageTypeTest() throws Exception {
    selectTagSrvMock = registerComponentMock(SelectTagServiceRole.class);
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    optionTagPT = (OptionTagPageType) Utils.getComponent(IJavaPageTypeRole.class,
        OptionTagPageType.PAGETYPE_NAME);
  }

  @Test
  public void testDefaultTagName_present() {
    replayDefault();
    assertTrue(optionTagPT.defaultTagName().isPresent());
    verifyDefault();
  }

  @Test
  public void testDefaultTagName_name() {
    replayDefault();
    assertEquals("option", optionTagPT.defaultTagName().get());
    verifyDefault();
  }

  @Test
  public void testGetViewTemplateName() {
    replayDefault();
    assertEquals(OptionTagPageType.VIEW_TEMPLATE_NAME, optionTagPT.getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void testGetName() {
    replayDefault();
    assertEquals(OptionTagPageType.PAGETYPE_NAME, optionTagPT.getName());
    verifyDefault();
  }

  @Test
  public void testCollectAttributes() throws Exception {
    DocumentReference currentDocRef = new DocumentReference(getContext().getDatabase(), "Content",
        "CurrentTestPage");
    XWikiDocument currentPageDoc = new XWikiDocument(currentDocRef);
    getContext().setDoc(currentPageDoc);
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    DocumentReference cellDocRef = new DocumentReference(getContext().getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getDocument(cellDocRef)).andReturn(cellDoc);
    String myTestValue = "myTestValue";
    BaseObject optionObj = new BaseObject();
    optionObj.setXClassReference(CLASS_REF);
    cellDoc.addXObject(optionObj);
    optionObj.setStringValue(FIELD_VALUE.getName(), myTestValue);
    DocumentReference selectCellDocRef = new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestParentSelectCell");
    expect(selectTagSrvMock.getSelectCellDocRef(eq(cellDocRef)))
        .andReturn(Optional.of(selectCellDocRef));
    XWikiDocument selectCellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getDocument(selectCellDocRef)).andReturn(selectCellDoc);
    Optional<String> currentStoredValue = Optional.of(myTestValue);
    expect(structDataEditorSrvMock.getRequestOrCellValue(selectCellDoc, currentPageDoc))
        .andReturn(currentStoredValue);
    replayDefault();
    optionTagPT.collectAttributes(attributes, cellDocRef);
    verifyDefault();
    List<CellAttribute> attrList = attributes.build();
    boolean selectedFound = false;
    for (CellAttribute elem : attrList) {
      if ("selected".equals(elem.getName())) {
        selectedFound = true;
      }
    }
    assertTrue(selectedFound);
    String valueFound = "";
    for (CellAttribute elem : attrList) {
      if ("value".equals(elem.getName())) {
        valueFound = elem.getValue().get();
      }
    }
    assertEquals(myTestValue, valueFound);
  }

}
