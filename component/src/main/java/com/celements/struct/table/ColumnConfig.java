package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

@NotThreadSafe
public class ColumnConfig implements Comparable<ColumnConfig> {

  private TableConfig tableConfig;

  private int number = 0;
  private int order = -1;
  private String name = "";
  private String title = "";
  private String content = "";
  private List<String> cssClasses = ImmutableList.of();

  private boolean headerMode = false;

  public TableConfig getTableConfig() {
    return tableConfig;
  }

  public void setTableConfig(TableConfig tableConfig) {
    this.tableConfig = tableConfig;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = firstNonNull(order, -1);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = Strings.nullToEmpty(name);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = Strings.nullToEmpty(title);
  }

  public String getContent() {
    if (headerMode) {
      headerMode = false;
      return getTitle();
    }
    return content;
  }

  public void setContent(String content) {
    this.content = Strings.nullToEmpty(content);
  }

  public List<String> getCssClasses() {
    return cssClasses;
  }

  public void setCssClasses(List<String> cssClasses) {
    this.cssClasses = ImmutableList.copyOf(cssClasses);
  }

  public void setHeaderMode() {
    headerMode = true;
  }

  @Override
  public int compareTo(ColumnConfig other) {
    ComparisonChain cmp = ComparisonChain.start();
    cmp = cmp.compare(this.getOrder(), other.getOrder(), Ordering.natural());
    cmp = cmp.compare(this.getNumber(), other.getNumber(), Ordering.natural());
    return cmp.result();
  }

  @Override
  public String toString() {
    return "ColumnConfig [number=" + number + ", order=" + order + ", name=" + name + ", title="
        + title + ", content=" + content + ", cssClasses=" + cssClasses + "]";
  }

}
