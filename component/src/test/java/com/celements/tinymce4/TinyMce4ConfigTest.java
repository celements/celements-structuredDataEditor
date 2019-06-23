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
            + "tablesplitcells tablemergecells bold", tinyMce4Config.rowLayoutConvert(
                "italic, tablecontrols, bold"));
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

}
