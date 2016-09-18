package com.celements.structEditor.fields;

import java.util.Set;

import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

public abstract class AbstractStructFieldPageType extends AbstractJavaPageType {

  protected static final String EDIT_TEMPLATE_NAME = "StructDataFieldEdit";

  @Requirement("structEditFieldTypeCategory")
  protected IPageTypeCategoryRole pageTypeCategory;

  @Override
  public Set<IPageTypeCategoryRole> getCategories() {
    return Sets.newHashSet(pageTypeCategory);
  }

  @Override
  public boolean hasPageTitle() {
    return false;
  }

  @Override
  public boolean displayInFrameLayout() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public boolean isUnconnectedParent() {
    return false;
  }

  protected abstract String getViewTemplateName();

  protected String getEditTemplateName() {
    return EDIT_TEMPLATE_NAME;
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    if ("edit".equals(renderMode)) {
      return getEditTemplateName();
    } else {
      return getViewTemplateName();
    }
  }

}
