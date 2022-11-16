package com.celements.structEditor.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DateTimePageTypeTest extends AbstractComponentTest {

  private DateTimePageType dateTimePT;
  private StructuredDataEditorService structDataEditorSrvMock;

  @Before
  public void setUp_DateTimePageTypeTest() throws Exception {
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
    dateTimePT = (DateTimePageType) Utils.getComponent(IJavaPageTypeRole.class,
        DateTimePageType.PAGETYPE_NAME);
  }

  @Test
  public void testDefaultTagName_present() {
    replayDefault();
    assertTrue(dateTimePT.defaultTagName().isPresent());
    verifyDefault();
  }

  @Test
  public void testDefaultTagName_name() {
    replayDefault();
    assertEquals("cel-input-date-time", dateTimePT.defaultTagName().get());
    verifyDefault();
  }

  @Test
  public void testGetViewTemplateName() {
    replayDefault();
    assertEquals(DateTimePageType.VIEW_TEMPLATE_NAME, dateTimePT.getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void testGetName() {
    replayDefault();
    assertEquals(DateTimePageType.PAGETYPE_NAME, dateTimePT.getName());
    verifyDefault();
  }

  @Test
  public void testCollectAttributes() throws Exception {
    DocumentReference currentDocRef = new DocumentReference(getContext().getDatabase(), "Content",
        "CurrentTestPage");
    XWikiDocument currentPageDoc = new XWikiDocument(currentDocRef);
    getContext().setDoc(currentPageDoc);
    final IModelAccessFacade modelAccessMock = createMockAndAddToDefault(IModelAccessFacade.class);
    dateTimePT.modelAccess = modelAccessMock;
    AttributeBuilder attributes = new DefaultAttributeBuilder();
    DocumentReference cellDocRef = new DocumentReference(getContext().getDatabase(), "TestSpace",
        "TestPage");
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    Date myTestDateValue = new Date();
    expect(structDataEditorSrvMock.getCellDateValue(same(cellDoc), eq(currentPageDoc)))
        .andReturn(Optional.of(myTestDateValue));
    String dateFormat = "mm.dd.yyy HH:MM";
    expect(structDataEditorSrvMock.getDateFormatFromField(same(cellDoc)))
        .andReturn(Optional.of(dateFormat));
    String attrName = "class_property_name";
    expect(structDataEditorSrvMock.getAttributeName(same(cellDoc), same(currentPageDoc)))
        .andReturn(Optional.of(attrName));
    replayDefault();
    dateTimePT.collectAttributes(attributes, cellDocRef);
    verifyDefault();
    List<CellAttribute> attrList = attributes.build();
    String nameFound = "";
    for (CellAttribute elem : attrList) {
      if ("name".equals(elem.getName())) {
        nameFound = elem.getValue().get();
      }
    }
    assertEquals(attrName, nameFound);
    String valueFound = "";
    for (CellAttribute elem : attrList) {
      if ("value".equals(elem.getName())) {
        valueFound = elem.getValue().get();
      }
    }
    String myTestDateValueFormatted = new SimpleDateFormat(dateFormat).format(myTestDateValue);
    assertEquals(myTestDateValueFormatted, valueFound);
  }

}
