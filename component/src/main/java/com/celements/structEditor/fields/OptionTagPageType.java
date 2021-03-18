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

import static com.celements.structEditor.classes.OptionTagEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
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
      Optional<DocumentReference> selectCellDocRef = selectTagService
          .getSelectCellDocRef(cellDocRef);
      if (selectCellDocRef.isPresent()) {
        Optional<String> optionValue = modelAccess.getFieldValue(cellDocRef, FIELD_VALUE)
            .toJavaUtil();
        Optional<String> cellValue = getStructDataEditorService().getCellValueAsString(
            selectCellDocRef.get(), modelContext.getCurrentDoc().orNull());
        if ((cellValue.isPresent() && cellValue.equals(optionValue)) || (!cellValue.isPresent()
            && modelAccess.getFieldValue(cellDocRef, FIELD_SELECTED).or(false))) {
          attrBuilder.addEmptyAttribute("selected");
        }
      }
      if (modelAccess.getFieldValue(cellDocRef, FIELD_DISABLED).or(false)) {
        attrBuilder.addEmptyAttribute("disabled");
      }
      attrBuilder.addNonEmptyAttribute("value", modelAccess.getFieldValue(cellDocRef,
          FIELD_VALUE).or(""));
      attrBuilder.addNonEmptyAttribute("label", modelAccess.getFieldValue(cellDocRef,
          FIELD_LABEL).or(""));
    } catch (DocumentNotExistsException exc) {
      log.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
