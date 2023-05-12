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
package com.celements.structEditor.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DisplayFieldPageTypeTest extends AbstractComponentTest {

  private DisplayFieldPageType pageType;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    pageType = (DisplayFieldPageType) Utils.getComponent(IJavaPageTypeRole.class,
        DisplayFieldPageType.PAGETYPE_NAME);
    getContext().setDoc(new XWikiDocument(new DocumentReference(
        getContext().getDatabase(), "Content", "Current")));
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals("DisplayField", pageType.getName());
    verifyDefault();
  }

  @Test
  public void test_getViewTemplateName() {
    replayDefault();
    assertEquals("DisplayFieldView", pageType.getViewTemplateName());
    verifyDefault();
  }

  @Test
  public void test_tagName() {
    replayDefault();
    Optional<String> name = pageType.tagName();
    verifyDefault();
    assertFalse(name.isPresent());
  }

}
