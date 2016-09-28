package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.HiddenTagEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.google.common.base.Optional;
import com.xpn.xwiki.objects.BaseObject;

@Component(HiddenTagPageType.INPUT_FIELD_PAGETYPE_NAME)
public class HiddenTagPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(HiddenTagPageType.class);

  public static final String INPUT_FIELD_PAGETYPE_NAME = "HiddenTag";

  static final String VIEW_TEMPLATE_NAME = "HiddenTagView";

  @Requirement(HiddenTagEditorClass.CLASS_DEF_HINT)
  private StructEditorClass hiddenTagEditorClass;

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
    return Optional.of("input");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    BaseObject hiddenConfig;
    DocumentReference structuredDataEditorClasseRef = structuredDataEditorClasses.getClassRef(
        cellDocRef.getWikiReference());
    DocumentReference hiddenClassRef = hiddenTagEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    try {
      hiddenConfig = modelAccess.getXObject(cellDocRef, hiddenClassRef);
      attrBuilder.addNonEmptyAttribute("type", "hidden");
      attrBuilder.addNonEmptyAttribute("name", hiddenConfig.getStringValue("hidden_tag_name"));
      attrBuilder.addNonEmptyAttribute("value", hiddenConfig.getStringValue("hidden_tag_value"));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", structuredDataEditorClasseRef,
          hiddenClassRef, exc);
    }
  }

}
