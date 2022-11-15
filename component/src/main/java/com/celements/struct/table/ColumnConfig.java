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
    this.name = Strings.nullToEmpty(name).trim().replaceAll("\\W+", "_");
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = Strings.nullToEmpty(title);
  }

  public String getContent() {
    if (getTableConfig().isHeaderMode()) {
      return getTitle();
    } else {
      return content;
    }
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
