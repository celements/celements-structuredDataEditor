package com.celements.structEditor.fields;

import java.util.Set;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.celements.struct.StructDataService;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.velocity.VelocityService;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public abstract class AbstractStructFieldPageType extends AbstractJavaPageType {

  protected static final String EDIT_TEMPLATE_NAME = "StructDataFieldEdit";

  @Requirement
  protected StructDataService structDataService;

  @Requirement("structEditFieldTypeCategory")
  protected IPageTypeCategoryRole pageTypeCategory;

  @Requirement
  protected VelocityService velocityService;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelUtils modelUtils;

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

  /**
   * @deprecated use {@link IModelAccessFacade#getFieldValue()} directly
   */
  @Deprecated
  protected <T> Optional<T> getFieldValue(DocumentReference cellDocRef, ClassField<T> classField)
      throws DocumentNotExistsException {
    return modelAccess.getFieldValue(cellDocRef, classField);
  }

  /**
   * @deprecated use {@link IModelAccessFacade#getFieldValue()} directly
   */
  @Deprecated
  protected Optional<String> getNotEmptyString(DocumentReference cellDocRef,
      ClassField<String> classField) throws DocumentNotExistsException {
    return modelAccess.getFieldValue(cellDocRef, classField);
  }

  /**
   * @deprecated use {@link IModelAccessFacade#getFieldValue()} directly
   */
  @Deprecated
  protected <T> Optional<T> getFieldValue(XWikiDocument cellDoc, ClassField<T> classField) {
    return modelAccess.getFieldValue(cellDoc, classField);
  }

  /**
   * @deprecated use {@link IModelAccessFacade#getFieldValue()} directly
   */
  @Deprecated
  protected Optional<String> getNotEmptyString(XWikiDocument cellDoc,
      ClassField<String> classField) {
    return modelAccess.getFieldValue(cellDoc, classField);
  }

  protected Optional<String> getVelocityFieldValue(XWikiDocument cellDoc,
      ClassField<String> classField) throws XWikiVelocityException {
    Optional<String> text = modelAccess.getFieldValue(cellDoc, classField);
    if (text.isPresent()) {
      text = Optional.of(velocityService.evaluateVelocityText(text.get()));
    }
    return text;
  }

  protected StructuredDataEditorService getStructDataEditorService() {
    return Utils.getComponent(StructuredDataEditorService.class);
  }

}
