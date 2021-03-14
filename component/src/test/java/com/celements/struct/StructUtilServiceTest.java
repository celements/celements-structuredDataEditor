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
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.reference.RefBuilder;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class StructUtilServiceTest extends AbstractComponentTest {

  private StructUtilService structUtils;

  @Before
  public void setUp_StructUtilServiceTest() throws Exception {
    structUtils = (StructUtilService) Utils.getComponent(StructUtilServiceRole.class);
  }

  @Test
  public void testFindParentCell_parentEmpty() throws Exception {
    String ptName = "SelectTag";
    DocumentReference cellDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "myTestSpace").doc("myDocName").build(DocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    replayDefault();
    Optional<XWikiDocument> parentCell = structUtils.findParentCell(cellDoc, ptName);
    assertFalse(parentCell.isPresent());
    verifyDefault();
  }

}
