package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;

import java.util.Set;

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
      System.out.println("<<<<<<<<<<<<<<< SelectTagAutocompletePageType collectAttributes IN: ");
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addNonEmptyAttribute("name", getStructDataEditorService().getAttributeName(
          cellDoc, modelContext.getDoc()).or(""));
      // all values on a doc for a specific string field
      Set<SelectAutocompleteRole> values = XWikiObjectFetcher.on(cellDoc).filter(
          selectTagAutocomplete).iter().transformAndConcat(new FieldGetterFunction<>(
              xObjFieldAccessor, SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE)).toSet();
      System.out.println("<<<<<<<<<<<<<<< SelectTagAutocompletePageType collectAttributes values: "
          + values);
      for (SelectAutocompleteRole selectAutocompleteRole : values) {
        System.out.println("<<<<<<<<<<<<<<< SelectTagAutocompletePageType collectAttributes name: "
            + selectAutocompleteRole.getName());
        System.out.println("<<<<<<<<<<<<<<< SelectTagAutocompletePageType collectAttributes css: "
            + selectAutocompleteRole.getCssClass());
        System.out.println("<<<<<<<<<<<<<<< SelectTagAutocompletePageType collectAttributes js: "
            + selectAutocompleteRole.getJsFilePath());
      }

      // modelAccess.getFieldValue(cellDoc, FIELD_AUTOCOMPLETE_TYPE);
      //
      // XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc).filter(selectTagAutocomplete);
      // if (fetcher.filter(SelectTagAutocompleteEditorClass.FIELD_AUTOCOMPLETE_TYPE, Arrays.asList(
      // AutocompleteType.place)).exists()) {
      // attrBuilder.addCssClasses("autocompletePlaces");
      // }
      if (modelAccess.getFieldValue(cellDoc, FIELD_AUTOCOMPLETE_IS_MULTISELECT).or(false)) {
        attrBuilder.addNonEmptyAttribute("multiple", "multiple");
      }
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
