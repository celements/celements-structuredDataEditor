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

import static com.celements.structEditor.classes.HiddenTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(HiddenTagPageType.PAGETYPE_NAME)
public class HiddenTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "HiddenTag";

  static final String VIEW_TEMPLATE_NAME = "HiddenTagView";

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
    attrBuilder.addNonEmptyAttribute("type", "hidden");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      Optional<String> name = modelAccess.getFieldValue(cellDoc, FIELD_NAME).toJavaUtil();
      Optional<String> value = getVelocityFieldValue(cellDoc, FIELD_VALUE);
      if (getStructDataEditorService().hasEditField(cellDoc)) {
        XWikiDocument onDoc = modelContext.getCurrentDoc().orNull();
        if (!name.isPresent()) {
          name = getStructDataEditorService().getAttributeName(cellDoc, onDoc);
        }
        if (!value.isPresent()) {
          value = getStructDataEditorService().getCellValueAsString(cellDocRef, onDoc);
        }
      }
      if (!value.isPresent() && name.isPresent()) {
        final String paramName = name.get();
        value = modelContext.getRequest().toJavaUtil().map(
            request -> request.getParameter(paramName));
      }
      attrBuilder.addNonEmptyAttribute("name", name.orElse(cellDocRef.getName()));
      attrBuilder.addNonEmptyAttribute("value", value.orElse(""));
    } catch (DocumentNotExistsException | XWikiVelocityException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
