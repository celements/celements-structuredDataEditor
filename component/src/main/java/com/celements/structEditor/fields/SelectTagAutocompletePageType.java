package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.SelectTagAutocompleteEditorClass.*;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.struct.SelectTagServiceRole;
import com.celements.structEditor.classes.SelectTagAutocompleteEditorClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component(SelectTagAutocompletePageType.PAGETYPE_NAME)
public class SelectTagAutocompletePageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "SelectTagAutocomplete";

  static final String VIEW_TEMPLATE_NAME = "SelectTagAutocompleteView";

  @Requirement(SelectTagAutocompleteEditorClass.CLASS_DEF_HINT)
  private ClassDefinition selectTagAutocomplete;

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjFieldAccessor;

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
      XWikiObjectFetcher xObjFetcher = XWikiObjectFetcher.on(cellDoc).filter(selectTagAutocomplete);
      selectTagService.getTypeImpl(cellDocRef)
          .ifPresent(type -> attrBuilder.addCssClasses(type.getCssClass()));
      if (xObjFetcher.fetchField(FIELD_AUTOCOMPLETE_IS_MULTISELECT).first().or(false)) {
        attrBuilder.addNonEmptyAttribute("multiple", "multiple");
      }
      xObjFetcher.fetchField(FIELD_AUTOCOMPLETE_SEPARATOR).first().toJavaUtil()
          .ifPresent(separator -> attrBuilder.addNonEmptyAttribute("data-separator", separator));
      getStructDataEditorService().getCellValueAsString(cellDocRef, currDoc)
          .ifPresent(docFN -> attrBuilder.addNonEmptyAttribute("data-value", docFN));
    } catch (DocumentNotExistsException exc) {
      log.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
