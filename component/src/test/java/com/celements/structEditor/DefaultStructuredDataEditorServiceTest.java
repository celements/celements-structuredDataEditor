package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class DefaultStructuredDataEditorServiceTest extends AbstractComponentTest {

  private DefaultStructuredDataEditorService service;
  private XWikiDocument cellDoc;
  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private String wikiName;
  ModelUtils modelutils;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IPageTypeResolverRole.class);
    service = (DefaultStructuredDataEditorService) Utils.getComponent(
        StructuredDataEditorService.class);
    modelutils = Utils.getComponent(ModelUtils.class);
    modelAccessMock = getMock(IModelAccessFacade.class);
    context = getContext();
    wikiName = context.getDatabase();
    cellDoc = new XWikiDocument(new DocumentReference(wikiName, "layout", "cell"));

  }

  @Test
  public void test_resolveFormPrefix_null() throws Exception {
    String prefix = null;
    replayDefault();
    String ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertEquals(prefix, ret);
  }

  @Test
  public void test_resolveFormPrefix() throws Exception {
    String prefix = "prefix";
    XWikiDocument parentDoc = new XWikiDocument(new DocumentReference(wikiName, "layout",
        "parent"));
    cellDoc.setParentReference((EntityReference) parentDoc.getDocumentReference());
    expect(modelAccessMock.getDocument(eq(parentDoc.getDocumentReference()))).andReturn(
        parentDoc).once();
    expect(getMock(IPageTypeResolverRole.class).getPageTypeRefForDoc(same(parentDoc))).andReturn(
        new PageTypeReference(FormFieldPageType.PAGETYPE_NAME, "",
            Collections.<String>emptyList())).once();
    expect(modelAccessMock.getProperty(same(parentDoc), same(
        FormFieldEditorClass.FIELD_PREFIX))).andReturn(prefix).once();
    replayDefault();
    String ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertEquals(prefix, ret);
  }

  // @Test
  // public void test_resolveFormPrefix_DocumentNotExists() throws Exception {
  // String prefix = null;
  // XWikiDocument parentDoc = new XWikiDocument(new DocumentReference(wikiName, "layout",
  // "parent"));
  // DocumentReference xClassDocRef = new DocumentReference(wikiName, "Celements",
  // "TestXClassName");
  // expect(modelAccessMock.getDocument(xClassDocRef)).andThrow(new DocumentNotExistsException(
  // xClassDocRef)).once();

  // replayDefault();
  // String ret = service.resolveFormPrefix(parentDoc);
  // verifyDefault();
  // assertEquals(prefix, ret);
  // }

  @Test
  public void test_getXClassPrettyName() throws Exception {
    String fieldXClassName = "Celements.TestXClassName";
    expect(modelAccessMock.getProperty(same(cellDoc), same(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME))).andReturn(fieldXClassName).once();
    DocumentReference xClassDocRef = new DocumentReference(wikiName, "Celements", "TestXClassName");
    XWikiDocument xClassDoc = new XWikiDocument(xClassDocRef);
    expect(modelAccessMock.getDocument(xClassDocRef)).andReturn(xClassDoc).atLeastOnce();
    String editFieldName = "edit_field";
    expect(modelAccessMock.getProperty(same(cellDoc), same(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME))).andReturn(editFieldName).once();
    BaseClass xClass = xClassDoc.getXClass();
    String thePrettyFieldName = "the Pretty Field Name";
    xClass.addTextField(editFieldName, thePrettyFieldName, 30);
    replayDefault();
    String ret = service.getXClassPrettyName(cellDoc).get();
    verifyDefault();
    assertEquals(thePrettyFieldName, ret);
  }

  @Test
  public void test_getAttributeNameInternal_fieldNameNull() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    XWikiDocument onDoc = null;
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn(null);
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn(null);

    Optional<String> name;
    Optional<String> expectedName = Optional.<String>absent();
    replayDefault();
    name = service.getAttributeNameInternal(cellDoc, onDoc);
    verifyDefault();
    assertEquals(expectedName, name);
  }

  @Test
  public void test_getAttributeNameInternal_fieldName_classRefNotPresent() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument onDoc = null;
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn(null).once();
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn("Test").times(2);
    Optional<String> name;
    String testString = "Test";
    Optional<String> ostr = Optional.fromNullable(testString);

    replayDefault();
    name = service.getAttributeNameInternal(cellDoc, onDoc);
    verifyDefault();
    assertEquals(ostr, name);
  }

  @Test
  public void test_getAttributeNameInternal_fieldName_classRef() throws Exception {
    DocumentReference docRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(docRef);
    XWikiDocument onDoc = null;
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn("className").once();

    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn("Test").times(2);
    Optional<String> name;
    String testString1 = "className";
    String testString2 = "Test";
    Optional<String> ostr = Optional.fromNullable("Celements." + testString1 + "_" + testString2);

    replayDefault();
    name = service.getAttributeNameInternal(cellDoc, onDoc);
    verifyDefault();
    assertEquals(ostr, name);
  }

  @Test
  public void test_getAttributeNameInternal_fieldName_classRef_BaseObjectNull() throws Exception {
    DocumentReference classRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference("wikiName", "Celements",
        "cell"));
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference("wikiName", "Celements", "on"));
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn(modelutils.serializeRef(
            classRef)).once();
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn("Test").times(2);
    expect(modelAccessMock.getXObject(onDoc, classRef)).andReturn(null);
    List<String> nameParts = new ArrayList<>();
    nameParts.add("-1");
    String testString1 = "TestXClassName";
    String testString2 = "Test";
    Optional<String> ostr = Optional.fromNullable("Celements." + testString1 + "_" + nameParts.get(
        0) + "_" + testString2);
    Optional<String> name;

    replayDefault();
    name = service.getAttributeNameInternal(cellDoc, onDoc);
    verifyDefault();
    assertEquals(ostr, name);

  }

  @Test
  public void test_getAttributeNameInternal_fieldName_classRef_BaseObjectNotNull()
      throws Exception {
    DocumentReference classRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference("wikiName", "Celements",
        "cell"));
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference("wikiName", "Celements", "on"));
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classRef);
    obj.setNumber(7);
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn(modelutils.serializeRef(
            classRef)).once();
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn("Test").times(2);
    expect(modelAccessMock.getXObject(onDoc, classRef)).andReturn(obj);
    List<String> nameParts = new ArrayList<>();
    nameParts.add("7");
    String testString1 = "TestXClassName";
    String testString2 = "Test";
    Optional<String> ostr = Optional.fromNullable("Celements." + testString1 + "_" + nameParts.get(
        0) + "_" + testString2);
    Optional<String> name;

    replayDefault();
    name = service.getAttributeNameInternal(cellDoc, onDoc);
    verifyDefault();
    assertEquals(ostr, name);

  }

  @Test
  public void test_getDateFormatFromField_classRefNotPresent_fieldNameNotPresent()
      throws Exception {
    DocumentReference classRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference("wikiName", "Celements",
        "cell"));
    expect(modelAccessMock.getDocument(classRef)).andReturn(cellDoc);
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn(null).times(1);
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn(null).times(1);

    Optional<String> name;
    replayDefault();
    name = service.getDateFormatFromField(classRef);
    verifyDefault();

    assertEquals(Optional.absent(), name);

  }

  @Test
  public void test_getDateFormatFromField_classRefPresent_fieldNameNotPresent() throws Exception {
    DocumentReference classRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(new DocumentReference("wikiName", "Celements",
        "cell"));
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME)).andReturn(modelutils.serializeRef(
            classRef)).times(1);
    expect(modelAccessMock.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)).andReturn("fieldTest").times(1);
    expect(modelAccessMock.getDocument(classRef)).andReturn(cellDoc).times(2);
    BaseClass xClass = cellDoc.getXClass();
    String thePrettyFieldName = "the Pretty Field Name";
    xClass.addDateField("fieldTest", thePrettyFieldName, "dd.MM.yyyy");
    replayDefault();
    Optional<String> name = service.getDateFormatFromField(classRef);
    verifyDefault();
    assertEquals("dd.MM.yyyy", name.get());
  }

  @Test
  public void test_getCellValueAsString_valueIsNull() throws Exception {
    DocumentReference celldocRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    XWikiDocument indoc = new XWikiDocument(new DocumentReference("wikiName", "Celements", "cell"));
    XWikiDocument ondoc = new XWikiDocument(new DocumentReference("wikiName1", "Celements1",
        "cell1"));
    expect(modelAccessMock.getDocument(celldocRef)).andReturn(indoc);
    expect(modelAccessMock.getProperty(eq(indoc), eq(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME))).andReturn(null);
    replayDefault();
    Optional<String> name = service.getCellValueAsString(celldocRef, ondoc);
    verifyDefault();
    assertEquals(Optional.absent(), name);
  }

  @Test
  public void test_getCellValueAsString_valueNotNull() throws Exception {
    DocumentReference celldocRef = new DocumentReference("wikiName", "Celements", "TestXClassName");
    DocumentReference cellClassRef = new DocumentReference("wikiName", "Celements",
        "TestXClassName");
    XWikiDocument cellDoc = new XWikiDocument(celldocRef);
    XWikiDocument onDoc = new XWikiDocument(new DocumentReference("wikiName1", "Celements1",
        "cell1"));
    expect(modelAccessMock.getDocument(celldocRef)).andReturn(cellDoc);
    expect(modelAccessMock.getProperty(eq(cellDoc), eq(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME))).andReturn("fieldName");
    expect(modelAccessMock.getProperty(eq(cellDoc), eq(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME))).andReturn(modelutils.serializeRef(
            cellClassRef));
    expect(modelAccessMock.getProperty(eq(onDoc), eq(celldocRef), eq("fieldName"))).andReturn(
        "12/12/2012");

    replayDefault();
    Optional<String> name = service.getCellValueAsString(celldocRef, onDoc);
    verifyDefault();
    assertEquals(Optional.of("12/12/2012"), name);
  }
}
