package com.celements.structEditor.fields;

import static com.celements.structEditor.classes.FormFieldEditorClass.*;
import static java.lang.Boolean.*;

import java.util.Collection;
import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.velocity.XWikiVelocityException;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.structEditor.classes.FormFieldEditorClass.Method;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(FormFieldPageType.PAGETYPE_NAME)
public class FormFieldPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "FormField";

  static final String VIEW_TEMPLATE_NAME = "FormFieldView";

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
    return Optional.of("form");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("celAddValidationToForm inactive");
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      XWikiObjectFetcher fetcher = XWikiObjectFetcher.on(cellDoc);
      if (fetcher.fetchField(FIELD_SEND_DATA_ENCODED).stream().anyMatch(TRUE::equals)) {
        attrBuilder.addNonEmptyAttribute("enctype", "multipart/form-data");
      }
      attrBuilder.addNonEmptyAttribute("action", getVelocityFieldValue(cellDoc, FIELD_ACTION)
          .orElse("?"));
      attrBuilder.addNonEmptyAttribute("method", fetcher.fetchField(FIELD_METHOD)
          .stream().flatMap(Collection::stream)
          .findFirst().orElse(Method.POST)
          .name());
      attrBuilder.addNonEmptyAttribute("autocomplete", "off");
    } catch (DocumentNotExistsException | XWikiVelocityException exc) {
      log.error("failed to add all attributes for '{}'", cellDocRef, exc);
    }
  }

}
