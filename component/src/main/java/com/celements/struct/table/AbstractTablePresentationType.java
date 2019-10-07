package com.celements.struct.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.DivWriter;
import com.celements.cells.attribute.AttributeBuilder;
import com.celements.cells.attribute.DefaultAttributeBuilder;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.struct.StructDataService;
import com.celements.structEditor.StructuredDataEditorService;
import com.celements.structEditor.classes.StructuredDataEditorClass;
import com.celements.velocity.VelocityService;
import com.celements.web.service.IWebUtilsService;

public abstract class AbstractTablePresentationType implements IPresentationTypeRole<TableConfig> {

  protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  public static final String STRUCT_TABLE_DIR = "/templates/celStruct/table";
  public static final String CSS_CLASS = "struct_table";

  @Requirement(StructuredDataEditorClass.CLASS_DEF_HINT)
  protected ClassDefinition structFieldClassDef;

  @Requirement
  protected StructDataService structDataService;

  @Requirement
  protected StructuredDataEditorService structDataEditorService;

  @Requirement
  protected VelocityService velocityService;

  @Requirement
  protected IPageTypeResolverRole pageTypeResolver;

  @Requirement
  protected IWebUtilsService webUtils;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelUtils modelUtils;

  @Requirement
  protected ModelContext context;

  @Override
  public void writeNodeContent(StringBuilder writer, boolean isFirstItem, boolean isLastItem,
      DocumentReference nodeDocRef, boolean isLeaf, int numItem, TableConfig table) {
    writeNodeContent(new DivWriter(writer), nodeDocRef, table);
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

  protected AttributeBuilder newAttributeBuilder() {
    return new DefaultAttributeBuilder();
  }

}
