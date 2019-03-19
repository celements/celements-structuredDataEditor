package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;
import static com.google.common.base.MoreObjects.*;

import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.PageTypeReference;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.velocity.VelocityContextModifier;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(TableRowPresentationType.NAME)
public class TableRowPresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTableRow";

  private static final Pattern PATTERN_NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]");

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
      if (text.isEmpty() && !colCfg.isHeaderMode()) {
        String macroPath = "/templates/" + STRUCT_TABLE_FOLDER + resolveMacroName(colCfg) + ".vm";
        text = webUtils.getTranslatedDiscTemplateContent(macroPath, null, null);
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
      }
    }
    return title;
  }

  private String loadColumnFieldValue(XWikiDocument cellDoc, XWikiDocument rowDoc, String name) {
    String value = "";
    Optional<BaseObject> obj = structDataEditorService.getXObjectInStructEditor(cellDoc, rowDoc);
    if (obj.isPresent() && hasValue(obj.get(), name)) {
      value = obj.get().displayView(name, context.getXWikiContext());
    }
    return value;
  }

  private boolean hasValue(BaseObject obj, String name) {
    return !firstNonNull(modelAccess.getProperty(obj, name), "").toString().trim().isEmpty();
  }

  /**
   * {@code <tblName>/col_<colName>.vm}
   * tblName - either table page type name or layout space primary name
   * colName - defined column name
   */
  String resolveMacroName(ColumnConfig colCfg) {
    String tblName = "";
    Optional<PageTypeReference> ptRef = pageTypeResolver.resolvePageTypeReference(context.getDoc());
    if (ptRef.isPresent()) {
      tblName = ptRef.get().getConfigName();
    }
    if (tblName.isEmpty()) {
      tblName = resolvePrimaryLayoutSpaceName(colCfg.getTableConfig());
    }
    return tblName + "/col_" + colCfg.getName();
  }

  private String resolvePrimaryLayoutSpaceName(TableConfig tableCfg) {
    SpaceReference layoutSpaceRef = tableCfg.getDocumentReference().getLastSpaceReference();
    return Splitter.on(PATTERN_NON_ALPHANUMERIC).split(layoutSpaceRef.getName()).iterator().next();
  }

  private VelocityContextModifier getVelocityContextModifier(final XWikiDocument rowDoc,
      final ColumnConfig colCfg) {
    return new VelocityContextModifier() {

      @Override
      public VelocityContext apply(VelocityContext vContext) {
        vContext.put("colcfg", colCfg);
        try {
          vContext.put("rowdoc", modelAccess.getApiDocument(rowDoc));
        } catch (NoAccessRightsException exc) {
          LOGGER.info("missing access rights on row", exc);
        }
        return vContext;
      }
    };
  }

}
