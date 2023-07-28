/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.structEditor.StructuredDataEditorService.*;
import static com.celements.structEditor.classes.StructuredDataEditorClass.*;
import static com.google.common.base.Strings.*;
import static java.util.stream.Collectors.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.velocity.VelocityService;
import com.celements.web.classes.KeyValueClass;
import com.google.common.primitives.Ints;
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
  private ClassReference testClassRef;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IPageTypeResolverRole.class,
        VelocityService.class, ModelContext.class);
    service = (DefaultStructuredDataEditorService) Utils.getComponent(
        StructuredDataEditorService.class);
    modelAccessMock = getMock(IModelAccessFacade.class);
    context = getContext();
    wikiName = context.getDatabase();
    testClassRef = new ClassReference("Celements", "TestXClassName");
    cellDoc = new XWikiDocument(new DocumentReference(wikiName, "layout", "cell"));
    getContext().setRequest(createMockAndAddToDefault(XWikiRequest.class));
    expect(getMock(ModelContext.class).getWikiRef())
        .andReturn(new WikiReference(wikiName)).anyTimes();
  }

  @Test
  public void test_resolveFormPrefix_null() throws Exception {
    expect(modelAccessMock.streamParents(cellDoc)).andReturn(Stream.empty());
    replayDefault();
    Optional<String> ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_resolveFormPrefix() throws Exception {
    String prefix = "prefix";
    XWikiDocument parentDoc = new XWikiDocument(
        new DocumentReference(wikiName, "layout", "parent"));
    createObj(parentDoc, FormFieldEditorClass.CLASS_REF)
        .setStringValue(FormFieldEditorClass.FIELD_PREFIX.getName(), prefix);
    expect(modelAccessMock.streamParents(cellDoc)).andReturn(Stream.of(parentDoc));
    replayDefault();
    Optional<String> ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertTrue(ret.isPresent());
    assertEquals(prefix, ret.get());
  }

  @Test
  public void test_getXClassPrettyName() throws Exception {
    String editFieldName = "edit_field";
    createObj(cellDoc, CLASS_REF)
        .setStringValue(FIELD_EDIT_FIELD_NAME.getName(), editFieldName);
    BaseClass xClass = expectClass(testClassRef);
    String thePrettyFieldName = "the Pretty Field Name";
    xClass.addTextField(editFieldName, thePrettyFieldName, 30);
    replayDefault();
    String ret = service.getXClassPrettyName(cellDoc).get();
    verifyDefault();
    assertEquals(thePrettyFieldName, ret);
  }

  @Test
  public void test_getCellValue_BooleanClass() throws Exception {
    String fieldName = "myfield";
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    BaseObject structObj = createObj(cellDoc, CLASS_REF);
    structObj.setStringValue(FIELD_EDIT_FIELD_CLASS.getName(), testClassRef.serialize());
    structObj.setStringValue(FIELD_EDIT_FIELD_NAME.getName(), fieldName);
    expectClass(testClassRef);
    BaseObject obj1 = createObj(onDoc, testClassRef);
    obj1.setIntValue(fieldName, 1);
    expectRequest("");

    replayDefault();
    assertEquals(1, service.getCellValue(cellDoc, onDoc).orElse(null));
    verifyDefault();
  }

  @Test
  public void test_getCellValueAsString_BooleanClass() throws Exception {
    String fieldName = "myfield";
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    BaseObject structObj = createObj(cellDoc, CLASS_REF);
    structObj.setStringValue(FIELD_EDIT_FIELD_CLASS.getName(), testClassRef.serialize());
    structObj.setStringValue(FIELD_EDIT_FIELD_NAME.getName(), fieldName);
    expectClass(testClassRef);
    BaseObject obj1 = createObj(onDoc, testClassRef);
    obj1.setIntValue(fieldName, 1);
    expectRequest("");

    replayDefault();
    assertEquals("1", service.getCellValueAsString(cellDoc, onDoc).orElse(null));
    verifyDefault();
  }

  @Test
  public void test_getXObjectInStructEditor_none() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual("");

    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);

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
    expectClass(testClassRef);
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_request() throws Exception {
    expectRequest("1");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    DocumentReference classDocRef = expectClass(testClassRef).getDocumentReference();
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef);

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
    expectClass(testClassRef);
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_computed() throws Exception {
    expectRequest("");
    int expNb = 1;
    expectComputed(Integer.toString(expNb));
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, expNb, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_computed_invalid() throws Exception {
    expectRequest("");
    String text = "invalid";
    expectComputed(text);
    expectMultilingual("");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, 0, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_lang() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual("de");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);
    createObj(onDoc, testClassRef, "fr");
    createObj(onDoc, testClassRef);
    int expNb = createObj(onDoc, testClassRef, "de").getNumber();

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, expNb, obj);
  }

  @Test
  public void test_getXObjectInStructEditor_lang_noObjWithLang() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual("de");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef);

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, 0, obj); // no obj matches the ctxlang, thus we expect the first obj as
                                     // fallback to read values from
  }

  @Test
  public void test_getXObjectInStructEditor_multilingual_defaultLang() throws Exception {
    expectRequest("");
    expectComputed("");
    expectMultilingual(true, "");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expect(getMock(ModelContext.class).getDefaultLanguage(onDoc.getDocumentReference()))
        .andReturn("de").atLeastOnce();
    expectClass(testClassRef);
    createObj(onDoc, testClassRef, "fr");
    createObj(onDoc, testClassRef, "en");
    int expNb = createObj(onDoc, testClassRef, "de").getNumber();

    replayDefault();
    Optional<BaseObject> obj = service.getXObjectInStructEditor(cellDoc, onDoc);
    verifyDefault();

    assertObj(testClassRef, expNb, obj);
  }

  @Test
  public void test_newXObjFetcher_byKeyValue_and() throws Exception {
    expectMultilingual(false, "");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);
    createKeyValue(onDoc, "myfield1", "myvalue", LABELS_AND.stream().findFirst());
    createKeyValue(onDoc, "myfield2", "myvalue", LABELS_AND.stream().findFirst());
    expectVelocityEval("myfield1", "myfield2", "myvalue");
    createObj(onDoc, testClassRef);
    createObj(onDoc, testClassRef).setStringValue("myfield1", "myvalue");
    BaseObject obj = createObj(onDoc, testClassRef);
    obj.setStringValue("myfield1", "myvalue");
    obj.setStringValue("myfield2", "myvalue");
    createObj(onDoc, testClassRef).setStringValue("myfield2", "myvalue");
    createObj(onDoc, testClassRef);

    replayDefault();
    List<BaseObject> objs = service.newXObjFetcher(cellDoc, onDoc).stream().collect(toList());
    verifyDefault();

    assertEquals(1, objs.size());
    assertObj(testClassRef, obj.getNumber(), Optional.of(objs.get(0)));
  }

  @Test
  public void test_newXObjFetcher_byKeyValue_or() throws Exception {
    expectMultilingual(false, "");
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference(wikiName, "some", "doc"));
    expectClass(testClassRef);
    createKeyValue(onDoc, "myfield1", "myvalue", LABELS_OR.stream().findFirst());
    createKeyValue(onDoc, "myfield2", "myvalue", LABELS_OR.stream().findFirst());
    expectVelocityEval("myfield1", "myfield2", "myvalue");
    createObj(onDoc, testClassRef);
    BaseObject obj1 = createObj(onDoc, testClassRef);
    obj1.setStringValue("myfield1", "myvalue");
    BaseObject obj2 = createObj(onDoc, testClassRef);
    obj2.setStringValue("myfield1", "myvalue");
    obj2.setStringValue("myfield2", "myvalue");
    BaseObject obj3 = createObj(onDoc, testClassRef);
    obj3.setStringValue("myfield2", "myvalue");
    createObj(onDoc, testClassRef);

    replayDefault();
    List<BaseObject> objs = service.newXObjFetcher(cellDoc, onDoc).stream().collect(toList());
    verifyDefault();

    assertEquals(3, objs.size());
    assertObj(testClassRef, obj1.getNumber(), Optional.of(objs.get(0)));
    assertObj(testClassRef, obj2.getNumber(), Optional.of(objs.get(1)));
    assertObj(testClassRef, obj3.getNumber(), Optional.of(objs.get(2)));
  }

  private void createKeyValue(XWikiDocument doc, String key, String value, Optional<String> label) {
    BaseObject kvObj1 = createObj(cellDoc, KeyValueClass.CLASS_REF);
    label.ifPresent(l -> kvObj1.setStringValue("label", l));
    kvObj1.setStringValue("key", key);
    kvObj1.setStringValue("value", value);
  }

  private void expectRequest(String nb) {
    Optional<String> ret = Optional.ofNullable(emptyToNull(nb));
    expect(getMock(ModelContext.class).getRequestParam("objNb"))
        .andReturn(ret);
    if (Ints.tryParse(nb) == null) {
      expect(getMock(ModelContext.class).getRequestParam("objNb_" + testClassRef.serialize()))
          .andReturn(ret).atLeastOnce();
    }
  }

  private void expectComputed(String text) throws XWikiVelocityException {
    text = emptyToNull(text);
    createObj(cellDoc, CLASS_REF)
        .setStringValue(FIELD_COMPUTED_OBJ_NB.getName(), text);
    if (text != null) {
      expectVelocityEval(text);
    }
  }

  private void expectVelocityEval(String... text) throws XWikiVelocityException {
    for (String t : text) {
      expect(getMock(VelocityService.class).evaluateVelocityText(t))
          .andReturn(t)
          .atLeastOnce();
    }
  }

  private void expectMultilingual(String contextLang) {
    boolean isMultilingual = !nullToEmpty(contextLang).isEmpty();
    expectMultilingual(isMultilingual, contextLang);
  }

  private void expectMultilingual(boolean isMultilingual, String contextLang) {
    createObj(cellDoc, CLASS_REF)
        .setIntValue(FIELD_MULTILINGUAL.getName(), isMultilingual ? 1 : 0);
    if (isMultilingual) {
      expect(getMock(ModelContext.class).getLanguage())
          .andReturn(Optional.ofNullable(emptyToNull(contextLang)))
          .atLeastOnce();
    }
  }

  private BaseClass expectClass(ClassReference classRef) {
    createObj(cellDoc, CLASS_REF)
        .setStringValue(FIELD_EDIT_FIELD_CLASS.getName(), classRef.serialize());
    XWikiDocument xClassDoc = new XWikiDocument(classRef.getDocRef(new WikiReference(wikiName)));
    xClassDoc.setNew(false);
    expect(modelAccessMock.getOrCreateDocument(xClassDoc.getDocumentReference()))
        .andReturn(xClassDoc).anyTimes();
    return xClassDoc.getXClass();
  }

  private void assertObj(ClassReference classRef, int expNb, Optional<BaseObject> obj) {
    assertTrue(obj.isPresent());
    assertEquals(classRef, new ClassReference(obj.get().getXClassReference()));
    assertEquals(expNb, obj.get().getNumber());
  }

  private BaseObject createObj(XWikiDocument doc, ClassReference classRef, String lang) {
    BaseObject obj = createObj(doc, classRef);
    obj.setStringValue("lang", lang);
    return obj;
  }

  private BaseObject createObj(XWikiDocument doc, ClassReference classRef) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef.getDocRef(doc.getDocumentReference().getWikiReference()));
    doc.addXObject(obj);
    return obj;
  }

}
