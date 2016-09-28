package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.SelectTagEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.StructuredDataEditorClasses;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.objects.BaseObject;

@Component(SelectTagPageType.SELECT_TAG_PAGETYPE_NAME)
public class SelectTagPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(SelectTagPageType.class);

  @Requirement(SelectTagEditorClass.CLASS_DEF_HINT)
  private StructEditorClass selectTagEditorClass;

  @Requirement(StructuredDataEditorClasses.CLASS_DEF_HINT)
  private StructEditorClass structuredDataEditorClass;

  public static final String SELECT_TAG_PAGETYPE_NAME = "SelectTag";

  static final String VIEW_TEMPLATE_NAME = "SelectTagView";

  @Override
  public String getName() {
    return SELECT_TAG_PAGETYPE_NAME;
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
    BaseObject structuredDataEditorConfig;
    BaseObject selectConfig;
    DocumentReference structuredDataEditorClassRef = structuredDataEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    DocumentReference selectClassRef = selectTagEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    try {
      structuredDataEditorConfig = modelAccess.getXObject(cellDocRef, structuredDataEditorClassRef);
      selectConfig = modelAccess.getXObject(cellDocRef, selectClassRef);

      if (selectConfig.getIntValue("select_tag_is_multiselect") == 1) {
        attrBuilder.addCssClasses("celMultiselect");
      }
      if (selectConfig.getIntValue("select_tag_is_bootstrap") == 1) {
        attrBuilder.addCssClasses("celBootstrap");
      }
      if (!Strings.isNullOrEmpty(structuredDataEditorConfig.getStringValue(
          "select_tag_separator"))) {

      }
      attrBuilder.addNonEmptyAttribute("name", structuredDataEditorConfig.getStringValue(
          "edit_field_class_fullname") + "_0_" + structuredDataEditorConfig.getStringValue(
              "edit_field_name"));
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", structuredDataEditorClassRef,
          selectClassRef, exc);
    }
  }

}
