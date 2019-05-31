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

  /**
   * Initialize struct editors and register to initialize on contentChanged events.
   */

  var initStructEditorContentChangedHandler = function(event) {
    var checkRoot = event.memo.htmlElem || document.body;
    console.log('initStructEditorContentChangedHandler: run for ', checkRoot);
    $(document.body).fire('structEdit:initStructEditor', { 'checkRoot' : checkRoot });
    console.log('initStructEditorContentChangedHandler: finish for ', checkRoot);
  };
  
  window.celStructEditorManager = new CELEMENTS.structEdit.StructEditorManager();

  celAddOnBeforeLoadListener(function() {
    $(document.body).stopObserving("celements:contentChanged", initStructEditorContentChangedHandler);
    $(document.body).observe("celements:contentChanged", initStructEditorContentChangedHandler);
    window.celStructEditorManager.startEditorManager();
  });

})(window);
