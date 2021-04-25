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
package com.celements.struct;

import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;
import static com.celements.structEditor.fields.SelectTagPageType.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.struct.edit.autocomplete.AutocompleteRole;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DefaultSelectTagService implements SelectTagServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSelectTagService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private StructUtilServiceRole structUtils;

  @Override
  public Optional<AutocompleteRole> getTypeImpl(DocumentReference cellDocRef) {
    try {
      return XWikiObjectFetcher.on(modelAccess.getDocument(cellDocRef))
          .fetchField(FIELD_AUTOCOMPLETE_TYPE)
          .stream().findFirst();
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return Optional.empty();
  }

  @Override
  public Optional<DocumentReference> getSelectCellDocRef(DocumentReference cellDocRef) {
    Optional<DocumentReference> selectCellDocRef = Optional.empty();
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      selectCellDocRef = structUtils.findParentCell(cellDoc, PAGETYPE_NAME)
          .map(XWikiDocument::getDocumentReference);
      LOGGER.debug("getSelectCellDocRef: '{}' for cell '{}'", selectCellDocRef, cellDocRef);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent on doc '{}' doesn't exist", cellDocRef, exc);
    }
    return selectCellDocRef;
  }

}
