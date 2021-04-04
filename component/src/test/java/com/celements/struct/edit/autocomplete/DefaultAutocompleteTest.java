package com.celements.struct.edit.autocomplete;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.structEditor.classes.OptionTagEditorClass.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.ModelUtils;
import com.celements.search.web.IWebSearchService;
import com.celements.structEditor.StructuredDataEditorService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class DefaultAutocompleteTest extends AbstractComponentTest {

  private DefaultAutocomplete autocomplete;

  private XWikiDocument doc;
  private XWikiDocument cellDoc;
  private final String selected = "wiki:space.target";
  private StructuredDataEditorService structMock;

  @Before
  public void prepare() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, StructuredDataEditorService.class,
        IWebSearchService.class);
    structMock = getMock(StructuredDataEditorService.class);
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    doc = new XWikiDocument(new DocumentReference("wiki", "space", "doc"));
    getContext().setDoc(doc);
    cellDoc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "LayoutSpace", "SomeAutocomplete"));
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(cellDoc.getDocumentReference()))
        .andReturn(cellDoc).anyTimes();
    autocomplete = (DefaultAutocomplete) Utils.getComponent(AutocompleteRole.class);
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals("default", autocomplete.getName());
    verifyDefault();
  }

  @Test
  public void test_getJsFilePath() {
    replayDefault();
    assertEquals("", autocomplete.getJsFilePath());
    verifyDefault();
  }

  @Test
  public void test_displayNameForValue() throws Exception {
    getContext().setLanguage("en");
    doc.setTitle("name");
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(doc.getDocumentReference(), "en"))
        .andReturn(doc).once();

    replayDefault();
    assertEquals("name", autocomplete.displayNameForValue(doc.getDocumentReference()));
    verifyDefault();
  }

  @Test
  public void test_getJsonForValue() throws Exception {
    getContext().setLanguage("fr");
    doc.setTitle("name");
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(doc.getDocumentReference(), "fr"))
        .andReturn(doc).once();

    replayDefault();
    String json = "{\"fullName\" : \"wiki:space.doc\", "
        + "\"name\" : \"name\"}";
    assertEquals(json, autocomplete.getJsonForValue(doc.getDocumentReference()).getJSON());
    verifyDefault();
  }

  @Test
  public void test_getSelectedValue_noContextDoc() throws Exception {
    getContext().setDoc(null);
    replayDefault();
    assertFalse(autocomplete.getSelectedValue(cellDoc.getDocumentReference()).isPresent());
    verifyDefault();
  }

  @Test
  public void test_getSelectedValue_fromRequest() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.of("key"));
    expect(getContext().getRequest().get("key")).andReturn(selected);

    replayDefault();
    assertEquals(selected, getUtils().serializeRef(
        autocomplete.getSelectedValue(cellDoc.getDocumentReference()).orElse(null)));
    verifyDefault();
  }

  @Test
  public void test_getValueFromRequest() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.of("key"));
    expect(getContext().getRequest().get("key")).andReturn(selected);

    replayDefault();
    assertEquals(selected, autocomplete.getValueFromRequest(cellDoc).orElse(null));
    verifyDefault();
  }

  @Test
  public void test_getValueFromRequest_noValueOnDoc() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.of("key"));
    expect(getContext().getRequest().get("key")).andReturn("");

    replayDefault();
    assertFalse(autocomplete.getValueFromRequest(cellDoc).isPresent());
    verifyDefault();
  }

  @Test
  public void test_getValueFromRequest_noRequestName() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.empty());

    replayDefault();
    assertFalse(autocomplete.getValueFromRequest(cellDoc).isPresent());
    verifyDefault();
  }

  @Test
  public void test_getSelectedValue_onDoc() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.empty());
    expect(structMock.getCellValueAsString(same(cellDoc), same(doc)))
        .andReturn(Optional.of(selected));

    replayDefault();
    assertEquals(selected, getUtils().serializeRef(
        autocomplete.getSelectedValue(cellDoc.getDocumentReference()).orElse(null)));
    verifyDefault();
  }

  @Test
  public void test_getValueOnDoc() throws Exception {
    expect(structMock.getCellValueAsString(same(cellDoc), same(doc)))
        .andReturn(Optional.of(selected));

    replayDefault();
    assertEquals(selected, autocomplete.getValueOnDoc(cellDoc).orElse(null));
    verifyDefault();
  }

  @Test
  public void test_getValueOnDoc_none() throws Exception {
    expect(structMock.getCellValueAsString(same(cellDoc), same(doc))).andReturn(Optional.empty());

    replayDefault();
    assertFalse(autocomplete.getValueOnDoc(cellDoc).isPresent());
    verifyDefault();
  }

  @Test
  public void test_getSelectedValue_defaultValue() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.empty());
    expect(structMock.getCellValueAsString(same(cellDoc), same(doc))).andReturn(Optional.empty());
    addDefaultValue(selected);

    replayDefault();
    assertEquals(selected, getUtils().serializeRef(
        autocomplete.getSelectedValue(cellDoc.getDocumentReference()).orElse(null)));
    verifyDefault();
  }

  @Test
  public void test_getDefaultValue() throws Exception {
    addDefaultValue(selected);
    replayDefault();
    assertEquals(selected, autocomplete.getDefaultValue(cellDoc).orElse(null));
    verifyDefault();
  }

  @Test
  public void test_getDefaultValue_none() throws Exception {
    replayDefault();
    assertFalse(autocomplete.getDefaultValue(cellDoc).isPresent());
    verifyDefault();
  }

  private void addDefaultValue(String value) {
    BaseObject obj = new BaseObject();
    obj.setDocumentReference(cellDoc.getDocumentReference());
    obj.setXClassReference(CLASS_REF);
    obj.setStringValue(FIELD_VALUE.getName(), value);
    cellDoc.addXObject(obj);
  }

  @Test
  public void test_getSelectedValue_none() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.empty());
    expect(structMock.getCellValueAsString(same(cellDoc), same(doc))).andReturn(Optional.empty());

    replayDefault();
    assertFalse(autocomplete.getSelectedValue(cellDoc.getDocumentReference()).isPresent());
    verifyDefault();
  }

  @Test
  public void test_getSelectedValue_unresolvable() throws Exception {
    expect(structMock.getAttributeName(same(cellDoc), same(doc))).andReturn(Optional.empty());
    expect(structMock.getCellValueAsString(same(cellDoc), same(doc)))
        .andReturn(Optional.of("asdf"));

    replayDefault();
    assertFalse(autocomplete.getSelectedValue(cellDoc.getDocumentReference()).isPresent());
    verifyDefault();
  }

  private ModelUtils getUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

}
