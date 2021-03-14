/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
