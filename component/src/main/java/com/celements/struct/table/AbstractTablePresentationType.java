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
package com.celements.struct.table;

import static com.google.common.base.Strings.*;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.DivWriter;
import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.struct.StructDataService;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.velocity.VelocityService;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableSet;

public abstract class AbstractTablePresentationType implements IPresentationTypeRole<TableConfig> {

  public static final String NAME = "struct-table";

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  public static final String STRUCT_TABLE_DIR = "/templates/celStruct/table";
  public static final String CSS_CLASS = "struct_table";

  private static final Set<String> EDIT_ACTIONS = ImmutableSet.of("edit", "inline");

  @Requirement(StructuredDataEditorClass.CLASS_DEF_HINT)
  protected ClassDefinition structFieldClassDef;

  @Requirement
  protected StructDataService structService;

  @Requirement
  protected StructuredDataEditorService editorService;

  @Requirement
  protected VelocityService velocityService;

  @Requirement
  protected IPageTypeResolverRole pageTypeResolver;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelUtils modelUtils;

  @Requirement
  protected ModelContext context;

  @Requirement
  protected Execution execution;

  @Override
  public void writeNodeContent(StringBuilder writer, boolean isFirstItem, boolean isLastItem,
      DocumentReference nodeDocRef, boolean isLeaf, int numItem, TableConfig table) {
    writeNodeContent(new DivWriter(writer), nodeDocRef, table);
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

  protected AttributeBuilder newAttributeBuilder() {
    return new DefaultAttributeBuilder();
  }

  protected boolean isEditAction() {
    return EDIT_ACTIONS.contains(context.getXWikiContext().getAction());
  }

  protected void writeCreateLink(ICellWriter writer) {
    writeLink(writer, "create", "halflings icon-plus");
  }

  protected void writeDeleteLink(ICellWriter writer) {
    writeLink(writer, "delete", "halflings icon-trash");
  }

  protected void writeLink(ICellWriter writer, String name, String icon) {
    writer.openLevel("a", newAttributeBuilder()
        .addCssClasses(CSS_CLASS + "_" + name)
        .addEmptyAttribute("href")
        .build());
    writer.openLevel("i", newAttributeBuilder()
        .addCssClasses("icon " + nullToEmpty(icon))
        .addAttribute("title", name)
        .build());
    writer.closeLevel(); // i
    writer.closeLevel(); // a
  }

}
