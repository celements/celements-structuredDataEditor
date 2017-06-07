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
    _closeClickHandlerBind : undefined,
    _saveClickHandlerBind : undefined,
    _editorManager : undefined,

    initButtons : function(editorManager) {
      var _me = this;
      _me._closeClickHandlerBind = _me._closeClickHandler.bind(_me);
      _me._saveClickHandlerBind = _me._saveClickHandler.bind(_me);
      _me._editorManager = editorManager;
      _me.initCloseButton();
      _me.initSaveButton();
    },

    _registerButton : function(cssClassName, clickHandler) {
      var _me = this;
      var buttonElem = _me._editorManager.getRootElem().down('.' + cssClassName);
      if (buttonElem) {
        buttonElem.stopObserving('click', clickHandler);
        buttonElem.observe('click', clickHandler);
        buttonElem.setStyle({'pointer' : 'cursor'});
      } else {
        console.warn('registerButton: no "' + cssClassName + '" found!');
      }
    },

    initCloseButton : function() {
      var _me = this;
      _me._registerButton('structEditClose', _me._closeClickHandlerBind);
    },

    initSaveButton : function() {
      var _me = this;
      _me._registerButton('structEditSave', _me._saveClickHandlerBind);
    },

    _closeClickHandler : function(event) {
      var _me = this;
      event.stop();
      _me._editorManager.checkUnsavedChanges(function(jsonResponses, failed) {
        console.log('closeClickHandler checkUnsavedChanges callback ', jsonResponses, failed);
        if (!failed) {
          window.onbeforeunload = null;
          window.location.href = _me._editorManager.getCancelURL();
        } else {
          console.error('closeClickHandler: checkUnsavedChanges failed! ', failed);
        }
      });
    },

    _saveClickHandler : function(event) {
      var _me = this;
      event.stop();
      _me._editorManager.saveAndContinue(function(jsonResponses, failed) {
        console.log('saveClickHandler saveAndContinue callback ', jsonResponses, failed);
        if (!failed) {
          //remove template in url query after creating document in inline mode
          try {
            if (window.location.search.match(/\&?template=[^\&]+/)) {
              window.onbeforeunload = null;
              window.location.search = _me._deleteParamsFromURL();
            }
          } catch (err) {
            console.error('_saveClickHandler: error in saveAndContinue callback ', err);
          }
          _me._editorManager.celFire('structEdit:saveAndContinueButtonSuccessful', {
            'jsonResponses' :jsonResponses
          });
        } else {
          _me._editorManager.celFire('structEdit:saveAndContinueButtonFailed', {
            'jsonResponses' :jsonResponses
          });
        }
      });
    },

    _deleteParamsFromURL : function() {
      var newUrlParams = [];
      var standardWhiteList = ["language", "xredirect", "xcontinue"];
      var additionalWhiteList = [];
      $j("input[name=white_list_url]").each(function( index, inputElem ) {
        additionalWhiteList.add(inputElem.value);
      });
      standardWhiteList = standardWhiteList.concat(additionalWhiteList);
      for (var index = 0; index < standardWhiteList.length; index++) {
        var regEx = new RegExp("^.*(" + standardWhiteList[index] + "=[^&]*).*$", "g");
        var regExArray = regEx.exec(window.location.search);
        if (regExArray != null) {
          newUrlParams = newUrlParams.concat(regExArray.slice(1));
        }
      }
      return newUrlParams.join('&');
    }

  });

  /****************************************
   * StructEditorManager class definition *
   ****************************************/
  window.CELEMENTS.structEdit.StructEditorManager = Class.create({
    _rootElem : undefined,
    _allStructEditorMap : undefined,
    _initStructEditorHandlerBind : undefined,
    _checkBeforeUnloadBind : undefined,
    _loading : undefined,
    _buttonHandler : undefined,
    _modalDialog : undefined,

    initialize : function(buttonHandler) {
      var _me = this;
      _me._initStructEditorHandlerBind = _me._initStructEditorHandler.bind(_me);
      _me._checkBeforeUnloadBind = _me._checkBeforeUnload.bind(_me);
      window.onbeforeunload = _me._checkBeforeUnloadBind;
      _me._buttonHandler = buttonHandler || new CELEMENTS.structEdit.CelementsButtonHandler();
      _me.registerListener();
      _me._loading = new CELEMENTS.LoadingIndicator();
    },

    setRootElem : function(rootElem) {
      var _me = this;
      _me._rootElem = $(rootElem);
    },

    getRootElem : function() {
      var _me = this;
      var rootElem = _me._rootElem || document.body;
      return rootElem;
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

    getCancelURL : function() {
      var _me = this;
      var redirectValue = '';
      if ($$('input.celEditorRedirect').size() > 0) {
        redirectValue = $F($$('input.celEditorRedirect')[0]);
      } else {
        var matchStr = window.location.search.match(/[?&]xredirect=([^&]*)/);
        if (matchStr) {
          redirectValue = matchStr[1];
        }
      }
      var redirectBaseValue = window.location.pathname.replace(/\/edit\/|\/inline\//,
          '/cancel/');
      redirectValue = redirectBaseValue + '?xredirect=' + redirectValue;
      console.log('_getCancelURL: return redirectValue ', redirectValue);
      return redirectValue;
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

    saveAllEditorsAsync : function(execCallback) {
      var _me = this;
      var dirtyEditors = _me.getDirtyEditors();
      var jsonResponses = new Hash();
      var saveAllEditors = function(remainingDirtyEditors) {
        var editorKey = remainingDirtyEditors.keys[0];
        var editor = remainingDirtyEditors.get(editorKey);
        remainingDirtyEditors.unset(editorKey);
        var remainingDirtyEditorsMap = remainingDirtyEditors;
        editor.saveAllDirtyForms(function(additionalResponses) {
          console.log('saveAllEditorsAsync: saveAllDirtyForms callback ', jsonResponses,
              additionalResponses);
          jsonResponses.update(additionalResponses);
          if (remainingDirtyEditorsMap.size() > 0) {
            console.log('next saveAllEditors with: ', remainingDirtyEditorsMap.inspect());
            saveAllEditors(remainingDirtyEditorsMap);
          } else {
            console.log('save done.', jsonResponses);
            execCallback(jsonResponses);
          }
        });
      };
      if (dirtyEditors.size() > 0) {
        saveAllEditors(dirtyEditors);
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
          [ { text: window.celMessages.structEditor.savingDialogButtonDoNotSave,
              handler : function() {
                console.log('doNotSave button pressed!');
                window.onbeforeunload = null;
                this.hide();
                execCallback();
              }
            },
            { text: window.celMessages.structEditor.savingDialogButtonCancel,
              handler : function() {
                console.log('cancel button pressed!');
                this.cancel();
                execCancelCallback();
              }
            },
            { text: window.celMessages.structEditor.savingDialogButtonSave,
              handler : function() {
                console.log('save button pressed!');
                var _dialog = this;
                _me.saveAllEditorsAsync(function(jsonResponses) {
                  _dialog.hide();
                  var failed = _me.showErrorMessages(jsonResponses);
                  console.log('saveAllEditorsAsync returning: ', failed, jsonResponses, execCallback);
                  execCallback(jsonResponses, failed);
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

    saveAndContinue : function(execCallback) {
      var _me = this;
      execCallback = execCallback || function() {};
      //TODO add possibility to add JS-listener which can do additional 'isDirty' checks
      if (_me.hasDirtyEditors()) {
        var savingDialog = _me._getModalDialog();
        savingDialog.setHeader(window.celMessages.structEditor.savingDialogHeader);
        savingDialog.setBody(_me._loading.getLoadingIndicator(true));
        savingDialog.cfg.queueProperty("buttons", null);
        savingDialog.render();
        savingDialog.show();
        //TODO add possibility to add JS-listener which can execute alternative save actions
        _me.saveAllEditorsAsync(function(jsonResponses) {
          savingDialog.hide();
          var failed = _me.showErrorMessages(jsonResponses);
          if (failed) {
            _me.celFire('structEdit:failingSaved', jsonResponses);
          } else {
            _me.celFire('structEdit:successfulSaved', jsonResponses);
          }
          execCallback(jsonResponses, failed);
        });
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

    initStructEditors : function() {
      var _me = this;
      _me.getRootElem().select('.structDataEditor').each(function(structRootElem) {
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
      var _me = this;
      if(!_me._modalDialog) {
        _me._modalDialog = new YAHOO.widget.SimpleDialog("modal dialog", {
            width: "auto",
            fixedcenter: true,
            visible: false,
            draggable: false,
            close: false,
            zindex: 101,
            modal: true,
            monitorresize:false,
            icon: YAHOO.widget.SimpleDialog.ICON_HELP,
            constraintoviewport: true
            } );
      }
      //add skin-div to get default yui-skin-sam layouting for the dialog
      var yuiSamSkinDiv = new Element('div', {'class' : 'yui-skin-sam structEditorModalDialog'});
      $(document.body).insert(yuiSamSkinDiv);
      _me._modalDialog.render(yuiSamSkinDiv);
      return _me._modalDialog;
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
        _resetOneFormDiffBind : undefined,

        initialize : function(editorRootElem) {
          var _me = this;
          _me._rootElem = editorRootElem;
          _me._resetOneFormDiffBind = _me._resetOneFormDiff.bind(_me);
          console.log('start init StructEditor for ', _me._rootElem);
          _me._resetFormDiffs();
          console.log('finish init StructEditor for ', _me._rootElem);
        },

        _resetFormDiffs : function() {
          var _me = this;
          _me._formDiffsMap = new Hash();
          _me._rootElem.select('form').each(_me._resetOneFormDiffBind);
        },

        _resetOneFormDiff : function(theForm) {
          var _me = this;
          _me._formDiffsMap = _me._formDiffsMap || new Hash();
          var formDiff = new CELEMENTS.structEdit.FormDiffBuilder(theForm);
          if (formDiff.isValidFormId()) {
            _me._formDiffsMap.set(formDiff.getFormId(), formDiff);
          }
        },

        getDirtyFormIds : function() {
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
            isDirty = (_me.getDirtyFormIds().size() > 0);
          }
          return isDirty;
        },

        _handleSaveAjaxResponse : function(formId, transport, jsonResponses) {
          if (transport.responseText.isJSON()) {
            console.log('_handleSaveAjaxResponse with json result: ', transport.responseText);
            var jsonResult = transport.responseText.evalJSON();
            jsonResponses.set(formId, jsonResult);
            if (jsonResult.successful) {
              return true;
            } else {
              console.warn('_handleSaveAjaxResponse: save failed for [' + formId + ']: ',
                  jsonResult);
            }
          } else {
            return true;
          }
          return false;
        },

        saveAllDirtyForms : function(execCallback, doNotSaveFormId) {
          var _me = this;
          doNotSaveFormId = doNotSaveFormId || [];
          var dirtyFormIds = _me.getDirtyFormIds();
          var jsonResponses = new Hash();
          var saveAllForms = function(allDirtyFormIds) {
            var formId = allDirtyFormIds.pop();
            var remainingDirtyFormIds = allDirtyFormIds;
            _me.saveAndContinueAjax(formId, { onSuccess : function(transport) {
              if (_me._handleSaveAjaxResponse(formId, transport, jsonResponses)) {
//                _me._isEditorDirtyOnLoad = false;
                _me._resetOneFormDiff(formId);
              }
              if (remainingDirtyFormIds.size() > 0) {
                console.log('next saveAllForms with: ', remainingDirtyFormIds);
                saveAllForms(remainingDirtyFormIds);
                } else {
                  console.log('save done.');
                  execCallback(jsonResponses.toObject());
                }
            }});
          };
          dirtyFormIds = dirtyFormIds.without(doNotSaveFormId);
          if (dirtyFormIds.size() > 0) {
            saveAllForms(dirtyFormIds);
          } else {
            execCallback(jsonResponses.toObject());
          }
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

    initialize : function(theForm) {
      var _me = this;
      _me._formElem = $(theForm);
      _me._initialValues = new Hash();
      _me._retrieveInitialValues();
    },

    getFormId : function() {
      var _me = this;
      return _me._formElem.id;
    },

    isValidFormId : function() {
      var _me = this;
      var formId = _me.getFormId();
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
      var formId = _me.getFormId();
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
      var formId = _me.getFormId();
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