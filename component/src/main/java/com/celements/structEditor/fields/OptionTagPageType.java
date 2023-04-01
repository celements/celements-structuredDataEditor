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

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.celements.structEditor.classes.OptionTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.struct.SelectTagServiceRole;

@Component(OptionTagPageType.PAGETYPE_NAME)
public class OptionTagPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "OptionTag";

  static final String VIEW_TEMPLATE_NAME = "OptionTagView";

  @Requirement
  private SelectTagServiceRole selectTagService;

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
    return Optional.of("option");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(modelAccess.getDocument(cellDocRef));
      selectTagService.getSelectCellDocRef(cellDocRef)
          .map(rethrowFunction(modelAccess::getDocument))
          .ifPresent(selectCellDoc -> {
            Optional<String> optionValue = fetcher.fetchField(FIELD_VALUE).findFirst();
            Optional<String> cellValue = getStructDataEditorService().getRequestOrCellValue(
                selectCellDoc, modelContext.getDocument().orElse(null));
            if ((cellValue.isPresent() && cellValue.equals(optionValue)) || (!cellValue.isPresent()
                && fetcher.fetchField(FIELD_SELECTED).findFirst().orElse(false))) {
              attrBuilder.addEmptyAttribute("selected");
            }
          });
      if (fetcher.fetchField(FIELD_DISABLED).findFirst().orElse(false)) {
        attrBuilder.addEmptyAttribute("disabled");
      }
      fetcher.fetchField(FIELD_VALUE).findFirst()
          .ifPresent(value -> attrBuilder.addUniqAttribute("value", value));
      fetcher.fetchField(FIELD_LABEL).findFirst()
          .ifPresent(label -> attrBuilder.addUniqAttribute("label", label));
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
