package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;
import static java.lang.Boolean.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.struct.SelectTagServiceRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(SelectTagAutocompletePageType.PAGETYPE_NAME)
public class SelectTagAutocompletePageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "SelectTagAutocomplete";

  static final String VIEW_TEMPLATE_NAME = "SelectTagAutocompleteView";

  @Requirement(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
  private ClassDefinition classDef;

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
    return Optional.of("select");
  }

  @Override
  public void collectAttributes(final AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      XWikiDocument currDoc = modelContext.getCurrentDoc().orNull();
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, currDoc).orElse(""));
      attrBuilder.addCssClasses("structAutocomplete");
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc).filter(classDef);
      selectTagService.getTypeImpl(cellDocRef).ifPresent(type -> {
        attrBuilder.addCssClasses(type.getName());
        attrBuilder.addNonEmptyAttribute("data-autocomplete-type", type.getName());
      });
      if (fetcher.fetchField(FIELD_AUTOCOMPLETE_IS_MULTISELECT).stream().anyMatch(TRUE::equals)) {
        attrBuilder.addNonEmptyAttribute("multiple", "multiple");
      }
      fetcher.fetchField(FIELD_AUTOCOMPLETE_SEPARATOR).stream().findFirst()
          .ifPresent(separator -> attrBuilder.addNonEmptyAttribute("data-separator", separator));
      getStructDataEditorService().getCellValueAsString(cellDocRef, currDoc)
          .ifPresent(docFN -> attrBuilder.addNonEmptyAttribute("data-value", docFN));
    } catch (DocumentNotExistsException exc) {
      log.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
