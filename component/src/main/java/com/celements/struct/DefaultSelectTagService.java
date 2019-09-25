package com.celements.struct;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.SelectAutocompleteRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.celements.structEditor.fields.SelectTagPageType;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
final public class DefaultSelectTagService implements SelectTagServiceRole {

  final private static Logger LOGGER = LoggerFactory.getLogger(DefaultSelectTagService.class);

  @Requirement(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
  private ClassDefinition selectTagAutocomplete;

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjFieldAccessor;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private StructUtilServiceRole structUtils;

  @Override
  public Optional<SelectAutocompleteRole> getTypeImpl(DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      return XWikiObjectFetcher.on(cellDoc)
          .fetchField(SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE)
          .first().toJavaUtil()
          .flatMap(list -> list.stream().findFirst());
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return Optional.empty();
  }

  @Override
  public Optional<DocumentReference> getSelectCellDocRef(DocumentReference cellDocRef) {
    Optional<DocumentReference> selectCellDocRef = Optional.empty();
    try {
      selectCellDocRef = structUtils.findParentCell(modelAccess.getDocument(cellDocRef),
          SelectTagPageType.PAGETYPE_NAME).map(selectDoc -> selectDoc.getDocumentReference());
      LOGGER.debug("getSelectCellDocRef: '{}' for cell '{}'", selectCellDocRef, cellDocRef);
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("parent on doc '{}' doesn't exist", cellDocRef, exc);
    }
    return selectCellDocRef;
  }

}
