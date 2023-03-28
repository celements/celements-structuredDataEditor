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

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(NumberTagPageType.PAGETYPE_NAME)
public class NumberTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "NumberTag";

  static final String VIEW_TEMPLATE_NAME = "NumberTagView";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  public Optional<String> tagName() {
    return Optional.of("input");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addUniqAttribute("type", "number");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      getStructDataEditorService()
          .getAttributeName(cellDoc, modelContext.getDocument().orElse(null))
          .ifPresent(name -> attrBuilder.addUniqAttribute("name", name));
      getStructDataEditorService()
          .getCellValueAsString(cellDoc, modelContext.getDocument().orElse(null))
          .ifPresent(value -> attrBuilder.addUniqAttribute("value", value));
    } catch (DocumentNotExistsException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
