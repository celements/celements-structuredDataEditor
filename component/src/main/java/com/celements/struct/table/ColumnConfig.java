package com.celements.struct.table;

import static com.google.common.base.MoreObjects.*;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class ColumnConfig implements Comparable<ColumnConfig> {

  private String title = "";
  private String content = "";
  private int order = 0;
  private List<String> cssClasses = ImmutableList.of();

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = Strings.nullToEmpty(title);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = Strings.nullToEmpty(content);
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = firstNonNull(order, 0);
  }

  public List<String> getCssClasses() {
    return cssClasses;
  }

  public void setCssClasses(List<String> cssClasses) {
    this.cssClasses = ImmutableList.copyOf(cssClasses);
  }

  @Override
  public int compareTo(ColumnConfig other) {
    return Integer.compare(this.order, other.order);
  }

  @Override
  public String toString() {
    return "ColumnConfig [title=" + title + ", content=" + content + ", order=" + order
        + ", cssClasses=" + cssClasses + "]";
  }

}
