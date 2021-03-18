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

@Component(ObjectListPageType.PAGETYPE_NAME)
public class ObjectListPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "ObjectList";

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
    return Optional.of("ul");
  }

  @Override
  public void collectAttributes(final AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addCssClasses("struct_object");
      getStructDataEditorService().getCellClassRef(cellDoc)
          .ifPresent(classRef -> attrBuilder.addNonEmptyAttribute("data-struct-class",
              classRef.serialize().replace('.', '_')));
    } catch (DocumentNotExistsException exc) {
      log.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
