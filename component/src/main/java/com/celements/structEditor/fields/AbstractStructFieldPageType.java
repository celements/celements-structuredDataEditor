package com.celements.structEditor.fields;

import java.util.Set;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.celements.structEditor.StructuredDataEditorService;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractStructFieldPageType extends AbstractJavaPageType {

  protected static final String EDIT_TEMPLATE_NAME = "StructDataFieldEdit";

  @Requirement("structEditFieldTypeCategory")
  protected IPageTypeCategoryRole pageTypeCategory;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected StructuredDataEditorService service;

  @Requirement
  protected ModelContext modelContext;

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

  protected <T> Optional<T> getFieldValue(DocumentReference cellDocRef, ClassField<T> classField)
      throws DocumentNotExistsException {
    return Optional.fromNullable(modelAccess.getProperty(cellDocRef, classField));
  }

  protected Optional<String> getNotEmptyString(DocumentReference cellDocRef,
      ClassField<String> classField) throws DocumentNotExistsException {
    return Optional.fromNullable(Strings.emptyToNull(modelAccess.getProperty(cellDocRef,
        classField)));
  }

  protected <T> Optional<T> getFieldValue(XWikiDocument cellDoc, ClassField<T> classField) {
    return Optional.fromNullable(modelAccess.getProperty(cellDoc, classField));
  }

  protected Optional<String> getNotEmptyString(XWikiDocument cellDoc,
      ClassField<String> classField) {
    return Optional.fromNullable(Strings.emptyToNull(modelAccess.getProperty(cellDoc, classField)));
  }

}
