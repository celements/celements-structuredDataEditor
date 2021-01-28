package com.celements.struct;

import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;
import static com.celements.structEditor.fields.SelectTagPageType.*;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.SelectAutocompleteRole;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DefaultSelectTagService implements SelectTagServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSelectTagService.class);

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private StructUtilServiceRole structUtils;

  @Override
  public Optional<SelectAutocompleteRole> getTypeImpl(DocumentReference cellDocRef) {
    try {
      return XWikiObjectFetcher.on(modelAccess.getDocument(cellDocRef))
          .fetchField(FIELD_AUTOCOMPLETE_TYPE)
          .stream().flatMap(Collection::stream)
          .findFirst();
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
