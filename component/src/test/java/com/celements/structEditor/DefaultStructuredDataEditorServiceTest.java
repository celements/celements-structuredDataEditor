package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.structEditor.fields.FormFieldPageType;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class DefaultStructuredDataEditorServiceTest extends AbstractComponentTest {

  private DefaultStructuredDataEditorService service;
  private XWikiDocument cellDoc;
  private IModelAccessFacade modelAccessMock;
  private XWikiContext context;
  private String wikiName;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IPageTypeResolverRole.class);
    service = (DefaultStructuredDataEditorService) Utils.getComponent(
        StructuredDataEditorService.class);
    modelAccessMock = getMock(IModelAccessFacade.class);
    context = getContext();
    wikiName = context.getDatabase();
    cellDoc = new XWikiDocument(new DocumentReference(wikiName, "layout", "cell"));
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
    expect(getMock(IPageTypeResolverRole.class).getPageTypeRefForDoc(same(parentDoc))).andReturn(
        new PageTypeReference(FormFieldPageType.PAGETYPE_NAME, "",
            Collections.<String>emptyList())).once();
    expect(modelAccessMock.getFieldValue(same(parentDoc), same(
        FormFieldEditorClass.FIELD_PREFIX))).andReturn(Optional.of(prefix)).once();
    replayDefault();
    Optional<String> ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertTrue(ret.isPresent());
    assertEquals(prefix, ret.get());
  }

  @Test
  public void test_getXClassPrettyName() throws Exception {
    DocumentReference xClassDocRef = new DocumentReference(wikiName, "Celements", "TestXClassName");
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS))).andReturn(Optional.of(
            xClassDocRef)).once();
    XWikiDocument xClassDoc = new XWikiDocument(xClassDocRef);
    expect(modelAccessMock.getDocument(xClassDocRef)).andReturn(xClassDoc).atLeastOnce();
    String editFieldName = "edit_field";
    expect(modelAccessMock.getFieldValue(same(cellDoc), same(
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME))).andReturn(Optional.of(
            editFieldName)).once();
    BaseClass xClass = xClassDoc.getXClass();
    String thePrettyFieldName = "the Pretty Field Name";
    xClass.addTextField(editFieldName, thePrettyFieldName, 30);
    replayDefault();
    String ret = service.getXClassPrettyName(cellDoc).get();
    verifyDefault();
    assertEquals(thePrettyFieldName, ret);
  }

}
