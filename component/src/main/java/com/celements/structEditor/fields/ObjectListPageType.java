package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(ObjectListPageType.PAGETYPE_NAME)
public class ObjectListPageType extends AbstractStructFieldPageType {

  public static final String PAGETYPE_NAME = "ObjectList";

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return PAGETYPE_NAME + "View";
  }

  @Override
  public void collectAttributes(final AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    try {
      XWikiDocument cellDoc = modelAccess.getDocument(cellDocRef);
      attrBuilder.addCssClasses("struct_object_list");
      getStructDataEditorService().getCellClassRef(cellDoc)
          .ifPresent(classRef -> attrBuilder.addNonEmptyAttribute("data-struct-class",
              classRef.serialize().replace('.', '_')));
    } catch (DocumentNotExistsException exc) {
      log.warn("cell doesn't exist '{}'", cellDocRef, exc);
    }
  }

}
