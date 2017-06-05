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

/*
*
*
**/
(function(window, undefined) {
  "use strict";

  if(typeof window.CELEMENTS.structEdit=="undefined"){window.CELEMENTS.structEdit={};};

  /**
   * StructEditorManager constructor
   */
  window.CELEMENTS.structEdit.StructEditorManager = Class.create({
        _initStructEditorHandlerBind : undefined,
        _allStructEditors : undefined,

        initialize : function() {
          var _me = this;
          _me._initStructEditorHandlerBind = _me._initStructEditorHandler.bind(_me);
          _me.registerListener();
        },

        registerListener : function() {
          var _me = this;
          _me._allStructEditors = new Hash();
          $(document.body).stopObserving("structEdit:initStructEditor",
              _me._initStructEditorHandlerBind);
          $(document.body).observe("structEdit:initStructEditor", _me._initStructEditorHandlerBind);
        },

        _createIdIfEmpty : function(structRoot) {
          var _me = this;
          var theId = structRoot.id;
          if (!theId || (theId === '')) {
            var count = 0;
            do {
              count++;
              theId = 'CELSTRUCTEDITOR:' + count;
            } while ($(theId));
            structRoot.id = theId;
          }
        },

        initStructEditors : function(checkRoot) {
          var _me = this;
          var rootElem = checkRoot || document.body;
          rootElem.select('.structDataEditor').each(function(structRootElem) {
            if (!structRootElem.hasClassName('celStructEditorLoading')
                && !structRootElem.hasClassName('celStructEditorLoaded')) {
              structRootElem.addClassName('celStructEditorLoading');
              console.log('initStructEditorHandler: start editor for ', structRootElem);
              var structRootId = _me._createIdIfEmpty(structRootElem);
              if (!_me._allStructEditors.get(structRootId)) {
                _me._allStructEditors.set(structRootId,
                    new CELEMENTS.structEdit.StructEditor(structRootElem));
              } else {
                console.warn('initStructEditorHandler: skip double init on ', structRootId);
              }
            }
          });
        },

        _initStructEditorHandler : function(event) {
          var _me = this;
          var checkRoot = event.memo.checkRoot || document.body;
          console.log('initStructEditorHandler: run for ', checkRoot);
          _me.initStructEditors(checkRoot);
          console.log('initStructEditorHandler: finish for ', checkRoot);
        }

  });
  CELEMENTS.structEdit.StructEditorManager.prototype = Object.extend(
      CELEMENTS.structEdit.StructEditorManager.prototype, CELEMENTS.mixins.Observable);

  CELEMENTS.structEdit.StructEditor = Class.create({
        _rootElem : undefined,

        initialize : function(editorRootElem) {
          var _me = this;
          _me._rootElem = editorRootElem;
          console.log('TODO: init StructEditor for ', _me._rootElem);
        },

        isDirty : function() {
          var _me = this;
          var isDirty = false;
          //TODO IMPLEMENT!!!
//          var isDirty = (_me.getDirtyFormIds().size() > 0) || _me._isEditorDirtyOnLoad;
//          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
//            console.log('isDirty: ', isDirty, ' , isEditorDirtyOnLoad: ',
//                _me._isEditorDirtyOnLoad);
//          }
          return isDirty;
        }

  });
  CELEMENTS.structEdit.StructEditor.prototype = Object.extend(
      CELEMENTS.structEdit.StructEditor.prototype, CELEMENTS.mixins.Observable);

})(window);