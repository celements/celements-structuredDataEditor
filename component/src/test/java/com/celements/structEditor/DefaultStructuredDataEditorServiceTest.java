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
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.celements.velocity.VelocityService;
import com.google.common.base.Strings;
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
        VelocityService.class, ModelContext.class);
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
    expectRequest("");
    expectComputed("");
    expectMultilingual("");

    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass();

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertFalse(obj.isPresent());
  }

  @Test
  public void test_getXObjectInStructEditor_default() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual("");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "");
    createObj(onDoc, classDocRef, "");

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_request() throws Exception {
    expectRequest("1");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "");
    createObj(onDoc, classDocRef, "");

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(1, obj.get().getNumber());
  }

  @Test
  public void test_getXObjectInStructEditor_request_invalid() throws Exception {
    expectRequest("asdf");
    expectComputed("");
    expectMultilingual("");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "");
    createObj(onDoc, classDocRef, "");

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_computed() throws Exception {
    expectRequest("");
    int expNb = 1;
    expectComputed(Integer.toString(expNb));
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "");
    createObj(onDoc, classDocRef, "");

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, expNb, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_computed_invalid() throws Exception {
    expectRequest("");
    String text = "invalid";
    expectComputed(text);
    expectMultilingual("");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "");
    createObj(onDoc, classDocRef, "");

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_lang() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual("de");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "fr");
    createObj(onDoc, classDocRef, "");
    int expNb = createObj(onDoc, classDocRef, "de").getNumber();

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, expNb, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_lang_noObjWithLang() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual("de");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "");
    createObj(onDoc, classDocRef, "");

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_multilingual_defaultLang() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual(true, "");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expect(getMock(ModelContext.class).getDefaultLanguage(onDoc.getDocumentReference()))
        .andReturn("de").atLeastOnce();
    DocumentReference classDocRef = expectClass().getDocumentReference();
    createObj(onDoc, classDocRef, "fr");
    createObj(onDoc, classDocRef, "en");
    int expNb = createObj(onDoc, classDocRef, "de").getNumber();

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(classDocRef, expNb, obj);
  }

  private void expectRequest(String nb) {
    expect(getMock(ModelContext.class).getRequestParameter("objNb"))
        .andReturn(com.google.common.base.Optional.fromNullable(Strings.emptyToNull(nb)))
        .once();
  }

  private void expectComputed(String text) throws XWikiVelocityException {
    text = Strings.emptyToNull(text);
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_COMPUTED_OBJ_NB)))
        .andReturn(com.google.common.base.Optional.fromNullable(text));
    if (text != null) {
      expect(getMock(VelocityService.class).evaluateVelocityText(text)).andReturn(text);
    }
  }

  private void expectMultilingual(String contextLang) {
    boolean isMultilingual = !Strings.nullToEmpty(contextLang).isEmpty();
    expectMultilingual(isMultilingual, contextLang);
  }

  private void expectMultilingual(boolean isMultilingual, String contextLang) {
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_MULTILINGUAL)))
        .andReturn(com.google.common.base.Optional.of(isMultilingual));
    if (isMultilingual) {
      expect(getMock(ModelContext.class).getLanguage())
          .andReturn(Optional.ofNullable(Strings.emptyToNull(contextLang)))
          .atLeastOnce();
    }
  }

  private BaseClass expectClass() {
    DocumentReference xClassDocRef = new DocumentReference(wikiName, "Celements", "TestXClassName");
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(FIELD_EDIT_FIELD_CLASS)))
        .andReturn(com.google.common.base.Optional.of(xClassDocRef)).atLeastOnce();
    XWikiDocument xClassDoc = new XWikiDocument(xClassDocRef);
    xClassDoc.setNew(false);
    expect(modelAccessMock.getOrCreateDocument(xClassDocRef)).andReturn(xClassDoc).anyTimes();
    return xClassDoc.getXClass();
  }

  private void assertObj(DocumentReference classDocRef, int expNb, Optional<BaseObject> obj) {
    assertTrue(obj.isPresent());
    assertEquals(classDocRef, obj.get().getXClassReference());
    assertEquals(expNb, obj.get().getNumber());
  }

  private static BaseObject createObj(XWikiDocument doc, DocumentReference classDocRef,
      String lang) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classDocRef);
    obj.setStringValue("lang", lang);
    doc.addXObject(obj);
    return obj;
  }

}
