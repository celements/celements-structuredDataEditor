package com.celements.structEditor.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;
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
import com.celements.struct.SelectTagServiceRole;
import com.celements.structEditor.SelectAutocompleteRole;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class SelectTagAutocompletePageTypeTest extends AbstractComponentTest {

  private SelectTagAutocompletePageType pageType;
  private SelectTagServiceRole selectTagSrvMock;
  private StructuredDataEditorService structDataEditorSrvMock;
  private SelectAutocompleteRole autocompleteMock;

  @Before
  public void setUp_OptionTagPageTypeTest() throws Exception {
    registerComponentMock(ModelAccessStrategy.class);
    selectTagSrvMock = registerComponentMock(SelectTagServiceRole.class);
    structDataEditorSrvMock = registerComponentMock(StructuredDataEditorService.class);
    autocompleteMock = createMockAndAddToDefault(SelectAutocompleteRole.class);
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
    BaseObject obj = createSelectTagObj(cellDoc);
    obj.setIntValue(FIELD_AUTOCOMPLETE_IS_MULTISELECT.getName(), 1);
    obj.setStringValue(FIELD_AUTOCOMPLETE_SEPARATOR.getName(), "|");
    expect(structDataEditorSrvMock.getAttributeName(cellDoc, currentDoc))
        .andReturn(Optional.of("anyName"));
    expect(structDataEditorSrvMock.getCellValueAsString(cellDoc.getDocumentReference(), currentDoc))
        .andReturn(Optional.of("Space.Class"));
    expect(selectTagSrvMock.getTypeImpl(cellDoc.getDocumentReference()))
        .andReturn(Optional.of(autocompleteMock));
    expect(autocompleteMock.getName()).andReturn("impl");
    expect(autocompleteMock.getCssClass()).andReturn("implClass");

    replayDefault();
    pageType.collectAttributes(attributes, cellDoc.getDocumentReference());
    verifyDefault();

    assertEquals(6, attributes.build().size());
    assertAttribute(attributes, "class", "structAutocomplete implClass");
    assertAttribute(attributes, "name", "anyName");
    assertAttribute(attributes, "multiple", "multiple");
    assertAttribute(attributes, "data-autocomplete-type", "impl");
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

  private static BaseObject createSelectTagObj(XWikiDocument doc) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(CLASS_REF.getDocRef(doc.getDocumentReference().getWikiReference()));
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
