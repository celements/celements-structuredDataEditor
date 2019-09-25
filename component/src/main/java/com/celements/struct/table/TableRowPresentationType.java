package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;
import static com.google.common.base.MoreObjects.*;

import java.util.regex.Pattern;

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
        String macroName = "col_" + colCfg.getName() + ".vm";
        text += getMacroContent(macroName);
        text += getMacroContent(resolveTableName(colCfg) + "/" + macroName);
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
    }
    return value;
  }

  private boolean hasValue(BaseObject obj, String name) {
    return !firstNonNull(modelAccess.getProperty(obj, name), "").toString().trim().isEmpty();
  }

  /**
   * @return the table name, which is the first valid value of the following possibilites:
   *         1. struct layout space name defined by StructLayoutClass_layoutSpace
   *         2. table page type name
   *         3. table config layout space name (legacy support)
   */
  String resolveTableName(final ColumnConfig colCfg) {
    String directory = structDataService.getStructLayoutSpaceRef(context.getCurrentDoc().get())
        .transform(this::getFirstPartOfLayoutName)
        .or(() -> pageTypeResolver.resolvePageTypeReference(context.getCurrentDoc().get())
            .transform(PageTypeReference::getConfigName).orNull());
    return Optional.fromNullable(directory).or(() -> getFirstPartOfLayoutName(
        colCfg.getTableConfig().getDocumentReference().getLastSpaceReference()));
  }

  private String getFirstPartOfLayoutName(SpaceReference layoutSpaceRef) {
    return Splitter.on(PATTERN_NON_ALPHANUMERIC).split(layoutSpaceRef.getName()).iterator().next();
  }

  private String getMacroContent(String path) {
    return webUtils.getTranslatedDiscTemplateContent(STRUCT_TABLE_DIR + path, null, null);
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
