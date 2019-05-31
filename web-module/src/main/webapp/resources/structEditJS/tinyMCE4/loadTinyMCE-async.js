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

(function(window, undefined) {
  "use strict";
  
  var initCelRTE4 = function() {
    console.log('initCelRTE4: start');
    var params = {
        xpage : 'celements_ajax',
        ajax_mode : 'struct/Tiny4Config'
     };
    var hrefSearch = window.location.search;
    var templateRegEx = new RegExp('^(\\?|(.*&)+)?template=([^=&]*).*$');
    if (hrefSearch.match(templateRegEx)) {
      params['template'] = window.location.search.replace(templateRegEx, '$3');
    }
    console.log('initCelRTE4: before Ajax tinymce');
    new Ajax.Request(getCelHost(), {
      method: 'post',
      parameters: params,
      onSuccess: function(transport) {
        var tinyConfigJSON = transport.responseText.replace(/\n/g,' ');
        if (tinyConfigJSON.isJSON()) {
//          window.tinymce.dom.Event.domLoaded = true; -> still needed in tinyMCE4?
          var tinyConfigObj = tinyConfigJSON.evalJSON();
          tinyConfigObj["body_class"] = getAllEditorBodyClasses(tinyConfigObj).join(',');
          console.log('initCelRTE4: tinyMCE.init');
          tinyMCE.init(tinyConfigObj);
        } else {
          console.error('TinyConfig is no json!', tinyConfigJSON);
        }
      }
    });
  };
  
  function(structManager){
    console.log('loadTinyMCE async finishHeaderListener: start');
    if (structManager) {
      if (structManager.isStartFinished()) {
        console.log('structEditorManager not initialized: register for finishLoading');
        structManager.celStopObserving('structEdit:finishedLoading', initCelRTE4);
        structManager.observe('structEdit:finishedLoading', initCelRTE4);
      } else {
        console.log('structEditorManager already initialized: initCelRTE4');
        initCelRTE4();
      }
    } else {
      console.error('No struct editor manager found -> Failed to initialize tinyMCE4.');
    }
    console.log('loadTinyMCE async finishHeaderListener: end');
  })(window.celStructEditorManager);
  
})(window);