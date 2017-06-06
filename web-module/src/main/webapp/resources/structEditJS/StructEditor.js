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

  /****************************************
   * CelementsButtonHandler class definition *
   ****************************************/
  window.CELEMENTS.structEdit.CelementsButtonHandler = Class.create({
    initButtons : function(editorManager) {
      var _me = this;
      _me.initCloseButton();
    },

    initCloseButton : function() {
      var _me = this;
      //TODO connect buttons with saving functions!
//      var closeClickHandler = function() {
//        _me.checkUnsavedChanges(function(transport, jsonResponses, failed) {
//          if (!failed) {
//            window.onbeforeunload = null;
//            window.location.href = _me._getCancelURL();
//          } else {
//            console.error('closeClickHandler: checkUnsavedChanges failed! ', failed);
//          }
//        });
//      };
//      var buttonLabel = _me.tabMenuConfig.closeButtonLabel || 'Close';
//      _me.addActionButton(buttonLabel, closeClickHandler);
    }

  });

  /****************************************
   * StructEditorManager class definition *
   ****************************************/
  window.CELEMENTS.structEdit.StructEditorManager = Class.create({
        _allStructEditorMap : undefined,
        _initStructEditorHandlerBind : undefined,
        _checkBeforeUnloadBind : undefined,
        _loading : undefined,
        _buttonHandler : undefined,

        initialize : function(buttonHandler) {
          var _me = this;
          _me._initStructEditorHandlerBind = _me._initStructEditorHandler.bind(_me);
          _me._checkBeforeUnloadBind = _me._checkBeforeUnload.bind(_me);
          window.onbeforeunload = _me._checkBeforeUnloadBind;
          _me._buttonHandler = buttonHandler || new CELEMENTS.structEdit.CelementsButtonHandler();
          _me.registerListener();
          _me._loading = new CELEMENTS.LoadingIndicator();
        },

        registerListener : function() {
          var _me = this;
          _me._allStructEditorMap = new Hash();
          $(document.body).stopObserving("structEdit:initStructEditor",
              _me._initStructEditorHandlerBind);
          $(document.body).observe("structEdit:initStructEditor", _me._initStructEditorHandlerBind);
        },

        initButtons : function() {
          var _me = this;
          _me._buttonHandler.initButtons(_me);
        },

        getDirtyEditors : function() {
          var _me = this;
          var dirtyEditors = new Hash();
          _me._allStructEditorMap.each(function(entry) {
            if (entry.value.isDirty()) {
              dirtyEditors.set(entry.key, entry.value);
            }
          });
          return dirtyEditors;
        },

        hasDirtyEditors : function() {
          var _me = this;
          return (_me.getDirtyEditors().size() > 0);
        },

        saveAllFormsAsync : function(execCallback) {
          var _me = this;
          var dirtyEditors = _me.getDirtyEditors();
          var jsonResponses = new Hash();
          var saveAllForms = function(remainingDirtyEditors) {
            var editorKey = remainingDirtyEditors.keys[0];
            var editor = remainingDirtyEditors.get(editorKey);
            remainingDirtyEditors.unset(editorKey);
            var remainingDirtyEditorsMap = remainingDirtyEditors;
            editor.saveAndContinue(function(additionalResponses) {
              jsonResponses.update(additionalResponses);
              if (remainingDirtyEditorsMap.size() > 0) {
                console.log('next saveAllForms with: ', remainingDirtyEditorsMap.inspect());
                saveAllForms(remainingDirtyEditorsMap);
                } else {
                  console.log('save done.');
                  execCallback(transport, jsonResponses);
                }
            });
          };
          if (dirtyEditors.size() > 0) {
            saveAllForms(dirtyEditors);
          } else {
            execCallback();
          }
        },

        checkUnsavedChanges : function(execCallback, execCancelCallback) {
          var _me = this;
          execCallback = execCallback || function() {};
          execCancelCallback = execCancelCallback || function() {};
          if (_me.hasDirtyEditors()) {
          var saveBeforeCloseQuestion = _me._getModalDialog();
            saveBeforeCloseQuestion.setHeader(window.celMessages.structEditor.savingDialogWarningHeader);
            saveBeforeCloseQuestion.setBody(window.celMessages.structEditor.savingDialogMessage);
            saveBeforeCloseQuestion.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_WARN);
            saveBeforeCloseQuestion.cfg.queueProperty("buttons",
                [ { text: window.celMessages.structEditor.savingDialogButtonDoNotSave, handler:function() {
                    window.onbeforeunload = null;
                    this.hide();
                    execCallback();
                    }},
                      { text: window.celMessages.structEditor.savingDialogButtonCancel, handler:function() {
                       this.cancel();
                       execCancelCallback();
                     } },
                      { text: window.celMessages.structEditor.savingDialogButtonSave, handler:function() {
                        var _dialog = this;
                        _me.saveAllFormsAsync(function(transport, jsonResponses) {
                          _dialog.hide();
                          var failed = _me.showErrorMessages(jsonResponses);
                          if ((typeof console != 'undefined')
                              && (typeof console.log != 'undefined')) {
                            console.log('saveAllFormsAsync returning: ', failed, jsonResponses,
                                execCallback);
                          }
                    execCallback(transport, jsonResponses, failed);
                  });
                        _dialog.setHeader(window.celMessages.structEditor.savingDialogHeader);
                        _dialog.cfg.queueProperty("buttons", null);
                        _dialog.setBody(_me._loading.getLoadingIndicator(true));
                        _dialog.render();
                      }, isDefault:true }
                    ]);
            saveBeforeCloseQuestion.render();
            saveBeforeCloseQuestion.show();
          } else {
            execCallback();
          }
        },

        _checkBeforeUnload : function() {
          var _me = this;
          if (_me.hasDirtyEditors()) {
            if (window.celMessages && window.celMessages.structEditor
                && window.celMessages.structEditor.unsavedChangesOnCloseMessage
                && (window.celMessages.structEditor.unsavedChangesOnCloseMessage != '')) {
              return window.celMessages.structEditor.unsavedChangesOnCloseMessage;
            }
            return "WARNING: You have currently unsafed changes. Those changes will be lost if you click OK.";
          }
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
              if (!_me._allStructEditorMap.get(structRootId)) {
                _me.celFire('structEdit:beforeInitEditor', { 'structRootId' : structRootId });
                var newEditor = new CELEMENTS.structEdit.StructEditor(structRootElem);
                _me._allStructEditorMap.set(structRootId, newEditor);
                _me.celFire('structEdit:afterInitEditor', {
                  'structRootId' : structRootId,
                  'editor' : newEditor
                });
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
        },

        /**
         * showErrorMessages display errors in jsonResponses to the user
         *
         * @param jsonResponses
         * @returns true if errors have been displayed
         *          false if no errors have been displayed
         */
        showErrorMessages : function(jsonResponses) {
          var _me = this;
          var errorMessages = new Array();
          jsonResponses.each(function(response) {
//            var formId = response.key;
            var formSaveResponse = response.value;
            if (!formSaveResponse.successful) {
              errorMessages.push(formSaveResponse.errorMessages);
              errorMessages = errorMessages.flatten();
            }
          });
          if (errorMessages.length > 0) {
            var errorMesgDialog = _me._getModalDialog();
            errorMesgDialog.setHeader('Saving failed!');
            errorMesgDialog.setBody("saving failed for the following reasons:<ul><li>"
                + errorMessages.join('</li><li>').replace(new RegExp('<li>$'),'') + "</ul>");
            errorMesgDialog.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_WARN);
            errorMesgDialog.cfg.queueProperty("buttons",
              [ { text: "OK", handler:function() {
                     this.cancel();
                   } }
              ]);
            errorMesgDialog.render();
            errorMesgDialog.show();
            return true;
          }
          return false;
        },

        _getModalDialog : function() {
          if(!this.modalDialog) {
            this.modalDialog = new YAHOO.widget.SimpleDialog("modal dialog", {
                width: "auto",
                fixedcenter: true,
                visible: false,
                draggable: false,
                close: false,
                zindex:4,
                modal:true,
                monitorresize:false,
                icon: YAHOO.widget.SimpleDialog.ICON_HELP,
                constraintoviewport: true
                } );
          }
          //add skin-div to get default yui-skin-sam layouting for the dialog
          var yuiSamSkinDiv = new Element('div', {'class' : 'yui-skin-sam'});
          $(document.body).insert(yuiSamSkinDiv);
          this.modalDialog.render(yuiSamSkinDiv);
          return this.modalDialog;
        }

  });
  CELEMENTS.structEdit.StructEditorManager.prototype = Object.extend(
      CELEMENTS.structEdit.StructEditorManager.prototype, CELEMENTS.mixins.Observable);

  /*********************************
   * StructEditor class definition *
   *********************************/
  CELEMENTS.structEdit.StructEditor = Class.create({
        _rootElem : undefined,
        _formDiffsMap : undefined,

        initialize : function(editorRootElem) {
          var _me = this;
          _me._rootElem = editorRootElem;
          console.log('TODO: init StructEditor for ', _me._rootElem);
          _me._resetFormDiffs();
          console.log('finish init StructEditor for ', _me._rootElem);
        },

        _resetFormDiffs : function() {
          var _me = this;
          _me._formDiffsMap = new Hash();
          _me._rootElem.select('form').each(function(formelem) {
            var formDiff = new CELEMENTS.structEdit.FormDiffBuilder(formelem);
            if (formDiff.isValidFormId()) {
              _me._formDiffsMap.set(formelem.id, formDiff);
            }
          });
        },

        getDirtyForms : function() {
          var _me = this;
          var dirtyFormIds = new Array();
          _me._formDiffsMap.each(function(formEntry) {
            if (formEntry.value.isDirty()) {
              dirtyFormIds.push(formEntry.key);
            }
          });
          return dirtyFormIds;
        },

        isDirty : function() {
          var _me = this;
          var memoObj = {
            'structRootId' : _me._rootElem.id,
            'editor' : _me,
            'isDirty' : false
          };
          _me.celFire('structEdit:isDirty', memoObj);
          var isDirty = memoObj.isDirty;
          console.log('isDirty after listeners for ', _me._rootElem, isDirty);
          if (!isDirty) {
            isDirty = (_me.getDirtyForms().size() > 0);
          }
          return isDirty;
        },

        saveAndContinue : function(responseCb) {
          var jsonResponses = new Hash();
          console.log('TODO do for all dirty forms');
          responseCb(jsonResponses.toObject());
        }

  });
  CELEMENTS.structEdit.StructEditor.prototype = Object.extend(
      CELEMENTS.structEdit.StructEditor.prototype, CELEMENTS.mixins.Observable);

  /************************************
   * FormDiffBuilder class definition *
   ************************************/
  CELEMENTS.structEdit.FormDiffBuilder = Class.create({
    _formElem : undefined,
    _initialValues : undefined,

    initialize : function(formElem) {
      var _me = this;
      _me._formElem = formElem;
      _me._initialValues = new Hash();
      _me._retrieveInitialValues();
    },

    isValidFormId : function() {
      var _me = this;
      var formId = _me._formElem.id;
      return (typeof formId == 'string') && (formId != '') && _me._formElem
        && (typeof _me._formElem.action != 'undefined') && (_me._formElem.action != '');
    },

    _updateOneTinyMCETextArea : function(ed) {
      var formfield = $(ed.id);
      try {
        if (typeof ed.serializer !== 'undefined') {
          formfield.value = ed.getContent();
          console.log('_updateOneTinyMCETextArea: for field ', formfield.id, formfield.value);
        } else {
          console.warn('_updateOneTinyMCETextArea: no serializer -> skip ' + ed.id);
        }
      } catch (exp) {
        console.error('_updateOneTinyMCETextArea: failed with exception ' + formfield.id,
            ed.serializer, exp);
      }
    },

    _updateTinyMCETextAreas : function() {
      var _me = this;
      var formId = _me._formElem.id;
      var mceFields = document.forms[formId].select('textarea.mceEditor');
      console.log('_updateTinyMCETextAreas: for ', formId, mceFields);
      mceFields.each(function(formfield) {
        if ((typeof tinyMCE !== 'undefined') && tinyMCE.get(formfield.id)) {
          _me._updateOneTinyMCETextArea(tinyMCE.get(formfield.id));
        }
      });
      console.log('_updateTinyMCETextAreas: end ', formId);
    },

    /**
     * submittable fields must have a name attribute and maynot be disabled
     * @param fieldElem
     * @returns {Boolean}
     */
    _isSubmittableField : function(fieldElem) {
      return (fieldElem.name && (fieldElem.name != '') && !fieldElem.disabled);
    },

    _retrieveInitialValues : function() {
      var _me = this;
      var formId = _me._formElem.id;
      console.log('retrieveInitialValues: ', formId);
      if (_me.isValidFormId()) {
        var elementsValues = new Hash();
        _me._updateTinyMCETextAreas();
        _me._formElem.getElements().each(function(elem) {
          console.log('retrieveInitialValues: check field ', formId, elem);
          if (_me._isSubmittableField(elem) && (!elementsValues.get(elem.name)
              || (elementsValues.get(elem.name) == ''))) {
            console.log('initValue for: ', elem.name, elem.value);
            var isInputElem = (elem.tagName.toLowerCase() == 'input');
            var elemValue = elem.value;
            if (isInputElem && (elem.type.toLowerCase() == 'radio')) {
              elemValue = elem.getValue() || elementsValues.get(elem.name) || null;
            } else if (isInputElem && (elem.type.toLowerCase() == 'checkbox')) {
              elemValue = elem.checked;
            }
            elementsValues.set(elem.name, elemValue);
          }
        });
        console.log('retrieveInitialValues: before add elementsValues ', formId);
        _me._initialValues = elementsValues;
      }
      console.log('retrieveInitialValues: end');
    },

    _formDirtyOnLoad : function() {
      var _me = this;
      return  (typeof(_me._formElem.celFormDirtyOnLoad) !== 'undefined')
          && (_me._formElem.celFormDirtyOnLoad.value == 'true');
    },

    /**
     * _isDirtyField and needs saving
     *
     * @param fieldElem
     * @return
     */
    _isDirtyField : function(fieldElem) {
      var _me = this;
      if (fieldElem.hasClassName('celDirtyOnLoad')) {
        return true;
      }
      var formId = fieldElem.up('form').id;
      if (fieldElem.hasClassName('mceEditor') && tinyMCE && tinyMCE.get(fieldElem.id)) {
        //FIXME sometimes isDirty from tinyMCE is wrong... thus we compare the .getContent
        //FIXME with the _initialValues instead.
//        return tinyMCE.get(fieldElem.id).isDirty();
        return (_me._initialValues.get(fieldElem.name) != tinyMCE.get(fieldElem.id).getContent());
      } else if (!fieldElem.hasClassName('celIgnoreDirty')) {
        var isInputElem = (fieldElem.tagName.toLowerCase() == 'input');
        var elemValue = fieldElem.value;
        if (isInputElem && (fieldElem.type.toLowerCase() == 'radio')) {
          if (fieldElem.checked) {
            elemValue = fieldElem.getValue();
          } else {
            return false;
          }
        } else if (isInputElem && (fieldElem.type.toLowerCase() == 'checkbox')) {
          elemValue = fieldElem.checked;
        }
        return (_me._initialValues.get(fieldElem.name) != elemValue);
      }
      return false;
    },

    isDirty : function() {
      var _me = this;
      var isDirty = false;
      if (_me.isValidFormId()) {
        if (_me._formDirtyOnLoad()) {
          console.log('isDirty formDirtyOnLoad found. ');
          isDirty = true;
        } else {
          _me._updateTinyMCETextAreas();
          _me._formElem.getElements().each(function(elem) {
            if (_me._isSubmittableField(elem) && _me._isDirtyField(elem)) {
              console.log('isDirty first found dirty field: ', elem.name);
              isDirty = true;
              throw $break;  //prototype each -> break
            }
          });
        }
      } else {
        console.warn('isDirty: form with id [' + _me.formElem.id
            + '] disappeared since loading the editor.');
      }
      return isDirty;
    }

  });

})(window);