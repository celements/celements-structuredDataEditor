package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class StructuredDataEditorScriptServiceTest extends AbstractComponentTest {

  private StructuredDataEditorScriptService structuredDataEditorScriptService;
  private StructuredDataEditorService serviceMock;
  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private String wikiName;
  private TextAreaFieldEditorClass field;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, StructuredDataEditorService.class);
    structuredDataEditorScriptService = (StructuredDataEditorScriptService) Utils.getComponent(
        ScriptService.class, "structuredDataEditor");
    serviceMock = getMock(StructuredDataEditorService.class);
    modelAccessMock = getMock(IModelAccessFacade.class);
    context = getContext();
    wikiName = context.getDatabase();
    context.setDoc(new XWikiDocument(new DocumentReference("wiki", "space", "contextDoc")));
  }

  @Test
  public void test_getPrettyName_throwException() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(serviceMock.getPrettyName(eq(docRef))).andThrow(new DocumentNotExistsException(docRef));

    replayDefault();
    String ret = structuredDataEditorScriptService.getPrettyName(docRef);
    verifyDefault();
    assertTrue(ret.isEmpty());
  }

  @Test
  public void test_getPrettyName_empty() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(serviceMock.getPrettyName(eq(docRef))).andReturn(Optional.<String>absent());

    replayDefault();
    String ret = structuredDataEditorScriptService.getPrettyName(docRef);
    verifyDefault();
    assertTrue(ret.isEmpty());
  }

  @Test
  public void test_getPrettyName_notEmpty() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(serviceMock.getPrettyName(eq(docRef))).andReturn(Optional.<String>of("reference"));

    replayDefault();
    String ret = structuredDataEditorScriptService.getPrettyName(docRef);
    verifyDefault();
    assertEquals("reference", ret);
  }

  @Test
  public void test_getTextAttributes_throwException() throws Exception {
    Map<String, String> retMap = new LinkedHashMap<>();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getDocument(eq(docRef))).andThrow(new DocumentNotExistsException(
        docRef));

    replayDefault();
    retMap = structuredDataEditorScriptService.getTextAttributes(docRef);
    verifyDefault();
    assertEquals(0, retMap.size());
  }

  @Test
  public void test_getTextAttributes_addNameAttributeToMap_Empty() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(cellDoc);
    expect(serviceMock.getAttributeName(cellDoc, context.getDoc())).andReturn(
        Optional.<String>absent());
    replayDefault();
    Map<String, String> retMap = structuredDataEditorScriptService.getTextAttributes(docRef);
    verifyDefault();
    assertEquals(2, retMap.size());
    assertEquals(ImmutableMap.<String, String>builder().put("value", "").put("type",
        "text").build(), retMap);
  }

  @Test
  public void test_getTextAttributes_addNameAttributeToMap_notEmpty() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(cellDoc);
    expect(serviceMock.getAttributeName(cellDoc, context.getDoc())).andReturn(Optional.<String>of(
        "val"));
    replayDefault();
    Map<String, String> retMap = structuredDataEditorScriptService.getTextAttributes(docRef);
    verifyDefault();
    assertEquals(3, retMap.size());
    assertEquals(ImmutableMap.<String, String>builder().put("name", "val").put("type", "text").put(
        "value", "").build(), retMap);

  }

  @Test
  public void test_getTextAttributes_addNameAttributeToMap_getTemplate_notEmpty() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    DocumentReference templateDocumentReference = new DocumentReference("wikiName", "Celements",
        "TemplateName");
    context.getDoc().setTemplateDocumentReference(templateDocumentReference);
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(cellDoc);
    expect(serviceMock.getAttributeName(cellDoc, context.getDoc())).andReturn(Optional.<String>of(
        "val"));
    replayDefault();
    Map<String, String> retMap = structuredDataEditorScriptService.getTextAttributes(docRef);
    verifyDefault();
    assertEquals(3, retMap.size());
    assertEquals(ImmutableMap.<String, String>builder().put("name", "val").put("type", "text").put(
        "value", "Celements.TemplateName").build(), retMap);
  }

  @Test
  public void test_getTextAreaAttributes_throwException() throws Exception {
    Map<String, String> retMap = new LinkedHashMap<>();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getDocument(eq(docRef))).andThrow(new DocumentNotExistsException(
        docRef));

    replayDefault();
    retMap = structuredDataEditorScriptService.getTextAreaAttributes(docRef);
    verifyDefault();
    assertEquals(0, retMap.size());
  }

  @Test
  public void test_getTextAreaAttributes_TextAreaFieldEditorClass_rows_cols_notNull()
      throws Exception {
    Map<String, String> retMap = new LinkedHashMap<>();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);

    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(cellDoc);
    expect(serviceMock.getAttributeName(cellDoc, context.getDoc())).andReturn(
        Optional.<String>absent());
    expect(modelAccessMock.getProperty(cellDoc, TextAreaFieldEditorClass.FIELD_ROWS)).andReturn(2);
    expect(modelAccessMock.getProperty(cellDoc, TextAreaFieldEditorClass.FIELD_COLS)).andReturn(3);

    replayDefault();
    retMap = structuredDataEditorScriptService.getTextAreaAttributes(docRef);
    verifyDefault();
    assertEquals(2, retMap.size());
    assertEquals(ImmutableMap.<String, String>builder().put("rows", "2").put("cols", "3").build(),
        retMap);
  }

  @Test
  public void test_addAttributeToMap_Null() throws Exception {
    Map<String, String> retMap = new LinkedHashMap<>();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    expect(modelAccessMock.getProperty(cellDoc, TextAreaFieldEditorClass.FIELD_COLS)).andReturn(
        null);

    replayDefault();
    structuredDataEditorScriptService.addAttributeToMap(retMap, "Test Null", cellDoc,
        TextAreaFieldEditorClass.FIELD_COLS);
    verifyDefault();
    assertEquals(0, retMap.size());
  }

  @Test
  public void test_getTextAreaContent_throwException() throws Exception {
    String retVal = new String();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getProperty(eq(docRef), eq(
        TextAreaFieldEditorClass.FIELD_VALUE))).andThrow(new DocumentNotExistsException(docRef));
    replayDefault();
    retVal = structuredDataEditorScriptService.getTextAreaContent(docRef);
    verifyDefault();
    assertEquals("", retVal.toString());
  }

  @Test
  public void test_getTextAreaContent_try() throws Exception {
    String retVal = new String();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getProperty(eq(docRef), eq(
        TextAreaFieldEditorClass.FIELD_VALUE))).andReturn("Test");
    replayDefault();
    retVal = structuredDataEditorScriptService.getTextAreaContent(docRef);
    verifyDefault();
    assertEquals("Test", retVal.toString());
  }

  @Test
  public void test_getCellValueAsString_throwException() throws Exception {
    String retVal = new String();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(serviceMock.getCellValueAsString(eq(docRef), eq(context.getDoc()))).andThrow(
        new DocumentNotExistsException(docRef));

    replayDefault();
    retVal = structuredDataEditorScriptService.getCellValueAsString(docRef);
    verifyDefault();
    assertEquals("", retVal.toString());
  }

  @Test
  public void test_getCellValueAsString_try() throws Exception {
    String retVal = new String();
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(serviceMock.getCellValueAsString(eq(docRef), eq(context.getDoc()))).andReturn(
        Optional.<String>of("Test"));
    replayDefault();
    retVal = structuredDataEditorScriptService.getCellValueAsString(docRef);
    verifyDefault();
    assertEquals("Test", retVal.toString());
  }

  @Test
  public void test_getCellPropertyClass_throwException() throws Exception {
    Optional<com.xpn.xwiki.api.PropertyClass> propertyClass;
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    expect(modelAccessMock.getDocument(eq(docRef))).andThrow(new DocumentNotExistsException(
        docRef));
    replayDefault();
    propertyClass = structuredDataEditorScriptService.getCellPropertyClass(docRef);
    verifyDefault();
    assertTrue(!propertyClass.isPresent());
  }

  @Test
  public void test_getCellPropertyClass_try() throws Exception {
    PropertyClass propertyClass = new PropertyClass();
    Optional<com.xpn.xwiki.api.PropertyClass> propertyClass1;
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(cellDoc);
    expect(serviceMock.getCellPropertyClass(eq(cellDoc))).andReturn(Optional.<PropertyClass>of(
        propertyClass));
    replayDefault();
    propertyClass1 = structuredDataEditorScriptService.getCellPropertyClass(docRef);
    verifyDefault();
    assertTrue(propertyClass1.isPresent());

  }

}
