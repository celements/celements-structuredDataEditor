package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.OptionTagEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.google.common.base.Optional;
import com.xpn.xwiki.objects.BaseObject;

@Component(OptionTagPageType.PAGETYPE_NAME)
public class OptionTagPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(OptionTagPageType.class);

  @Requirement(OptionTagEditorClass.CLASS_DEF_HINT)
  private StructEditorClass optionTagEditorClass;

  public static final String PAGETYPE_NAME = "OptionTag";

  static final String VIEW_TEMPLATE_NAME = "OptionTagView";

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
    return Optional.of("option");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    BaseObject optionConfig;
    DocumentReference optionClassRef = optionTagEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    try {
      optionConfig = modelAccess.getXObject(cellDocRef, optionClassRef);

      if (optionConfig.getIntValue("option_tag_is_selected") == 1) {
        attrBuilder.addEmptyAttribute("selected");
      }
      if (optionConfig.getIntValue("option_tag_is_disabled") == 1) {
        attrBuilder.addEmptyAttribute("disabled");
      }
      attrBuilder.addNonEmptyAttribute("value", optionConfig.getStringValue("option_tag_value"));
      attrBuilder.addNonEmptyAttribute("label", optionConfig.getStringValue("option_tag_label"));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", optionClassRef, exc);
    }
  }

}
