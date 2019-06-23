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
package com.celements.tinymce4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.rteConfig.RteConfigRole;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import jersey.repackaged.com.google.common.base.Joiner;

@Component(TinyMce4Config.HINT)
public class TinyMce4Config implements RteConfigRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(TinyMce4Config.class);

  /**
   * CAUTION: do not change the HINT it will be used from the vm-scripts
   */
  public static final String HINT = "tinymce4";

  @Requirement
  private RteConfigRole rteConfig;

  private static final String SEPARATOR = "|";
  private static final List<String> ALL_SEPARATOR_LIST = ImmutableList.of(SEPARATOR, "separator");
  private static final ImmutableList<String> TABLE_CONTROLS = ImmutableList.of("table", SEPARATOR,
      "tablerowprops", "tablecellprops", SEPARATOR, "tableinsertrowbefore", "tableinsertrowafter",
      "tabledeleterow", SEPARATOR, "tableinsertcolbefore", "tableinsertcolafter", "tabledeletecol",
      SEPARATOR, "tablesplitcells", "tablemergecells");
  private static final ImmutableList<String> CELIMAGE = ImmutableList.of("celimage");
  private static final ImmutableList<String> CELLINK = ImmutableList.of("cellink");
  private static final List<String> BUTTONS_BLACKLIST = ImmutableList.of("save", "cancel", "");
  private static final Map<String, List<String>> BUTTONS_CONVERSIONMAP = initButtonConversionMap();

  private static final ImmutableMap<String, List<String>> initButtonConversionMap() {
    return ImmutableMap.<String, List<String>>builder().put("image", CELIMAGE).put("advimage",
        CELIMAGE).put("separator", ImmutableList.of(SEPARATOR)).put("advlink", CELLINK).put("link",
            CELLINK).put("tablecontrols", TABLE_CONTROLS).put("justifyleft", ImmutableList.of(
                "alignleft")).put("justifycenter", ImmutableList.of("aligncenter")).put(
                    "justifyright", ImmutableList.of("alignright")).put("justifyfull",
                        ImmutableList.of("alignjustify")).build();
  }

  private static final Pattern ROW_LAYOUT_REGEX = Pattern.compile("row_\\d+");

  @Override
  public List<DocumentReference> getRTEConfigsList() {
    final List<DocumentReference> rteConfigsList = rteConfig.getRTEConfigsList();
    LOGGER.debug("getRTEConfigsList: returning '{}'", rteConfigsList);
    return rteConfigsList;
  }

  @Override
  public String getRTEConfigField(String name) {
    String rteConfigField = rteConfig.getRTEConfigField(name);
    if (ROW_LAYOUT_REGEX.matcher(name).matches()) {
      LOGGER.info("getRTEConfigField converting value '{}' for '{}'", rteConfigField, name);
      rteConfigField = rowLayoutConvert(rteConfigField);
    }
    LOGGER.debug("getRTEConfigField for '{}': returning '{}'", name, rteConfigField);
    return rteConfigField;
  }

  String rowLayoutConvert(String rteConfigField) {
    List<String> rteRowList = new ArrayList<>();
    final String[] rteRowArray = rteConfigField.split("[,;]");
    boolean isFirst = true;
    boolean pendingAddSeparator = false;
    for (String element : rteRowArray) {
      final String buttonName = element.trim();
      if (!BUTTONS_BLACKLIST.contains(buttonName)) {
        List<String> newButtonNameList = ImmutableList.of(buttonName);
        if (BUTTONS_CONVERSIONMAP.containsKey(buttonName)) {
          newButtonNameList = BUTTONS_CONVERSIONMAP.get(buttonName);
        }
        boolean isSeparator = ALL_SEPARATOR_LIST.contains(buttonName);
        if (!isFirst || !isSeparator) {
          isFirst = false;
          if (isSeparator) {
            pendingAddSeparator = true;
          } else {
            if (pendingAddSeparator) {
              rteRowList.add(SEPARATOR);
              pendingAddSeparator = false;
            }
            rteRowList.addAll(newButtonNameList);
          }
        }
      }
    }
    return Joiner.on(" ").join(rteRowList);
  }

}
