package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.FieldGetterFunction;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.SelectAutocompleteRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(SelectTagAutocompletePageType.PAGETYPE_NAME)
public class SelectTagAutocompletePageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(SelectTagAutocompletePageType.class);

  public static final String PAGETYPE_NAME = "SelectTagAutocomplete";

  static final String VIEW_TEMPLATE_NAME = "SelectTagAutocompleteView";

  @Requirement(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
  private ClassDefinition selectTagAutocomplete;

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjFieldAccessor;

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.of("select");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, modelContext.getDoc()).or(""));
      FluentIterable<BaseObject> objIter = XWikiObjectFetcher.on(cellDoc).filter(
          selectTagAutocomplete).iter();
      Optional<SelectAutocompleteRole> type = objIter.transformAndConcat(new FieldGetterFunction<>(
          xObjFieldAccessor, SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE)).first();
      if (type.isPresent()) {
        attrBuilder.addCssClasses(type.get().getCssClass());
      }
      boolean multiselect = objIter.transform(new FieldGetterFunction<>(xObjFieldAccessor,
          SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_IS_MULTISELECT)).first().or(false);
      if (multiselect) {
        attrBuilder.addNonEmptyAttribute("multiple", "multiple");
      }
      Optional<String> separator = objIter.transform(new FieldGetterFunction<>(xObjFieldAccessor,
          SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_SEPARATOR)).first();
      if ((separator.isPresent())) {
        attrBuilder.addNonEmptyAttribute("data-separator", separator.get());
      }
      String docFN = getStructDataEditorService().getCellValueAsString(cellDocRef,
          modelContext.getDoc()).or("");
      if (!Strings.isNullOrEmpty(docFN)) {
        attrBuilder.addNonEmptyAttribute("data-value", docFN);
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
