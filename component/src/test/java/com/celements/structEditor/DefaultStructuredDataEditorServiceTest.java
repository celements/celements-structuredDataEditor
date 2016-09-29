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
import com.celements.structEditor.fields.FormFieldPageType;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DefaultStructuredDataEditorServiceTest extends AbstractComponentTest {

  private DefaultStructuredDataEditorService service;
  private XWikiDocument cellDoc;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, IPageTypeResolverRole.class);
    service = (DefaultStructuredDataEditorService) Utils.getComponent(
        StructuredDataEditorService.class);
    cellDoc = new XWikiDocument(new DocumentReference("wiki", "layout", "cell"));
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
    XWikiDocument parentDoc = new XWikiDocument(new DocumentReference("wiki", "layout", "parent"));
    cellDoc.setParentReference((EntityReference) parentDoc.getDocumentReference());
    expect(getMock(IModelAccessFacade.class).getDocument(eq(
        parentDoc.getDocumentReference()))).andReturn(parentDoc).once();
    expect(getMock(IPageTypeResolverRole.class).getPageTypeRefForDoc(same(parentDoc))).andReturn(
        new PageTypeReference(FormFieldPageType.PAGETYPE_NAME, "",
            Collections.<String>emptyList())).once();
    expect(getMock(IModelAccessFacade.class).getProperty(same(parentDoc), same(
        FormFieldEditorClass.FIELD_PREFIX))).andReturn(prefix).once();
    replayDefault();
    String ret = service.resolveFormPrefix(cellDoc);
    verifyDefault();
    assertEquals(prefix, ret);
  }

  @Test
  public void test_getXClassPrettyName() throws Exception {
    String prettyName = "";
    replayDefault();
    String ret = service.getXClassPrettyName(cellDoc);
    verifyDefault();
    assertEquals(prettyName, ret);
  }

}
