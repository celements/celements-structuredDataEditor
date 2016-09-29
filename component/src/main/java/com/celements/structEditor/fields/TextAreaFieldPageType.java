package com.celements.structEditor.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.structEditor.classes.StructEditorClass;
import com.celements.structEditor.classes.TextAreaFieldEditorClass;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(TextAreaFieldPageType.INPUT_FIELD_PAGETYPE_NAME)
public class TextAreaFieldPageType extends AbstractStructFieldPageType {

  private static Logger LOGGER = LoggerFactory.getLogger(TextAreaFieldPageType.class);

  public static final String INPUT_FIELD_PAGETYPE_NAME = "TextAreaField";

  static final String VIEW_TEMPLATE_NAME = "TextAreaFieldView";

  @Requirement(TextAreaFieldEditorClass.CLASS_DEF_HINT)
  private StructEditorClass textAreaFieldEditorClass;

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
    return Optional.of("textarea");
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      addNameAttribute(attrBuilder, cellDoc);
    } catch (DocumentNotExistsException exc) {
      LOGGER.error("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
