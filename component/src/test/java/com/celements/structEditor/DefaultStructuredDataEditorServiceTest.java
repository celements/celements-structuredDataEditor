package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.structEditor.classes.StructuredDataEditorClass.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.velocity.VelocityService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class DefaultStructuredDataEditorServiceTest extends AbstractComponentTest {

  private DefaultStructuredDataEditorService service;
  private XWikiDocument cellDoc;
  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private String wikiName;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IPageTypeResolverRole.class,
        VelocityService.class);
    service = (DefaultStructuredDataEditorService) Utils.getComponent(
        StructuredDataEditorService.class);
    modelAccessMock = getMock(IModelAccessFacade.class);
    context = getContext();
    wikiName = context.getDatabase();
    cellDoc = new XWikiDocument(new DocumentReference(wikiName, "layout", "cell"));
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
  }

  @Test
  public void test_resolveFormPrefix_null() throws Exception {
    replayDefault();
    Optional<String> ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_resolveFormPrefix() throws Exception {
    String prefix = "prefix";
    XWikiDocument parentDoc = new XWikiDocument(new DocumentReference(wikiName, "layout",
        "parent"));
    cellDoc.setParentReference((EntityReference) parentDoc.getDocumentReference());
    expect(modelAccessMock.getDocument(eq(parentDoc.getDocumentReference()))).andReturn(
        parentDoc).once();
    final PageTypeReference ptRef = new PageTypeReference(FormFieldPageType.PAGETYPE_NAME, "",
        Collections.<String>emptyList());
    expect(getMock(IPageTypeResolverRole.class).resolvePageTypeReference(same(
        parentDoc))).andReturn(com.google.common.base.Optional.of(ptRef)).once();
    expect(modelAccessMock.getFieldValue(same(parentDoc), same(FormFieldEditorClass.FIELD_PREFIX)))
        .andReturn(com.google.common.base.Optional.of(prefix)).once();
    replayDefault();
    Optional<String> ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertTrue(ret.isPresent());
    assertEquals(prefix, ret.get());
  }

  @Test
  public void test_getXClassPrettyName() throws Exception {
    String editFieldName = "edit_field";
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_EDIT_FIELD_NAME)))
        .andReturn(com.google.common.base.Optional.of(editFieldName)).once();
    BaseClass xClass = expectClass();
    String thePrettyFieldName = "the Pretty Field Name";
    xClass.addTextField(editFieldName, thePrettyFieldName, 30);
    replayDefault();
    String ret = service.getXClassPrettyName(cellDoc).get();
    verifyDefault();
    assertEquals(thePrettyFieldName, ret);
  }

  @Test
  public void test_getXObjectInStructEditor_none() throws Exception {
    expect(getContext().getRequest().get("objNb")).andReturn("");
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_COMPUTED_OBJ_NB)))
        .andReturn(com.google.common.base.Optional.absent());
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass();

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertFalse(obj.isPresent());
  }

  @Test
  public void test_getXObjectInStructEditor_default() throws Exception {
    expect(getContext().getRequest().get("objNb")).andReturn("");
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_COMPUTED_OBJ_NB)))
        .andReturn(com.google.common.base.Optional.absent());
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef);
    createObj(onDoc, classDocRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(0, obj.get().getNumber());
  }

  @Test
  public void test_getXObjectInStructEditor_request() throws Exception {
    expect(getContext().getRequest().get("objNb")).andReturn("1");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef);
    createObj(onDoc, classDocRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(1, obj.get().getNumber());
  }

  @Test
  public void test_getXObjectInStructEditor_request_invalid() throws Exception {
    expect(getContext().getRequest().get("objNb")).andReturn("asdf");
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_COMPUTED_OBJ_NB)))
        .andReturn(com.google.common.base.Optional.absent());
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef);
    createObj(onDoc, classDocRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(0, obj.get().getNumber());
  }

  @Test
  public void test_getXObjectInStructEditor_computed() throws Exception {
    expect(getContext().getRequest().get("objNb")).andReturn("");
    String text = "1";
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_COMPUTED_OBJ_NB)))
        .andReturn(com.google.common.base.Optional.of(text));
    expect(getMock(VelocityService.class).evaluateVelocityText(text)).andReturn(text);
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef);
    createObj(onDoc, classDocRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(1, obj.get().getNumber());
  }

  @Test
  public void test_getXObjectInStructEditor_computed_invalid() throws Exception {
    expect(getContext().getRequest().get("objNb")).andReturn("");
    String text = "invalid";
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_COMPUTED_OBJ_NB)))
        .andReturn(com.google.common.base.Optional.of(text));
    expect(getMock(VelocityService.class).evaluateVelocityText(text)).andReturn(text);
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef);
    createObj(onDoc, classDocRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(0, obj.get().getNumber());
  }

  private BaseClass expectClass() throws DocumentNotExistsException {
    DocumentReference xClassDocRef = new DocumentReference(wikiName, "Celements", "TestXClassName");
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_EDIT_FIELD_CLASS)))
        .andReturn(com.google.common.base.Optional.of(xClassDocRef)).once();
    XWikiDocument xClassDoc = new XWikiDocument(xClassDocRef);
    expect(modelAccessMock.getDocument(xClassDocRef)).andReturn(xClassDoc).anyTimes();
    return xClassDoc.getXClass();
  }

  private static BaseObject createObj(XWikiDocument doc, DocumentReference classDocRef) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classDocRef);
    doc.addXObject(obj);
    return obj;
  }

}
