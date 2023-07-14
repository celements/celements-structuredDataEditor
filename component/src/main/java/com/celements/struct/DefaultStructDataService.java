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
package com.celements.struct;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;

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
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.struct.classes.StructLayoutClass;
import com.celements.struct.classes.TableClass;
import com.celements.struct.classes.TableColumnClass;
import com.celements.struct.table.ColumnConfig;
import com.celements.struct.table.TableConfig;
import com.celements.web.CelConstant;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultStructDataService implements StructDataService, Initializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStructDataService.class);

  @Requirement
  private LayoutServiceRole layoutService;

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
    return new WikiReference(CelConstant.CENTRAL_WIKI_NAME);
  }

  @Override
  public Optional<TableConfig> loadTableConfig(XWikiDocument cellDoc) {
    Optional<TableConfig> tableCfg = XWikiObjectFetcher.on(cellDoc).filter(tableClass).stream()
        .map(tableConverter).findFirst();
    tableCfg.ifPresent(cfg -> {
      List<ColumnConfig> columns = XWikiObjectFetcher.on(cellDoc).filter(columnClass).stream()
          .map(columnConverter).collect(toList());
      cfg.setColumns(columns);
    });
    LOGGER.info("loadTableConfig: for '{}' got '{}'", cellDoc, tableCfg);
    return tableCfg;
  }

  @Override
  public Optional<SpaceReference> getStructLayoutSpaceRef(XWikiDocument doc) {
    Optional<SpaceReference> structLayoutRef = XWikiObjectFetcher.on(doc)
        .fetchField(StructLayoutClass.FIELD_LAYOUT_SPACE)
        .stream().findFirst();
    return structLayoutRef
        .filter(layoutService::existsLayout)
        .or(() -> RefBuilder.from(structLayoutRef.orElse(null))
            .with(getCentralWikiRef())
            .buildOpt(SpaceReference.class)
            .filter(layoutService::existsLayout));
  }

}
