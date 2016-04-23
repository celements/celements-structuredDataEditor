package com.celements.structEditor.pagetypes;

import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

@Component(StructuredDataEditorPageType.STRUCURED_DATA_EDITOR_PAGETYPE_NAME)
public class StructuredDataEditorPageType extends AbstractJavaPageType {

  public static final String STRUCURED_DATA_EDITOR_PAGETYPE_NAME = "StrucuredDataEditor";

  @Requirement
  private IPageTypeCategoryRole pageTypeCategory;

  @Override
  public boolean displayInFrameLayout() {
    return true;
  }

  @Override
  public Set<IPageTypeCategoryRole> getCategories() {
    return Sets.newHashSet(pageTypeCategory);
  }

  @Override
  public String getName() {
    return STRUCURED_DATA_EDITOR_PAGETYPE_NAME;
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    return "Templates.StructuredDataEditor";
  }

  @Override
  public boolean hasPageTitle() {
    return false;
  }

  @Override
  public boolean isUnconnectedParent() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

}
