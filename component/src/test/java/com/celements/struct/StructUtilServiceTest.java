package com.celements.struct;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;

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
        "myTestSpace").doc("myDocName").build(ImmutableDocumentReference.class);
    XWikiDocument cellDoc = new XWikiDocument(cellDocRef);
    replayDefault();
    Optional<XWikiDocument> parentCell = structUtils.findParentCell(cellDoc, ptName);
    assertFalse(parentCell.isPresent());
    verifyDefault();
  }

}
