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

import static com.celements.structEditor.classes.FormFieldEditorClass.*;
import static java.lang.Boolean.*;

import java.util.Collection;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.FormFieldEditorClass.Method;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(FormFieldPageType.PAGETYPE_NAME)
public class FormFieldPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "FormField";

  static final String VIEW_TEMPLATE_NAME = "FormFieldView";

  @Requirement(FormFieldEditorClass.CLASS_DEF_HINT)
  private ClassDefinition classDef;

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
    return Optional.of("form");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("celAddValidationToForm inactive");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      XWikiObjectFetcher objFetcher = XWikiObjectFetcher.on(cellDoc).filter(classDef);
      if (objFetcher.fetchField(FIELD_SEND_DATA_ENCODED).stream().anyMatch(TRUE::equals)) {
        attrBuilder.addNonEmptyAttribute("enctype", "multipart/form-data");
      }
      attrBuilder.addNonEmptyAttribute("action", getVelocityFieldValue(cellDoc, FIELD_ACTION)
          .orElse("?"));
      attrBuilder.addNonEmptyAttribute("method", objFetcher.fetchField(FIELD_METHOD)
          .stream().flatMap(Collection::stream)
          .findFirst().orElse(Method.POST)
          .name());
      attrBuilder.addNonEmptyAttribute("autocomplete", "off");
    } catch (DocumentNotExistsException | XWikiVelocityException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
