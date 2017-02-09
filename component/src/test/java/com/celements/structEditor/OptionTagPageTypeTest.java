package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.CellAttribute;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.structEditor.classes.OptionTagEditorClass;
import com.celements.structEditor.fields.OptionTagPageType;
import com.google.common.base.Optional;
import com.xpn.xwiki.web.Utils;

public class OptionTagPageTypeTest extends AbstractComponentTest {

  private OptionTagPageType optionTagPageType;
  private IModelAccessFacade modelAccessMock;
  private DefaultAttributeBuilder attrBuilder;
  private StructuredDataEditorService strucDataEdSrvMock;

  @Before
  public void prepare_Test() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    strucDataEdSrvMock = registerComponentMock(StructuredDataEditorService.class);
    optionTagPageType = (OptionTagPageType) Utils.getComponent(IJavaPageTypeRole.class,
        OptionTagPageType.PAGETYPE_NAME);
    attrBuilder = new DefaultAttributeBuilder();
  }

  @Test
  public void testGetName() {
    String expectedStr = OptionTagPageType.PAGETYPE_NAME;
    assertEquals(expectedStr, optionTagPageType.getName());
  }

  @Test
  public void test_GetViewTemplateName() {
    String expectedStr = "OptionTagView";
    assertEquals(expectedStr, optionTagPageType.getViewTemplateName());
  }

  @Test
  public void test_defaultTagName() {
    assertEquals("option", optionTagPageType.defaultTagName().get());
  }

  @Test
  public void test_collectAttributes_DocumentNotExistsException() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_DISABLED))).andThrow(new DocumentNotExistsException(docRef));
    expect(strucDataEdSrvMock.getSelectCellDocRef(docRef)).andReturn(
        Optional.<DocumentReference>absent());
    replayDefault();
    optionTagPageType.collectAttributes(attrBuilder, docRef);
    verifyDefault();
    assertEquals(0, attrBuilder.build().size());
  }

  @Test
  public void test_collectAttributes_none() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_DISABLED))).andReturn(Optional.of(false));
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_VALUE))).andReturn(Optional.<String>absent());
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_LABEL))).andReturn(Optional.<String>absent());
    expect(strucDataEdSrvMock.getSelectCellDocRef(docRef)).andReturn(
        Optional.<DocumentReference>absent());
    replayDefault();
    optionTagPageType.collectAttributes(attrBuilder, docRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(0, cellAttrList.size());
  }

  @Test
  public void test_collectAttributes_field_selected_true() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    DocumentReference docRef2 = new DocumentReference("wikiName", "Celements", "gfrt");
    expect(strucDataEdSrvMock.getSelectCellDocRef(eq(docRef))).andReturn(
        Optional.<DocumentReference>of(docRef2));
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_VALUE))).andReturn(Optional.<String>absent());
    expect(strucDataEdSrvMock.getCellValueAsString(eq(docRef2), eq(
        getContext()).getDoc())).andReturn(Optional.<String>absent());
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_SELECTED))).andReturn(Optional.of(true));
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_DISABLED))).andReturn(Optional.of(false));
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_VALUE))).andReturn(Optional.<String>absent());
    expect(modelAccessMock.getFieldValue(eq(docRef), eq(
        OptionTagEditorClass.FIELD_LABEL))).andReturn(Optional.<String>absent());
    replayDefault();
    optionTagPageType.collectAttributes(attrBuilder, docRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(1, cellAttrList.size());
    assertEquals("selected", cellAttrList.get(0).getValue().get());
  }

}
