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
package com.celements.structEditor.fields;

import org.xwiki.component.annotation.Component;

/**
 * @deprecated since 5.10 instead use {@link TextAreaTagPageType}
 */
@Deprecated
@Component(TextAreaFieldPageType.INPUT_FIELD_PAGETYPE_NAME)
public class TextAreaFieldPageType extends AbstractStructFieldPageType {

  public static final String INPUT_FIELD_PAGETYPE_NAME = "TextAreaField";

  static final String VIEW_TEMPLATE_NAME = "TextAreaFieldView";

  @Override
  public String getName() {
    return INPUT_FIELD_PAGETYPE_NAME;
  }

  @Override
  protected String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

}
