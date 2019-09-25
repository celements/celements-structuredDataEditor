package com.celements.struct;

import java.util.Set;

import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

public abstract class StructBasePageType extends AbstractJavaPageType {

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
  public String getRenderTemplateForRenderMode(String renderMode) {
    if ("edit".equals(renderMode)) {
      return getEditRenderTemplate();
    } else {
      return getViewRenderTemplate();
    }
  }

  public abstract String getViewRenderTemplate();

  public abstract String getEditRenderTemplate();

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
    return false;
  }

}
