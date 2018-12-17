package com.celements.struct.table;

import static com.celements.model.util.ReferenceSerializationMode.*;
import static com.google.common.base.MoreObjects.*;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.ICellWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.struct.VelocityContextModifier;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TableRowPresentationType.NAME)
public class TableRowPresentationType extends AbstractTablePresentationType {

  public static final String NAME = "structTableRow";

  @Requirement
  private IPageTypeResolverRole pageTypeResolver;

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
      attributes.addCssClasses(CSS_CLASS + "_cell_" + colCfg.getNumber());
      attributes.addCssClasses(colCfg.getCssClasses());
      writer.openLevel(attributes.build());
      writer.appendContent(evaluateTableCellContent(rowDoc, colCfg));
      writer.closeLevel();
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("writeTableCell - failed for [{}], [{}]", rowDocRef, colCfg, exc);
    }
  }

  private String evaluateTableCellContent(final XWikiDocument rowDoc, final ColumnConfig colCfg) {
    String text = "";
    text = colCfg.getContent();
    if (Strings.nullToEmpty(text).trim().isEmpty()) {
      // fallback celStruct/table/<tblName>/col_<colNb>.vm
      text = "#parse('celStruct/table/" + resolveTableName(colCfg) + "/col_" + firstNonNull(
          colCfg.getOrder(), colCfg.getNumber()) + ".vm')";
    }
    String content;
    try {
      content = structDataService.evaluateVelocityText(rowDoc, text, getVelocityContextModifier(
          rowDoc, colCfg));
    } catch (XWikiVelocityException exc) {
      LOGGER.warn("writeTableCell - failed for [{}]", colCfg, exc);
      content = "failed to evaluate velocity - " + exc.getMessage() + ": " + text;
    }
    return content;
  }

  private String resolveTableName(final ColumnConfig colCfg) {
    String tableName = "";
    Optional<PageTypeReference> ptRef = pageTypeResolver.resolvePageTypeReference(context.getDoc());
    if (ptRef.isPresent()) {
      tableName = ptRef.get().getConfigName();
    }
    if (tableName.isEmpty()) {
      tableName = colCfg.getTableConfig().getCssId();
    }
    if (tableName.isEmpty()) {
      tableName = modelUtils.serializeRef(colCfg.getTableConfig().getDocumentReference(), LOCAL);
    }
    return tableName;
  }

  private VelocityContextModifier getVelocityContextModifier(final XWikiDocument rowDoc,
      final ColumnConfig colCfg) {
    return new VelocityContextModifier() {

      @Override
      public VelocityContext apply(VelocityContext vContext) {
        vContext.put("colCfg", colCfg);
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
