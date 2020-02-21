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
import com.celements.model.access.ModelAccessStrategy;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NumberTagPageTypeTest extends AbstractComponentTest {

  private NumberTagPageType pageType;
  private StructuredDataEditorService structDataEditorSrvMock;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(ModelAccessStrategy.class);
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
    expect(structDataEditorSrvMock.getCellValueAsString(eq(cellDoc.getDocumentReference()),
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
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference(
        getContext().getDatabase(), "Layout", "Cell"));
    expect(getMock(ModelAccessStrategy.class).exists(cellDoc.getDocumentReference(), ""))
        .andReturn(false);
    AttributeBuilder attributes = new DefaultAttributeBuilder();

    replayDefault();
    pageType.collectAttributes(attributes, cellDoc.getDocumentReference());
    verifyDefault();

    assertEquals(1, attributes.build().size());
    assertAttribute(attributes, "type", "number");
  }

  private static final XWikiDocument expectDoc(DocumentReference docRef) {
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(ModelAccessStrategy.class).exists(doc.getDocumentReference(), ""))
        .andReturn(true);
    expect(getMock(ModelAccessStrategy.class).getDocument(doc.getDocumentReference(), ""))
        .andReturn(doc);
    return doc;
  }

  private static void assertAttribute(AttributeBuilder attributes, String name, String value) {
    assertEquals(name, value, attributes.build().stream()
        .filter(attr -> attr.getName().equals(name))
        .map(attr -> attr.getValue().or(""))
        .findFirst().orElse(""));
  }

}
