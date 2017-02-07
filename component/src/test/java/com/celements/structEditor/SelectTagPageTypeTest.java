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
import com.celements.structEditor.classes.SelectTagEditorClass;
import com.celements.structEditor.fields.SelectTagPageType;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class SelectTagPageTypeTest extends AbstractComponentTest {

  private SelectTagPageType selectTagPageType;
  private IModelAccessFacade modelAccessMock;
  private DefaultAttributeBuilder attrBuilder;
  private StructuredDataEditorService strucDataEdSrvMock;

  @Before
  public void prepare_Test() throws Exception {

    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    strucDataEdSrvMock = registerComponentMock(StructuredDataEditorService.class);
    selectTagPageType = (SelectTagPageType) Utils.getComponent(IJavaPageTypeRole.class,
        SelectTagPageType.PAGETYPE_NAME);
    attrBuilder = new DefaultAttributeBuilder();
  }

  @Test
  public void testGetName() {
    String expectedStr = "SelectTag";
    assertEquals(expectedStr, selectTagPageType.getName());
  }

  @Test
  public void test_GetViewTemplateName() {
    String expectedStr = "SelectTagView";
    assertEquals(expectedStr, selectTagPageType.getViewTemplateName());
  }

  @Test
  public void test_defaultTagName() {
    assertEquals("select", selectTagPageType.defaultTagName().get());
  }

  @Test
  public void test_collectAttributes_DocumentNotExistsException() throws Exception {
    DocumentReference cellDocRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andThrow(new DocumentNotExistsException(
        cellDocRef));
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    assertEquals(0, attrBuilder.build().size());
  }

  @Test
  public void test_collectAttributes_is_multiselect() throws Exception {
    DocumentReference cellDocRef = new DocumentReference("wikiName",
        SelectTagEditorClass.SPACE_NAME, "TestXClassName");
    XWikiDocument XWikiDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_MULTISELECT))).andReturn(Optional.of(true));
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_BOOTSTRAP))).andReturn(Optional.of(false));
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(XWikiDoc);
    expect(strucDataEdSrvMock.getAttributeName(eq(XWikiDoc), eq(getContext().getDoc()))).andReturn(
        Optional.<String>absent());
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(1, cellAttrList.size());
    assertEquals("class", cellAttrList.get(0).getName());
    assertEquals("celMultiselect", cellAttrList.get(0).getValue().get());
  }

  @Test
  public void test_collectAttributes_field_is_bootstrap() throws Exception {
    DocumentReference cellDocRef = new DocumentReference("wikiName",
        SelectTagEditorClass.SPACE_NAME, "TestXClassName");
    XWikiDocument XWikiDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_BOOTSTRAP))).andReturn(Optional.of(true));
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_MULTISELECT))).andReturn(Optional.of(false));
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(XWikiDoc);
    expect(strucDataEdSrvMock.getAttributeName(eq(XWikiDoc), eq(getContext().getDoc()))).andReturn(
        Optional.<String>absent());
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(1, cellAttrList.size());
    assertEquals("class", cellAttrList.get(0).getName());
    assertEquals("celBootstrap", cellAttrList.get(0).getValue().get());
  }

  @Test
  public void test_collectAttributes_none() throws Exception {
    DocumentReference cellDocRef = new DocumentReference("wikiName",
        SelectTagEditorClass.SPACE_NAME, "TestXClassName");
    XWikiDocument XWikiDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_BOOTSTRAP))).andReturn(Optional.of(false));
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_MULTISELECT))).andReturn(Optional.of(false));
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(XWikiDoc);
    expect(strucDataEdSrvMock.getAttributeName(eq(XWikiDoc), eq(getContext().getDoc()))).andReturn(
        Optional.<String>absent());
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(0, cellAttrList.size());
  }

  @Test
  public void test_collectAttributes_all() throws Exception {
    DocumentReference cellDocRef = new DocumentReference("wikiName",
        SelectTagEditorClass.SPACE_NAME, "TestXClassName");
    XWikiDocument XWikiDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_BOOTSTRAP))).andReturn(Optional.of(true));
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_MULTISELECT))).andReturn(Optional.of(true));
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(XWikiDoc);
    expect(strucDataEdSrvMock.getAttributeName(eq(XWikiDoc), eq(getContext().getDoc()))).andReturn(
        Optional.<String>of("name"));
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(2, cellAttrList.size());
    assertEquals("name", cellAttrList.get(0).getName());
    assertEquals("class", cellAttrList.get(1).getName());
    assertEquals("celMultiselect celBootstrap", cellAttrList.get(1).getValue().get());
  }

  @Test
  public void test_collectAttributes_addNonEmptyAttribute_name() throws Exception {
    DocumentReference cellDocRef = new DocumentReference("wikiName",
        SelectTagEditorClass.SPACE_NAME, "TestXClassName");
    XWikiDocument XWikiDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_MULTISELECT))).andReturn(Optional.of(false));
    expect(modelAccessMock.getFieldValue(eq(XWikiDoc), eq(
        SelectTagEditorClass.FIELD_IS_BOOTSTRAP))).andReturn(Optional.of(false));
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(XWikiDoc);
    expect(strucDataEdSrvMock.getAttributeName(eq(XWikiDoc), eq(getContext().getDoc()))).andReturn(
        Optional.<String>of("name"));
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    List<CellAttribute> cellAttrList = attrBuilder.build();
    assertEquals(1, cellAttrList.size());
    assertEquals("name", cellAttrList.get(0).getName());
  }

}
