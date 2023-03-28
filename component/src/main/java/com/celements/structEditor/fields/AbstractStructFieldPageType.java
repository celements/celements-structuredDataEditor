/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.structEditor.fields;

import static com.celements.common.lambda.LambdaExceptionUtil.*;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.celements.struct.StructDataService;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.velocity.VelocityService;
import com.google.common.collect.Sets;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public abstract class AbstractStructFieldPageType extends AbstractJavaPageType {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

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

  @Override
  public com.google.common.base.Optional<String> defaultTagName() {
    return com.google.common.base.Optional.fromJavaUtil(tagName());
  }

  public Optional<String> tagName() {
    return Optional.empty();
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

  protected Optional<String> getVelocityFieldValue(XWikiDocument cellDoc,
      ClassField<String> classField) throws XWikiVelocityException {
    return XWikiObjectFetcher.on(cellDoc)
        .fetchField(classField)
        .findFirst()
        .map(rethrowFunction(velocityService::evaluateVelocityText));
  }

  /**
   * CAUTION: cyclic dependency with DefaultStructuredDataEditorService !!!!
   */
  protected StructuredDataEditorService getStructDataEditorService() {
    return Utils.getComponent(StructuredDataEditorService.class);
  }

}
