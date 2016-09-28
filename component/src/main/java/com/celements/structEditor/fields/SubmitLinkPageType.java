package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.google.common.base.Optional;
import com.xpn.xwiki.objects.BaseObject;

@Component(SubmitLinkPageType.INPUT_FIELD_PAGETYPE_NAME)
public class SubmitLinkPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(SubmitLinkPageType.class);

  public static final String INPUT_FIELD_PAGETYPE_NAME = "SubmitLink";

  static final String VIEW_TEMPLATE_NAME = "SubmitLinkView";

  @Requirement(StructuredDataEditorClass.CLASS_DEF_HINT)
  private StructEditorClass structuredDataEditorClasses;

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
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
    BaseObject structuredDataEditorConfig;
    DocumentReference structuredDataEditorClasseRef = structuredDataEditorClasses.getClassRef(
        cellDocRef.getWikiReference());
    attrBuilder.addCssClasses("celSubmitFormWithValidation submit");
    attrBuilder.addNonEmptyAttribute("href", "#");
    try {
      structuredDataEditorConfig = modelAccess.getXObject(cellDocRef,
          structuredDataEditorClasseRef);
      attrBuilder.addNonEmptyAttribute("name", structuredDataEditorConfig.getStringValue(
          "edit_field_class_fullname") + "_0_" + structuredDataEditorConfig.getStringValue(
              "edit_field_name"));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", structuredDataEditorClasseRef,
          exc);
    }
  }

}
