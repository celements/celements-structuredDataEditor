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
package com.celements.structEditor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.ClassReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;

@ComponentRole
public interface StructuredDataEditorService {

  Set<String> LABELS_AND = Set.of("struct-obj-filter", "struct-obj-filter-and");
  Set<String> LABELS_OR = Set.of("struct-obj-filter-or");

  @NotNull
  Optional<String> getAttributeName(@NotNull XWikiDocument cellDoc, @Nullable XWikiDocument onDoc);

  @NotNull
  Optional<String> getPrettyName(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<ClassReference> getCellClassRef(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<String> getCellFieldName(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<BaseClass> getCellXClass(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<PropertyClass> getCellPropertyClass(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<BaseObject> getXObjectInStructEditor(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  @NotNull
  Stream<BaseObject> streamXObjectsForCell(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  @NotNull
  Optional<String> getCellValueAsString(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  @NotNull
  Optional<String> getRequestOrCellValue(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  Optional<String> getDateFormatFromField(XWikiDocument cellDoc);

  @NotNull
  Optional<Date> getCellDateValue(@NotNull XWikiDocument cellDoc, @Nullable XWikiDocument onDoc);

  @NotNull
  List<String> getCellListValue(@NotNull XWikiDocument cellDoc, @Nullable XWikiDocument onDoc);

  @NotNull
  Optional<Object> getCellValue(@NotNull XWikiDocument cellDoc, @Nullable XWikiDocument onDoc);

  boolean hasEditField(@NotNull XWikiDocument cellDoc);

  boolean isMultilingual(@NotNull XWikiDocument cellDoc);

  @NotNull
  Optional<String> getLangNameAttribute(@NotNull XWikiDocument cellDoc,
      @Nullable XWikiDocument onDoc);

  @NotNull
  Map<String, String> getCellPossibleValues(@NotNull XWikiDocument cellDoc);

}
