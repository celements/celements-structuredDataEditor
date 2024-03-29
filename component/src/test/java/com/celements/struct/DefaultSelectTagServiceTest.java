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
package com.celements.struct;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.reference.RefBuilder;
import com.celements.struct.edit.autocomplete.AutocompleteRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class DefaultSelectTagServiceTest extends AbstractComponentTest {

  private DefaultSelectTagService selectTagServ;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepare() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    selectTagServ = (DefaultSelectTagService) Utils.getComponent(SelectTagServiceRole.class);
  }

  @Test
  public void test_getTypeImpl_configMissing() throws Exception {
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myTestDoc").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    replayDefault();
    Optional<AutocompleteRole> typeImpl = selectTagServ.getTypeImpl(cellDocRef);
    assertFalse(typeImpl.isPresent());
    verifyDefault();
  }

  @Test
  public void test_getTypeImpl_config_notSet() throws Exception {
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myTestDoc").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    addXObject(cellDoc, SelectTagAutocompleteEditorClass.CLASS_REF);
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    replayDefault();
    Optional<AutocompleteRole> typeImpl = selectTagServ.getTypeImpl(cellDocRef);
    assertFalse(typeImpl.isPresent());
    verifyDefault();
  }

  @Test
  public void test_getSelectCellDocRef() throws Exception {
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myTestPage").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    expect(modelAccessMock.streamParents(cellDoc)).andReturn(Stream.empty());
    expect(modelAccessMock.getDocument(eq(cellDocRef))).andReturn(cellDoc);
    replayDefault();
    Optional<DocumentReference> selectCellDocRef = selectTagServ.getSelectCellDocRef(cellDocRef);
    assertFalse(selectCellDocRef.isPresent());
    verifyDefault();
  }

  private BaseObject addXObject(XWikiDocument doc, ClassReference classRef) {
    final BaseObject xObj = new BaseObject();
    xObj.setDocumentReference(doc.getDocumentReference());
    xObj.setXClassReference(classRef.getDocRef(doc.getDocumentReference().getWikiReference()));
    return xObj;
  }

}
