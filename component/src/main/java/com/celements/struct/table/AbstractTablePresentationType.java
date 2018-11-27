package com.celements.struct.table;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.struct.StructDataService;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

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
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, TableConfig table) {
    writeNodeContent(outStream, docRef, table);
  }

  @Override
  public void writeNodeContent(StringBuilder outStream, DocumentReference docRef,
      TableConfig table) {
    checkNotNull(outStream);
    checkNotNull(docRef);
    checkNotNull(table);
    LOGGER.debug("writeNodeContent - for [{}] with [{}]", docRef, table);
    List<String> cssClasses = Lists.newArrayList(getDefaultCssClass());
    cssClasses.addAll(getCssClasses(table));
    outStream.append("<div " + getCssClassHtml(cssClasses) + ">");
    writeDivContent(outStream, docRef, table);
    outStream.append("</div>");
  }

  protected abstract List<String> getCssClasses(TableConfig table);

  protected abstract void writeDivContent(StringBuilder outStream, DocumentReference docRef,
      TableConfig table);

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

  protected String getCssClassHtml(Iterable<String> cssClasses) {
    return "class=\"" + Joiner.on(',').join(cssClasses) + "\"" + ">";
  }

}
