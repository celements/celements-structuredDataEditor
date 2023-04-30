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

import static com.celements.web.classes.oldcore.XWikiDocumentClass.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Predicates.*;
import static java.util.stream.Collectors.*;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.common.MoreOptional;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.StringFieldAccessor;
import com.celements.model.field.XDocumentFieldAccessor;
import com.celements.model.field.XObjectStringFieldAccessor;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.struct.StructDataService;
import com.celements.velocity.VelocityContextModifier;
import com.celements.velocity.VelocityService;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(TableRowColumnPresentationType.NAME)
public class TableRowColumnPresentationType extends AbstractTableRowPresentationType {

  public static final String NAME = AbstractTableRowPresentationType.NAME + "-column";

  private static final String STRUCT_TABLE_DIR = "/templates/celStruct/table";
  private static final Pattern PATTERN_NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]");

  @Requirement(XDocumentFieldAccessor.NAME)
  private FieldAccessor<XWikiDocument> xDocFieldAccessor;

  @Requirement(XObjectStringFieldAccessor.NAME)
  private StringFieldAccessor<BaseObject> xObjStringFieldAccessor;

  @Requirement(CLASS_DEF_HINT)
  private ClassDefinition xwikiDocPseudoClass;

  @Requirement
  private StructDataService structService;

  @Requirement
  private VelocityService velocityService;

  @Requirement
  private IPageTypeResolverRole pageTypeResolver;

  @Override
  protected void writeRowContent(ICellWriter writer, DocumentReference rowDocRef,
      TableConfig tableCfg) {
    for (ColumnConfig colCfg : tableCfg.getColumns()) {
      try {
        XWikiDocument rowDoc = modelAccess.getDocument(rowDocRef);
        AttributeBuilder attributes = new DefaultAttributeBuilder();
        attributes.addCssClasses(CSS_CLASS + "_cell");
        attributes.addCssClasses("cell_" + colCfg.getNumber());
        attributes.addCssClasses(colCfg.getName());
        attributes.addCssClasses(colCfg.getCssClasses());
        writer.openLevel(attributes.build());
        writer.appendContent(evaluateTableCellContent(rowDoc, colCfg));
        writer.closeLevel();
      } catch (DocumentNotExistsException exc) {
        logger.warn("writeTableCell - failed for [{}], [{}]", rowDocRef, colCfg, exc);
      }
    }
  }

  private String evaluateTableCellContent(XWikiDocument rowDoc, ColumnConfig colCfg) {
    String content = evaluateColumnContentOrMacro(rowDoc, colCfg);
    if (content.isEmpty() && !colCfg.getName().isEmpty()) {
      XWikiDocument tableCfgDoc = modelAccess.getOrCreateDocument(
          colCfg.getTableConfig().getDocumentReference());
      if (colCfg.getTableConfig().isHeaderMode()) {
        content = resolveTitleFromDictionary(tableCfgDoc, colCfg.getName());
      } else {
        content = loadColumnFieldValue(tableCfgDoc, rowDoc, colCfg.getName());
      }
    }
    return content;
  }

  private String evaluateColumnContentOrMacro(XWikiDocument rowDoc, ColumnConfig colCfg) {
    String content = "";
    try {
      String text = colCfg.getContent().trim();
      if (text.isEmpty() && !colCfg.getTableConfig().isHeaderMode()) {
        String macroName = "col_" + colCfg.getName() + ".vm";
        XWikiDocument tableCfgDoc = modelAccess.getOrCreateDocument(
            colCfg.getTableConfig().getDocumentReference());
        text = resolvePossibleTableNames(tableCfgDoc)
            .map(name -> getMacroContent(STRUCT_TABLE_DIR, name, macroName))
            .filter(String::isEmpty)
            .findFirst().orElse("");
      }
      content = velocityService.evaluateVelocityText(rowDoc, text, getVelocityContextModifier(
          rowDoc, colCfg)).trim();
    } catch (XWikiVelocityException exc) {
      logger.error("writeTableCell - failed for [{}]", colCfg, exc);
    }
    return content;
  }

  private String loadColumnFieldValue(XWikiDocument cellDoc, XWikiDocument rowDoc, String name) {
    String value = "";
    Optional<BaseObject> obj = editorService.getXObjectInStructEditor(cellDoc, rowDoc);
    if (obj.isPresent() && hasValue(obj.get(), name)) {
      value = obj.get().displayView(name, context.getXWikiContext());
    } else if (xwikiDocPseudoClass.getField(name).isPresent()) {
      value = xDocFieldAccessor.get(rowDoc, xwikiDocPseudoClass.getField(name).get())
          .map(Object::toString).orElse("");
    }
    return value;
  }

  private boolean hasValue(BaseObject obj, String name) {
    return !firstNonNull(xObjStringFieldAccessor.get(obj, name), "").toString().trim().isEmpty();
  }

  /**
   * @return possible table names:
   *         1. struct layout space name defined by StructLayoutClass_layoutSpace
   *         2. table page type name
   */
  Stream<String> resolvePossibleTableNames(XWikiDocument tableCfgDoc) {
    return Stream.of(
        structService.getStructLayoutSpaceRef(tableCfgDoc)
            .map(this::getFirstPartOfLayoutName),
        pageTypeResolver.resolvePageTypeReference(tableCfgDoc).toJavaUtil()
            .map(PageTypeReference::getConfigName),
        Optional.of(""))
        .flatMap(MoreOptional::stream);
  }

  private String getFirstPartOfLayoutName(SpaceReference layoutSpaceRef) {
    return Splitter.on(PATTERN_NON_ALPHANUMERIC).split(layoutSpaceRef.getName()).iterator()
        .next();
  }

  private String getMacroContent(String... paths) {
    return Strings.nullToEmpty(webUtils.getTranslatedDiscTemplateContent(
        Stream.of(paths).filter(not(String::isEmpty)).collect(joining("/")),
        null, null)).trim();
  }

  protected VelocityContextModifier getVelocityContextModifier(final XWikiDocument rowDoc,
      final ColumnConfig colCfg) {
    return vContext -> {
      vContext.put("colcfg", colCfg);
      try {
        vContext.put("rowdoc", modelAccess.getApiDocument(rowDoc));
      } catch (NoAccessRightsException exc) {
        logger.info("missing access rights on row", exc);
      }
      return vContext;
    };
  }

}
