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

import static com.google.common.base.MoreObjects.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.NotThreadSafe;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.presentation.PresentationNodeData;
import com.celements.struct.classes.TableClass.Type;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@NotThreadSafe
public class TableConfig implements PresentationNodeData {

  private boolean headerMode = false;

  private DocumentReference documentReference;
  private Type type = Type.DOC;
  private String query = "";
  private List<String> sortFields = ImmutableList.of();
  private int resultLimit = 0;
  private String cssId = "";
  private List<String> cssClasses = ImmutableList.of();
  private SpaceReference rowLayout;
  private SpaceReference headerLayout;
  private List<ColumnConfig> columns = ImmutableList.of();

  public DocumentReference getDocumentReference() {
    return documentReference;
  }

  public void setDocumentReference(DocumentReference documentReference) {
    this.documentReference = documentReference;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = Optional.ofNullable(type).orElse(Type.DOC);
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = Strings.nullToEmpty(query);
  }

  public List<String> getSortFields() {
    return sortFields;
  }

  public void setSortFields(List<String> sortFields) {
    this.sortFields = ImmutableList.copyOf(sortFields);
  }

  public Integer getResultLimit() {
    return resultLimit;
  }

  public void setResultLimit(Integer resultLimit) {
    this.resultLimit = firstNonNull(resultLimit, 0);
  }

  public String getCssId() {
    return cssId;
  }

  public void setCssId(String cssId) {
    this.cssId = Strings.nullToEmpty(cssId);
  }

  public List<String> getCssClasses() {
    return cssClasses;
  }

  public void setCssClasses(List<String> cssClasses) {
    this.cssClasses = ImmutableList.copyOf(cssClasses);
  }

  public SpaceReference getHeaderLayout() {
    return headerLayout;
  }

  public void setHeaderLayout(SpaceReference headerLayout) {
    this.headerLayout = headerLayout;
  }

  public SpaceReference getRowLayout() {
    return rowLayout;
  }

  public void setRowLayout(SpaceReference rowLayout) {
    this.rowLayout = rowLayout;
  }

  public List<ColumnConfig> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnConfig> columns) {
    for (ColumnConfig colCfg : columns) {
      colCfg.setTableConfig(this);
    }
    this.columns = columns.stream().sorted(naturalOrder()).collect(toList());
  }

  public boolean isHeaderMode() {
    return headerMode;
  }

  public void setHeaderMode(boolean headerMode) {
    this.headerMode = headerMode;
  }

  @Override
  public String toString() {
    return "TableConfig [documentReference=" + documentReference + ", type=" + type + ", query="
        + query + ", sortFields=" + sortFields + ", resultLimit=" + resultLimit + ", cssId=" + cssId
        + ", cssClasses=" + cssClasses + ", rowLayout=" + rowLayout + ", headerLayout="
        + headerLayout + ", columns=" + columns + "]";
  }

}
