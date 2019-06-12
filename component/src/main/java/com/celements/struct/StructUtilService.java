package com.celements.struct;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
final public class StructUtilService implements StructUtilServiceRole {

  @Requirement
  private IModelAccessFacade modelAccess;

  @Override
  final public Optional<XWikiDocument> findParentCell(XWikiDocument cellDoc, String ptName)
      throws DocumentNotExistsException {
    while (cellDoc.getParentReference() != null) {
      cellDoc = modelAccess.getDocument(cellDoc.getParentReference());
      PageTypeReference ptRef = getPtResolver().resolvePageTypeReference(cellDoc).orNull();
      if ((ptRef != null) && ptRef.getConfigName().equals(ptName)) {
        return Optional.of(cellDoc);
      }
    }
    return Optional.empty();
  }

  /**
   * CAUTION: cyclic dependency with struct pageTypes like OptionTagPageType !!!!
   */
  private IPageTypeResolverRole getPtResolver() {
    return Utils.getComponent(IPageTypeResolverRole.class);
  }

}
