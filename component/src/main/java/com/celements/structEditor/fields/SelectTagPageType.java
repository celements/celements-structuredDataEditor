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

import static com.celements.structEditor.classes.SelectTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(SelectTagPageType.PAGETYPE_NAME)
public class SelectTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "SelectTag";

  static final String VIEW_TEMPLATE_NAME = "SelectTagView";

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
    return Optional.of("select");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc);
      XWikiDocument currDoc = modelContext.getDocument().orElse(null);
      getStructDataEditorService().getAttributeName(cellDoc, currDoc)
          .ifPresent(name -> attrBuilder.addUniqAttribute("name", name));
      boolean isBootstrap = fetcher.fetchField(FIELD_IS_BOOTSTRAP).findFirst().orElse(false);
      boolean isMultiselect = fetcher.fetchField(FIELD_IS_MULTISELECT).findFirst().orElse(false);
      if (isBootstrap || isMultiselect) {
        attrBuilder.addCssClasses("celBootstrap");
        fetcher.fetchField(FIELD_BOOTSTRAP_CONFIG).findFirst()
            .ifPresent(cfg -> attrBuilder.addUniqAttribute("data-bootstrapConfig", cfg));
      }
      if (isMultiselect) {
        attrBuilder.addCssClasses("celMultiselect");
        attrBuilder.addUniqAttribute("multiple", "multiple");

      }
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
