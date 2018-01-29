package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.AutocompleteType;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(SelectTagAutocompletePageType.PAGETYPE_NAME)
public class SelectTagAutocompletePageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(SelectTagAutocompletePageType.class);

  public static final String PAGETYPE_NAME = "SelectTagAutocomplete";

  static final String VIEW_TEMPLATE_NAME = "SelectTagAutocompleteView";

  @Requirement(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
  private ClassDefinition selectTagAutocomplete;

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
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc).filter(selectTagAutocomplete);
      if (fetcher.filter(SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE, Arrays.asList(
          AutocompleteType.place)).exists()) {
        attrBuilder.addCssClasses("autocompletePlaces");
      }
      if (modelAccess.getFieldValue(cellDoc, FIELD_AUTOCOMPLETE_IS_MULTISELECT).or(false)) {
        attrBuilder.addNonEmptyAttribute("multiple", "multiple");
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
