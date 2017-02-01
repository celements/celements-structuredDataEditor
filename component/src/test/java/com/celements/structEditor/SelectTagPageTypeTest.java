package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
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
import com.celements.structEditor.fields.SelectTagPageType;
import com.xpn.xwiki.web.Utils;

public class SelectTagPageTypeTest extends AbstractComponentTest {

  private SelectTagPageType selectTagPageType;
  private IModelAccessFacade modelAccessMock;
  private DefaultAttributeBuilder attrBuilder;

  @Before
  public void prepare_Test() throws Exception {

    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
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
    List<CellAttribute> list = new ArrayList<>();
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andThrow(new DocumentNotExistsException(
        cellDocRef));
    replayDefault();
    selectTagPageType.collectAttributes(attrBuilder, cellDocRef);
    verifyDefault();
    assertEquals(list, attrBuilder.build());
  }

}
