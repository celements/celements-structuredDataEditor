package com.celements.struct;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.reflect.ReflectiveInstanceSupplier;
import com.celements.convert.bean.BeanClassDefConverter;
import com.celements.convert.bean.XObjectBeanConverter;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.struct.classes.StructLayoutClass;
import com.celements.struct.classes.TableClass;
import com.celements.struct.classes.TableColumnClass;
import com.celements.struct.table.ColumnConfig;
import com.celements.struct.table.TableConfig;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultStructDataService implements StructDataService, Initializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStructDataService.class);

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
  public WikiReference getCentralWikiRef() {
    return new WikiReference("celements2web");
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
    LOGGER.info("loadTableConfig: for '{}' got '{}'", cellDoc, tableCfg);
    return tableCfg;
  }

  @Override
  public Optional<SpaceReference> getStructLayoutSpaceRef(XWikiDocument doc) {
    SpaceReference structLayoutRef = XWikiObjectFetcher.on(doc)
        .fetchField(StructLayoutClass.FIELD_LAYOUT_SPACE).first().orNull();
    if (structLayoutRef != null) {
      SpaceReference centralLayoutRef = RefBuilder.from(structLayoutRef).with(getCentralWikiRef())
          .build(SpaceReference.class);
      if (new PageLayoutCommand().layoutExists(structLayoutRef)) {
        return Optional.of(structLayoutRef);
      } else if (new PageLayoutCommand().layoutExists(centralLayoutRef)) {
        return Optional.of(centralLayoutRef);
      }
    }
    return Optional.absent();
  }

}
