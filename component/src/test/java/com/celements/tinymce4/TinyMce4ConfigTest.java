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
package com.celements.tinymce4;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.reference.RefBuilder;
import com.celements.rteConfig.RteConfigRole;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.web.Utils;

public class TinyMce4ConfigTest extends AbstractComponentTest {

  private RteConfigRole rteConfigMock;
  private TinyMce4Config tinyMce4Config;

  @Before
  public void setUp_TinyMce4ConfigTest() throws Exception {
    rteConfigMock = registerComponentMock(RteConfigRole.class);
    tinyMce4Config = (TinyMce4Config) Utils.getComponent(RteConfigRole.class, TinyMce4Config.HINT);
  }

  @Test
  public void test_getRTEConfigsList() {
    final DocumentReference testRteConfDocRef1 = new RefBuilder().wiki(
        getContext().getDatabase()).space("RteConfigs").doc("TestConfig1").build(
            DocumentReference.class);
    final DocumentReference testRteConfDocRef2 = new RefBuilder().with(testRteConfDocRef1).doc(
        "TestConfig2").build(DocumentReference.class);
    final List<DocumentReference> expectedConfigDocList = ImmutableList.of(testRteConfDocRef1,
        testRteConfDocRef2);
    expect(rteConfigMock.getRTEConfigsList()).andReturn(expectedConfigDocList);
    replayDefault();
    assertEquals(expectedConfigDocList, tinyMce4Config.getRTEConfigsList());
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_anyField() {
    final String testPropName = "testProperty";
    final String expectedResult = "the|Expected|Result";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn(expectedResult);
    replayDefault();
    assertEquals(expectedResult, tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_row_1() {
    final String testPropName = "row_1";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("the,Expected,Result");
    replayDefault();
    assertEquals("the Expected Result", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_row_2() {
    final String testPropName = "row_2";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("the,Expected,Result");
    replayDefault();
    assertEquals("the Expected Result", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_valid_elements() {
    final String testPropName = "valid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("#p,a[!href],br");
    replayDefault();
    assertEquals("#p,a[!href],br", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_valid_elements_default() {
    final String testPropName = "valid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("");
    replayDefault();
    assertEquals(TinyMce4Config.VALID_ELEMENTS_DEF, tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_invalid_elements() {
    final String testPropName = "invalid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("#p,a[!href],br");
    replayDefault();
    assertEquals("#p,a[!href],br", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_invalid_elements_default() {
    final String testPropName = "invalid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("");
    replayDefault();
    assertEquals(TinyMce4Config.INVALID_ELEMENTS_DEF, tinyMce4Config.getRTEConfigField(
        testPropName));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_trailingSeparator() {
    replayDefault();
    assertEquals("list | bold italic celimage cellink", tinyMce4Config.rowLayoutConvert(
        "|, list,|,bold,italic,image,link,separator"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_doubleSeparator() {
    replayDefault();
    assertEquals("list | bold italic celimage cellink", tinyMce4Config.rowLayoutConvert(
        "list,separator,|,bold,italic,image,link"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_skip_empty_elements() {
    replayDefault();
    assertEquals("list | bold italic", tinyMce4Config.rowLayoutConvert(
        "list,separator,,|,,bold,italic,"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_skip_additional_spaces() {
    replayDefault();
    assertEquals("list | bold italic", tinyMce4Config.rowLayoutConvert(
        "list,separator, | , bold, italic"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_replacements() {
    replayDefault();
    assertEquals("celimage cellink celimage cellink celimage cellink",
        tinyMce4Config.rowLayoutConvert("image,link,celimage,cellink,advimage,advlink"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_tablecontrols() {
    replayDefault();
    assertEquals(
        "italic table | tablerowprops tablecellprops | tableinsertrowbefore tableinsertrowafter"
            + " tabledeleterow | tableinsertcolbefore tableinsertcolafter tabledeletecol | "
            + "tablesplitcells tablemergecells bold",
        tinyMce4Config.rowLayoutConvert(
            "italic, tablecontrols, bold"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_pasteword() {
    replayDefault();
    assertEquals("italic pastetext paste bold", tinyMce4Config.rowLayoutConvert(
        "italic, pastetext,pasteword, bold"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_general() {
    replayDefault();
    assertEquals("list | bold italic celimage cellink | cellink celimage",
        tinyMce4Config.rowLayoutConvert("cancel, save, separator, list,separator,bold,italic,image,"
            + "link;|;advlink;advimage;|"));
    verifyDefault();
  }

  @Test
  public void test_validElementsCheck_default4empty() {
    replayDefault();
    assertEquals(TinyMce4Config.VALID_ELEMENTS_DEF, tinyMce4Config.validElementsCheck(""));
    verifyDefault();
  }

  @Test
  public void test_validElementsCheck_default4null() {
    replayDefault();
    assertEquals(TinyMce4Config.VALID_ELEMENTS_DEF, tinyMce4Config.validElementsCheck(null));
    verifyDefault();
  }

  @Test
  public void test_validElementsCheck_sanity() {
    replayDefault();
    assertFalse(TinyMce4Config.VALID_ELEMENTS_DEF.matches(" "));
    assertFalse(Strings.isNullOrEmpty(TinyMce4Config.VALID_ELEMENTS_DEF));
    verifyDefault();
  }

  @Test
  public void test_invalidElementsCheck_default4empty() {
    replayDefault();
    assertEquals(TinyMce4Config.INVALID_ELEMENTS_DEF, tinyMce4Config.invalidElementsCheck(""));
    verifyDefault();
  }

  @Test
  public void test_invalidElementsCheck_default4null() {
    replayDefault();
    assertEquals(TinyMce4Config.INVALID_ELEMENTS_DEF, tinyMce4Config.invalidElementsCheck(null));
    verifyDefault();
  }

  @Test
  public void test_invalidElementsCheck_sanity() {
    replayDefault();
    assertFalse(TinyMce4Config.INVALID_ELEMENTS_DEF.matches(" "));
    assertFalse(Strings.isNullOrEmpty(TinyMce4Config.INVALID_ELEMENTS_DEF));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_overwriteWith_none() {
    replayDefault();
    assertEquals("", tinyMce4Config.rowLayoutConvert("none"));
    verifyDefault();
  }

}
