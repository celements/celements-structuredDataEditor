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
import com.celements.model.field.FieldGetterFunction;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.SelectAutocompleteRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultSelectTagService implements SelectTagServiceRole {

  private static Logger LOGGER = LoggerFactory.getLogger(DefaultSelectTagService.class);

  @Requirement(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
  private ClassDefinition selectTagAutocomplete;

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjFieldAccessor;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Override
  public Optional<SelectAutocompleteRole> getTypeImpl(DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      FluentIterable<BaseObject> objIter = XWikiObjectFetcher.on(cellDoc).filter(
          selectTagAutocomplete).iter();
      return objIter.transformAndConcat(new FieldGetterFunction<>(xObjFieldAccessor,
          SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE)).first().toJavaUtil();
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
    return Optional.empty();
  }

}
