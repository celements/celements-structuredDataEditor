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
package com.celements.structEditor.fields;

import java.util.List;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TextAreaTagPageType.PAGETYPE_NAME)
public class TextAreaTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "TextAreaTag";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return PAGETYPE_NAME + "View";
  }

  @Override
  public Optional<String> tagName() {
    return Optional.of("textarea");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("structEditTextArea");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc);
      XWikiDocument onDoc = modelContext.getDocument().orElse(null);
      getStructDataEditorService().getAttributeName(cellDoc, onDoc)
          .ifPresent(name -> attrBuilder.addUniqAttribute("name", name));
      fetcher.fetchField(TextAreaFieldEditorClass.FIELD_ROWS).findFirst()
          .ifPresent(rows -> attrBuilder.addUniqAttribute("rows", rows.toString()));
      fetcher.fetchField(TextAreaFieldEditorClass.FIELD_COLS).findFirst()
          .ifPresent(cols -> attrBuilder.addUniqAttribute("cols", cols.toString()));
      if (fetcher.fetchField(TextAreaFieldEditorClass.FIELD_IS_RICHTEXT).findFirst().orElse(false)) {
        attrBuilder.addCssClasses(List.of("mceEditor", "tinyMCE", "tinyMCEV4"));
      }
    } catch (DocumentNotExistsException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
