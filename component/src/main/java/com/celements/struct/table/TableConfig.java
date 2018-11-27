package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.List;

import com.celements.navigation.presentation.PresentationNodeData;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

public class TableConfig implements PresentationNodeData {

  private String query = "";
  private List<String> sortFields = ImmutableList.of();
  private int resultLimit = 0;
  private List<String> tableCssClasses = ImmutableList.of();
  private List<String> rowCssClasses = ImmutableList.of();
  private List<ColumnConfig> columns = ImmutableList.of();

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

  public List<String> getTableCssClasses() {
    return tableCssClasses;
  }

  public void setTableCssClasses(List<String> cssClasses) {
    this.tableCssClasses = ImmutableList.copyOf(cssClasses);
  }

  public List<String> getRowCssClasses() {
    return rowCssClasses;
  }

  public void setRowCssClasses(List<String> cssClasses) {
    this.rowCssClasses = ImmutableList.copyOf(cssClasses);
  }

  public List<ColumnConfig> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnConfig> columns) {
    this.columns = FluentIterable.from(columns).toSortedList(Ordering.natural());
  }

  @Override
  public String toString() {
    return "TableConfig [query=" + query + ", sortFields=" + sortFields + ", resultLimit="
        + resultLimit + ", tableCssClasses=" + tableCssClasses + ", rowCssClasses=" + rowCssClasses
        + ", columnCount=" + columns.size() + "]";
  }

}
