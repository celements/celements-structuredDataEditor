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

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.presentation.PresentationNodeData;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

@NotThreadSafe
public class TableConfig implements PresentationNodeData {

  private DocumentReference documentReference;
  private String query = "";
  private List<String> sortFields = ImmutableList.of();
  private int resultLimit = 0;
  private String cssId = "";
  private List<String> cssClasses = ImmutableList.of();
  private List<ColumnConfig> columns = ImmutableList.of();

  public DocumentReference getDocumentReference() {
    return documentReference;
  }

  public void setDocumentReference(DocumentReference documentReference) {
    this.documentReference = documentReference;
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

  public List<ColumnConfig> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnConfig> columns) {
    for (ColumnConfig colCfg : columns) {
      colCfg.setTableConfig(this);
    }
    this.columns = FluentIterable.from(columns).toSortedList(Ordering.natural());
  }

  public void setHeaderMode(boolean headerMode) {
    for (ColumnConfig col : columns) {
      col.setHeaderMode(headerMode);
    }
  }

  @Override
  public String toString() {
    return "TableConfig [documentReference=" + documentReference + ", query=" + query
        + ", sortFields=" + sortFields + ", resultLimit=" + resultLimit + ", cssId=" + cssId
        + ", cssClasses=" + cssClasses + ", columns=" + columns + "]";
  }

}
