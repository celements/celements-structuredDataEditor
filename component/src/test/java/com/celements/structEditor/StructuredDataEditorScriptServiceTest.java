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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class StructuredDataEditorScriptServiceTest extends AbstractComponentTest {

  private StructuredDataEditorScriptService structuredDataEditorScriptService;
  private StructuredDataEditorService serviceMock;
  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private String wikiName;

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
    retMap = structuredDataEditorScriptService.getTextAreaAttributes(docRef);
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
    expect(modelAccessMock.getDocument(eq(docRef))).andReturn(cellDoc);
    expect(serviceMock.getAttributeName(cellDoc, context.getDoc())).andReturn(Optional.<String>of(
        "val"));
    expect(serviceMock.getAttributeName(cellDoc, context.getDoc())).andReturn(Optional.<String>of(
        "val2"));
    replayDefault();
    Map<String, String> retMap = structuredDataEditorScriptService.getTextAttributes(docRef);
    verifyDefault();
    assertEquals(2, retMap.size());
    assertEquals(ImmutableMap.<String, String>builder().put("name", "val").put("type", "text").put(
        "value", "val2").build(), retMap);
  }

}
