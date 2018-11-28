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
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.struct.StructDataService;

public abstract class AbstractTablePresentationType implements IPresentationTypeRole<TableConfig> {

  protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  public static final String CSS_CLASS = "struct_table";

  @Requirement
  protected StructDataService structDataService;

  @Requirement
  protected IModelAccessFacade modelAccess;

  @Requirement
  protected ModelUtils modelUtils;

  @Requirement
  protected ModelContext context;

  @Override
  public void writeNodeContent(StringBuilder writer, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, TableConfig table) {
    writeNodeContent(new DivWriter(writer), docRef, table);
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

  protected AttributeBuilder newAttributeBuilder() {
    return new DefaultAttributeBuilder();
  }

}
