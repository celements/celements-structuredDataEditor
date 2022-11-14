package com.celements.struct.table;

import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.presentation.IPresentationTypeRole;
import com.google.common.collect.ImmutableSet;

@ComponentRole
public interface ITablePresentationType extends IPresentationTypeRole<TableConfig> {

  String NAME = "struct-table";
  String CSS_CLASS = "struct_table";
  Set<String> EDIT_ACTIONS = ImmutableSet.of("edit", "inline");

}
