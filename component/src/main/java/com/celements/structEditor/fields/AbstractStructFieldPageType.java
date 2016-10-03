package com.celements.structEditor.fields;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public abstract class AbstractStructFieldPageType extends AbstractJavaPageType {

  protected static final String EDIT_TEMPLATE_NAME = "StructDataFieldEdit";

  @Requirement("structEditFieldTypeCategory")
  protected IPageTypeCategoryRole pageTypeCategory;

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
   * used for tags with name attribute
   */
  protected final void addNameAttribute(AttributeBuilder attrBuilder, XWikiDocument cellDoc) {
    List<String> nameParts = new ArrayList<>();
    String className = modelAccess.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_CLASS_NAME);
    if (!className.isEmpty()) {
      int objNb = getObjNumber(cellDoc, className);
      nameParts.add(Strings.emptyToNull(className));
      nameParts.add(Strings.emptyToNull(Integer.toString(objNb)));
    }
    nameParts.add(Strings.emptyToNull(modelAccess.getProperty(cellDoc,
        StructuredDataEditorClass.FIELD_EDIT_FIELD_NAME)));
    attrBuilder.addAttribute("name", Joiner.on('_').skipNulls().join(nameParts));
  }

  private int getObjNumber(XWikiDocument cellDoc, String className) {
    DocumentReference classRef = modelUtils.resolveRef(className, DocumentReference.class,
        cellDoc.getDocumentReference().getWikiReference());
    BaseObject obj = modelAccess.getXObject(cellDoc, classRef);
    return obj != null ? obj.getNumber() : -1;
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

  protected StructuredDataEditorService getStructDataEditorService() {
    return Utils.getComponent(StructuredDataEditorService.class);
  }

}
