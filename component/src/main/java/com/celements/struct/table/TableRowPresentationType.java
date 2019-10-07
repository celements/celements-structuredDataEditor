package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;
import static com.celements.web.classes.oldcore.XWikiDocumentClass.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Predicates.*;
import static java.util.stream.Collectors.*;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XDocumentFieldAccessor;
import com.celements.pagetype.PageTypeReference;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.velocity.VelocityContextModifier;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(TableRowPresentationType.NAME)
public class TableRowPresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTableRow";

  private static final Pattern PATTERN_NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]");

  @Requirement(XDocumentFieldAccessor.NAME)
  private FieldAccessor<XWikiDocument> xDocFieldAccessor;

  @Requirement(CLASS_DEF_HINT)
  private ClassDefinition xwikiDocPseudoClass;

  @Override
  public String getDefaultCssClass() {
    return CSS_CLASS + "_row";
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "";
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference rowDocRef,
      TableConfig tableCfg) {
    LOGGER.info("writeNodeContent - for [{}] with [{}]", rowDocRef, tableCfg);
    AttributeBuilder attributes = newAttributeBuilder();
    attributes.addCssClasses(getDefaultCssClass());
    attributes.addAttribute("data-ref", modelUtils.serializeRef(rowDocRef, COMPACT_WIKI));
    writer.openLevel("li", attributes.build());
    for (ColumnConfig colCfg : tableCfg.getColumns()) {
      writeTableCell(writer, rowDocRef, colCfg);
    }
    if (tableCfg.getColumns().isEmpty()) {
      writer.appendContent("no columns defined");
    }
    writer.closeLevel();
  }

  private void writeTableCell(ICellWriter writer, DocumentReference rowDocRef,
      ColumnConfig colCfg) {
    try {
      XWikiDocument rowDoc = modelAccess.getDocument(rowDocRef);
      AttributeBuilder attributes = newAttributeBuilder();
      attributes.addCssClasses(CSS_CLASS + "_cell");
      attributes.addCssClasses("cell_" + colCfg.getNumber());
      attributes.addCssClasses(colCfg.getName());
      attributes.addCssClasses(colCfg.getCssClasses());
      writer.openLevel(attributes.build());
      writer.appendContent(evaluateTableCellContent(rowDoc, colCfg));
      writer.closeLevel();
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("writeTableCell - failed for [{}], [{}]", rowDocRef, colCfg, exc);
    }
  }

  private String evaluateTableCellContent(XWikiDocument rowDoc, ColumnConfig colCfg) {
    String content = evaluateColumnContentOrMacro(rowDoc, colCfg);
    if (content.isEmpty() && !colCfg.getName().isEmpty()) {
      XWikiDocument tableCfgDoc = modelAccess.getOrCreateDocument(
          colCfg.getTableConfig().getDocumentReference());
      if (colCfg.isHeaderMode()) {
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
      String macroName = "col_" + colCfg.getName() + ".vm";
      if (text.isEmpty() && !colCfg.isHeaderMode()) {
        for (Iterator<String> iter = resolvePossibleTableNames(context.getCurrentDoc().get())
            .iterator(); (text.isEmpty() && iter.hasNext());) {
          text = getMacroContent(STRUCT_TABLE_DIR, iter.next(), macroName);
        }
      }
      content = velocityService.evaluateVelocityText(rowDoc, text, getVelocityContextModifier(
          rowDoc, colCfg)).trim();
    } catch (XWikiVelocityException exc) {
      LOGGER.error("writeTableCell - failed for [{}]", colCfg, exc);
    }
    return content;
  }

  private String resolveTitleFromDictionary(XWikiDocument cellDoc, String name) {
    String title = "";
    Optional<ClassReference> classRef = structDataEditorService.getCellClassRef(cellDoc);
    if (classRef.isPresent()) {
      String dictKey = modelUtils.serializeRef(classRef.get()) + "_" + name;
      String msg = webUtils.getAdminMessageTool().get(dictKey);
      if (!dictKey.equals(msg)) {
        title = msg;
      } else {
        LOGGER.info("resolveTitleFromDictionary: nothing found for [{}]", dictKey);
      }
    }
    return title;
  }

  private String loadColumnFieldValue(XWikiDocument cellDoc, XWikiDocument rowDoc, String name) {
    String value = "";
    Optional<BaseObject> obj = structDataEditorService.getXObjectInStructEditor(cellDoc, rowDoc);
    if (obj.isPresent() && hasValue(obj.get(), name)) {
      value = obj.get().displayView(name, context.getXWikiContext());
    } else if (xwikiDocPseudoClass.getField(name).isPresent()) {
      value = xDocFieldAccessor.getValue(rowDoc, xwikiDocPseudoClass.getField(name).get())
          .transform(Object::toString).or("");
    }
    return value;
  }

  private boolean hasValue(BaseObject obj, String name) {
    return !firstNonNull(modelAccess.getProperty(obj, name), "").toString().trim().isEmpty();
  }

  /**
   * @return possible table names:
   *         1. struct layout space name defined by StructLayoutClass_layoutSpace
   *         2. table page type name
   */
  List<String> resolvePossibleTableNames(XWikiDocument tableDoc) {
    ImmutableList.Builder<String> tableNames = new ImmutableList.Builder<>();
    structDataService.getStructLayoutSpaceRef(tableDoc).toJavaUtil()
        .map(this::getFirstPartOfLayoutName).ifPresent(tableNames::add);
    pageTypeResolver.resolvePageTypeReference(tableDoc).toJavaUtil()
        .map(PageTypeReference::getConfigName).ifPresent(tableNames::add);
    tableNames.add("");
    return tableNames.build();
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

  VelocityContextModifier getVelocityContextModifier(final XWikiDocument rowDoc,
      final ColumnConfig colCfg) {
    return vContext -> {
      vContext.put("colcfg", colCfg);
      try {
        vContext.put("rowdoc", modelAccess.getApiDocument(rowDoc));
      } catch (NoAccessRightsException exc) {
        LOGGER.info("missing access rights on row", exc);
      }
      return vContext;
    };
  }

}
