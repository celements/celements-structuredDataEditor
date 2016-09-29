package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.google.common.base.Optional;

@Component(SubmitLinkPageType.PAGETYPE_NAME)
public class SubmitLinkPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "SubmitLink";

  static final String VIEW_TEMPLATE_NAME = "SubmitLinkView";

  @Requirement(StructuredDataEditorClass.CLASS_DEF_HINT)
  private StructEditorClass structuredDataEditorClasses;

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
    return Optional.of("a");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("celSubmitFormWithValidation submit");
    attrBuilder.addNonEmptyAttribute("href", "#");
  }

}
