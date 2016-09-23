package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.context.ModelContext;
import com.celements.structEditor.classes.SelectTagEditorClass;
import com.celements.structEditor.classes.StructEditorClass;
import com.xpn.xwiki.objects.BaseObject;

@Component(SelectTagPageType.SELECT_TAG_PAGETYPE_NAME)
public class SelectTagPageType extends AbstractStructFieldPageType {

  @Requirement(SelectTagEditorClass.CLASS_DEF_HINT)
  private StructEditorClass selectTagEditorClass;

  @Requirement
  private ModelContext modelContext;

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
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    BaseObject selectConfig;
    // try {
    System.out.println("\n<<<<<<<<<<<<<<<<<<<<<<<< TEST");
    DocumentReference classRef = selectTagEditorClass.getClassRef(cellDocRef.getWikiReference());
    // selectConfig = modelAccess.getXObject(cellDocRef, classRef);
    // System.out.println(
    // "\n<<<<<<<<<<<<<<<<<<<<<<<< SelectTagPageType collectAttributes selectConfig: ["
    // + selectConfig + "]\n" + "\n<<<<<<<<<<<<<<<<<<<<<<<< cellDocRef: [" + cellDocRef
    // + "]\n" + "\n<<<<<<<<<<<<<<<<<<<<<<<< selectTagEditorClass.getClassRef(): ["
    // + selectTagEditorClass.getClassRef(cellDocRef.getWikiReference()) + "]\n"
    // + "selectConfig.getIntValue('select_tag_is_multiselect'): ["
    // + selectConfig.getIntValue("select_tag_is_multiselect") + "]");
    //
    // if (selectConfig.getIntValue("select_tag_is_multiselect") == 1) {
    // attrBuilder.addCssClasses("celMultiselect");
    // }
    // attrBuilder.addNonEmptyAttribute("selected", "");
    // attrBuilder.addNonEmptyAttribute("name", modelAccess.getXObject(
    // modelContext.getDoc().getDocumentReference(), get));
    // } catch (DocumentNotExistsException exp) {
    // // TODO Auto-generated catch block
    // exp.printStackTrace();
    // }
  }

}
