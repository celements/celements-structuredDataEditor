package com.celements.structEditor;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.IJavaPageTypeRole;
import com.xpn.xwiki.web.Utils;

public class StructuredDataEditorPageTypeTest extends AbstractComponentTest {

  private StructuredDataEditorPageType structDataEditPT;

  @Before
  public void setUp_StructuredDataEditorPageTypeTest() throws Exception {
    structDataEditPT = (StructuredDataEditorPageType) Utils.getComponent(IJavaPageTypeRole.class,
        StructuredDataEditorPageType.STRUCURED_DATA_EDITOR_PAGETYPE_NAME);
  }

  @Test
  public void test_getName() {
    replayDefault();
    assertEquals(StructuredDataEditorPageType.STRUCURED_DATA_EDITOR_PAGETYPE_NAME,
        structDataEditPT.getName());
    verifyDefault();
  }

  @Test
  public void test_displayInFrameLayout() {
    replayDefault();
    assertTrue(structDataEditPT.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void test_getCategories() {
    replayDefault();
    Set<IPageTypeCategoryRole> categories = structDataEditPT.getCategories();
    assertNotNull(categories);
    IPageTypeCategoryRole pageTypeCat = Utils.getComponent(IPageTypeCategoryRole.class);
    assertEquals(1, categories.size());
    assertTrue(categories.contains(pageTypeCat));
    verifyDefault();
  }

  @Test
  public void test_getRenderTemplateForRenderMode_edit() {
    replayDefault();
    assertEquals(StructuredDataEditorPageType.EDIT_TEMPLATE_NAME,
        structDataEditPT.getRenderTemplateForRenderMode("edit"));
    verifyDefault();
  }

  @Test
  public void test_getRenderTemplateForRenderMode_view() {
    replayDefault();
    assertEquals(StructuredDataEditorPageType.VIEW_TEMPLATE_NAME,
        structDataEditPT.getRenderTemplateForRenderMode("view"));
    verifyDefault();
  }

  @Test
  public void test_hasPageTitle() {
    replayDefault();
    assertFalse(structDataEditPT.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void test_isUnconnectedParent() {
    replayDefault();
    assertFalse(structDataEditPT.isUnconnectedParent());
    verifyDefault();
  }

  @Test
  public void test_isVisible() {
    replayDefault();
    assertFalse(structDataEditPT.isVisible());
    verifyDefault();
  }

}
