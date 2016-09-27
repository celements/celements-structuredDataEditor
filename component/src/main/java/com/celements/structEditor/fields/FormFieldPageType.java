package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.structEditor.classes.FormFieldEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.objects.BaseObject;

@Component(FormFieldPageType.FORM_FIELD_PAGETYPE_NAME)
public class FormFieldPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(SelectTagPageType.class);

  @Requirement(FormFieldEditorClass.CLASS_DEF_HINT)
  private StructEditorClass formFieldEditorClass;

  @Requirement
  private ModelContext modelContext;

  public static final String FORM_FIELD_PAGETYPE_NAME = "FormField";

  static final String VIEW_TEMPLATE_NAME = "FormFieldView";

  @Override
  public String getName() {
    return FORM_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  @Override
  public Optional<String> defaultTagName() {
    return Optional.of("form");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    attrBuilder.addCssClasses("celAddValidationToForm inactive");
    BaseObject formFieldConfig;
    DocumentReference formFieldClassRef = formFieldEditorClass.getClassRef(
        cellDocRef.getWikiReference());
    try {
      formFieldConfig = modelAccess.getXObject(cellDocRef, formFieldClassRef);
      if (formFieldConfig.getIntValue("form_field_send_data_encoded") == 1) {
        attrBuilder.addNonEmptyAttribute("enctype", "multipart/form-data");
      }
      String method = new String();
      if (!Strings.isNullOrEmpty(formFieldConfig.getStringValue("form_field_method"))) {
        method = formFieldConfig.getStringValue("form_field_method");
      } else {
        method = "post";
      }
      if (!Strings.isNullOrEmpty(formFieldConfig.getStringValue("form_field_action"))) {
        attrBuilder.addNonEmptyAttribute("action", formFieldConfig.getStringValue(
            "form_field_action"));
      }
      attrBuilder.addNonEmptyAttribute("method", method);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("Document {} or Document {} does not exist {}", formFieldClassRef, exc);
    }
  }

}
