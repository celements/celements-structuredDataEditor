package com.celements.structEditor.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.structEditor.classes.OptionTagEditorClass.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class OptionTagPageTypeTest extends AbstractComponentTest {

  private OptionTagPageType optionTagPT;
  private StructuredDataEditorService structDataEditorSrvMock;

  @Before
  public void setUp_OptionTagPageTypeTest() throws Exception {
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
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
    final IModelAccessFacade modelAccessMock = createMockAndAddToDefault(IModelAccessFacade.class);
    optionTagPT.modelAccess = modelAccessMock;
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    DocumentReference cellDocRef = new DocumentReference(getContext().getDatabase(), "TestSpace",
        "TestPage");
    String myTestValue = "myTestValue";
    Optional<String> optionConfigValue = Optional.of(myTestValue);
    expect(modelAccessMock.getFieldValue(eq(cellDocRef), eq(FIELD_VALUE))).andReturn(
        optionConfigValue).atLeastOnce();
    DocumentReference parentCell = new DocumentReference(getContext().getDatabase(), "TestSpace",
        "TestParentSelectCell");
    Optional<DocumentReference> selectCellDocRef = Optional.of(parentCell);
    expect(structDataEditorSrvMock.getSelectCellDocRef(eq(cellDocRef))).andReturn(selectCellDocRef);
    Optional<String> currentStoredValue = Optional.of(myTestValue);
    expect(structDataEditorSrvMock.getCellValueAsString(eq(parentCell), same(
        currentPageDoc))).andReturn(currentStoredValue);
    expect(modelAccessMock.getFieldValue(eq(cellDocRef), eq(FIELD_DISABLED))).andReturn(
        Optional.absent());
    expect(modelAccessMock.getFieldValue(eq(cellDocRef), eq(FIELD_LABEL))).andReturn(
        Optional.absent());
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
