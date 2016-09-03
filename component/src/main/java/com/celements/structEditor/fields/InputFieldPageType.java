package com.celements.structEditor.fields;

import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

@Component(InputFieldPageType.INPUT_FIELD_PAGETYPE_NAME)
public class InputFieldPageType extends AbstractJavaPageType {

  public static final String INPUT_FIELD_PAGETYPE_NAME = "InputField";

  static final String VIEW_TEMPLATE_NAME = "InputFieldView";
  static final String EDIT_TEMPLATE_NAME = "StructDataFieldEdit";

  @Requirement("structEditFieldTypeCategory")
  private IPageTypeCategoryRole pageTypeCategory;

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
  }

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
  public String getRenderTemplateForRenderMode(String renderMode) {
    if ("edit".equals(renderMode)) {
      return EDIT_TEMPLATE_NAME;
    } else {
      return VIEW_TEMPLATE_NAME;
    }
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public boolean isUnconnectedParent() {
    return false;
  }

}
