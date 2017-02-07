package com.celements.structEditor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.celements.structEditor.fields.OptionTagPageType;
import com.xpn.xwiki.web.Utils;

public class OptionTagPageTypeTest extends AbstractComponentTest {

  OptionTagPageType optionTagPageType;

  @Before
  public void prepare_Test() throws Exception {
    optionTagPageType = (OptionTagPageType) Utils.getComponent(IJavaPageTypeRole.class,
        OptionTagPageType.PAGETYPE_NAME);

  }

  @Test
  public void testGetName() {
    String expectedStr = OptionTagPageType.PAGETYPE_NAME;
    assertEquals(expectedStr, optionTagPageType.getName());
  }

  @Test
  public void test_GetViewTemplateName() {
    String expectedStr = "OptionTagView";
    assertEquals(expectedStr, optionTagPageType.getViewTemplateName());
  }

  @Test
  public void test_defaultTagName() {
    assertEquals("option", optionTagPageType.defaultTagName().get());
  }

}
