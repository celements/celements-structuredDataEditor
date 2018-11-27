package com.celements.struct;

import static com.google.common.base.Preconditions.*;

import java.io.StringWriter;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.common.reflect.ReflectiveInstanceSupplier;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.rights.access.exceptions.NoAccessRightsException;
import com.celements.struct.classes.TableClass;
import com.celements.struct.classes.TableColumnClass;
import com.celements.struct.table.ColumnConfig;
import com.celements.struct.table.TableConfig;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultStructDataService implements StructDataService, Initializable {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultStructDataService.class);

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  @Requirement
  private VelocityManager velocityManager;

  @Requirement(XObjectBeanConverter.NAME)
  private BeanClassDefConverter<BaseObject, TableConfig> tableConverter;

  @Requirement(XObjectBeanConverter.NAME)
  private BeanClassDefConverter<BaseObject, ColumnConfig> columnConverter;

  @Requirement(TableClass.CLASS_DEF_HINT)
  private ClassDefinition tableClass;

  @Requirement(TableColumnClass.CLASS_DEF_HINT)
  private ClassDefinition columnClass;

  @Override
  public void initialize() throws InitializationException {
    tableConverter.initialize(tableClass);
    tableConverter.initialize(new ReflectiveInstanceSupplier<>(TableConfig.class));
    columnConverter.initialize(columnClass);
    columnConverter.initialize(new ReflectiveInstanceSupplier<>(ColumnConfig.class));
  }

  @Override
  public String evaluateVelocityText(XWikiDocument doc, String text) throws NoAccessRightsException,
      XWikiVelocityException {
    checkNotNull(doc);
    VelocityContext vContext = (VelocityContext) velocityManager.getVelocityContext().clone();
    vContext.put("doc", modelAccess.getApiDocument(doc));
    return evaluateVelocityText(doc, text, vContext);
  }

  @Override
  public String evaluateVelocityText(String text) throws XWikiVelocityException {
    return evaluateVelocityText(context.getDoc(), text, velocityManager.getVelocityContext());
  }

  private String evaluateVelocityText(XWikiDocument doc, String text, VelocityContext vContext)
      throws XWikiVelocityException {
    StringWriter writer = new StringWriter();
    velocityManager.getVelocityEngine().evaluate(vContext, writer, modelUtils.serializeRef(
        checkNotNull(doc).getDocumentReference()), Strings.nullToEmpty(text));
    String result = writer.toString();
    LOGGER.debug("evaluateVelocityText - for [{}], [{}]: {}", doc, text, result);
    return result;
  }

  @Override
  public Optional<TableConfig> loadTableConfig(XWikiDocument cellDoc) {
    Optional<TableConfig> tableCfg = XWikiObjectFetcher.on(cellDoc).filter(
        tableClass).iter().transform(tableConverter).first();
    if (tableCfg.isPresent()) {
      List<ColumnConfig> columns = XWikiObjectFetcher.on(cellDoc).filter(
          columnClass).iter().transform(columnConverter).toList();
      tableCfg.get().setColumns(columns);
    }
    return tableCfg;
  }

}
