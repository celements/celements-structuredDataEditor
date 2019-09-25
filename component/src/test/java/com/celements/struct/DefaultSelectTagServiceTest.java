package com.celements.struct;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.reference.RefBuilder;
import com.celements.structEditor.SelectAutocompleteRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class DefaultSelectTagServiceTest extends AbstractComponentTest {

  private DefaultSelectTagService selectTagServ;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_DefaultSelectTagServiceTest() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    selectTagServ = (DefaultSelectTagService) Utils.getComponent(SelectTagServiceRole.class);
  }

  @Test
  public void testGetTypeImpl_configMissing() throws Exception {
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myTestDoc").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    replayDefault();
    Optional<SelectAutocompleteRole> typeImpl = selectTagServ.getTypeImpl(cellDocRef);
    assertFalse(typeImpl.isPresent());
    verifyDefault();
  }

  @Test
  public void testGetTypeImpl_config_notSet() throws Exception {
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myTestDoc").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    final BaseObject selectConfigObj = addXObject(cellDoc,
        SelectTagAutocompleteEditorClass.CLASS_DEF_HINT);
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    expect(modelAccessMock.getFieldValue(eq(selectConfigObj), eq(
        SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE))).andReturn(
            com.google.common.base.Optional.fromNullable(Collections.emptyList()));
    replayDefault();
    Optional<SelectAutocompleteRole> typeImpl = selectTagServ.getTypeImpl(cellDocRef);
    assertFalse(typeImpl.isPresent());
    verifyDefault();
  }

  @Test
  public void testGetSelectCellDocRef() throws Exception {
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myTestPage").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    replayDefault();
    Optional<DocumentReference> selectCellDocRef = selectTagServ.getSelectCellDocRef(cellDocRef);
    assertFalse(selectCellDocRef.isPresent());
    verifyDefault();
  }

  private BaseObject addXObject(XWikiDocument doc, String hint) {
    ClassDefinition classDef = Utils.getComponent(ClassDefinition.class, hint);
    final BaseObject xObj = new BaseObject();
    xObj.setDocumentReference(doc.getDocumentReference());
    xObj.setXClassReference(classDef.getClassReference());
    return xObj;
  }

}
