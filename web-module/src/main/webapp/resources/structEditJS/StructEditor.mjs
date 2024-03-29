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

if(typeof window.CELEMENTS.structEdit=="undefined"){window.CELEMENTS.structEdit={};};
if(typeof window.CELEMENTS.commons=="undefined"){window.CELEMENTS.commons={};};

/*******************************
 * UrlFactory class definition *
 *******************************/
window.CELEMENTS.commons.UrlFactory = Class.create({

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
    console.log('getCancelURL: return redirectValue ', redirectValue);
    return redirectValue;
  },

  deleteParamsFromURL : function() {
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

/*******************************************
 * CelementsButtonHandler class definition *
 *******************************************/
window.CELEMENTS.structEdit.CelementsButtonHandler = Class.create({
  _closeClickHandlerBind : undefined,
  _saveClickHandlerBind : undefined,
  _editorManager : undefined,
  _urlFactory : undefined,

  initButtons : function(editorManager) {
    var _me = this;
    _me._closeClickHandlerBind = _me._closeClickHandler.bind(_me);
    _me._saveClickHandlerBind = _me._saveClickHandler.bind(_me);
    _me._urlFactory = new CELEMENTS.commons.UrlFactory();
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
        window.location.href = _me._urlFactory.getCancelURL();
      } else {
        console.error('closeClickHandler: checkUnsavedChanges failed! ', failed);
      }
    });
  },

  _saveClickHandler : function(event) {
    event.stop();
    this._editorManager.saveAndContinue((jsonResponses, failed) => {
      console.log('saveClickHandler saveAndContinue callback ', jsonResponses, failed);
      if (!failed) {
        const saveEvent = this._editorManager.celFire('structEdit:saveAndContinueButtonSuccessful',
          { jsonResponses });
        if (window.location.search.match(/\&?template=[^\&]+/)) {
          console.log('saveClickHandler: reload without template in url query', saveEvent);
          window.onbeforeunload = null;
          window.location.search = this._urlFactory.deleteParamsFromURL();
        } else if ((saveEvent.detail || saveEvent.memo).reload) {
          console.log('saveClickHandler: reload flag set', saveEvent);
          window.onbeforeunload = null;
          window.location.reload()
        } else {
          console.log('saveClickHandler: no reload', saveEvent);
        }
      } else {
        this._editorManager.celFire('structEdit:saveAndContinueButtonFailed', { jsonResponses });
      }
    });
  }

});

/****************************************
 * StructEditorManager class definition *
 ****************************************/
window.CELEMENTS.structEdit.StructEditorManager = Class.create({
  _rootElem : undefined,
  _allStructEditorMap : undefined,
  _initStructEditorHandlerBind : undefined,
  _saveAllEditorsAsyncBind : undefined,
  _buttonHandler : undefined,
  _startFinished : undefined,

  initialize : function(buttonHandler) {
    this._initStructEditorHandlerBind = this._initStructEditorHandler.bind(this);
    this._saveAllEditorsAsyncBind = this.saveAllEditorsAsync.bind(this);
    window.onbeforeunload = event => this._checkBeforeUnload(event);
    this._buttonHandler = buttonHandler || new CELEMENTS.structEdit.CelementsButtonHandler();
    this._startFinished = false;
    document.body?.dispatchEvent(new CustomEvent('structEdit:finishedInitialize', {
      'bubbles' : false,
      'cancelable' : false,
      'detail' : {
        'structEditorManager' : this
      }
    }));
  },

  setRootElem : function(rootElem) {
    this._rootElem = $(rootElem);
  },

  getRootElem : function() {
    return this._rootElem || document.body;
  },

  registerListener : function() {
    var _me = this;
    _me._allStructEditorMap = new Hash();
    $(document.body).stopObserving("structEdit:initStructEditor",
        _me._initStructEditorHandlerBind);
    $(document.body).observe("structEdit:initStructEditor", _me._initStructEditorHandlerBind);
  },

  startEditorManager : function() {
    var _me = this;
    _me.registerListener();
    _me._initButtons();
    _me._initStructEditors();
    _me._startFinished = true;
    _me.celFire('structEdit:finishedLoading');
  },

  isStartFinished : function() {
    var _me = this;
    return _me._startFinished;
  },

  _initButtons : function() {
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
    //TODO CELDEV-500: add possibility to add JS-listener which can do additional 'isDirty' checks
    return (_me.getDirtyEditors().size() > 0);
  },

  saveAllEditorsAsync : function(execCallback) {
    var _me = this;
    var dirtyEditors = _me.getDirtyEditors();
    var jsonResponses = new Hash();
    var saveAllEditors = function(remainingDirtyEditors) {
      var editorKey = remainingDirtyEditors.keys()[0];
      var editor = remainingDirtyEditors.get(editorKey);
      remainingDirtyEditors.unset(editorKey);
      var remainingDirtyEditorsMap = remainingDirtyEditors;
      editor.saveAllDirtyFormsAsync(function(additionalResponses) {
        console.log('saveAllEditorsAsync: saveAllDirtyFormsAsync callback ', jsonResponses,
            additionalResponses);
        jsonResponses.update(additionalResponses);
        if (remainingDirtyEditorsMap.size() > 0) {
          console.log('next saveAllEditors with: ', remainingDirtyEditorsMap.inspect());
          saveAllEditors(remainingDirtyEditorsMap);
        } else {
          console.log('save done.', jsonResponses, execCallback);
          execCallback(jsonResponses);
          console.log('after execCallback.');
        }
      });
    };
    if (dirtyEditors.size() > 0) {
      saveAllEditors(dirtyEditors);
    } else {
      execCallback();
    }
  },

  _getUnsavedChangesHandler : function(execCallback) {
    var _me = this;
    var unsavedHandler = new CELEMENTS.structEdit.UnsavedChangesHandler(execCallback);
    unsavedHandler.setDoBeforeFunction(_me._saveAllEditorsAsyncBind);
    unsavedHandler.celObserve('structEdit:failingSaved', function(event) {
      _me.celFire('structEdit:failingSaved', event.memo)
    });
    unsavedHandler.celObserve('structEdit:successfulSaved', function(event) {
      _me.celFire('structEdit:successfulSaved', event.memo)
    });
    return unsavedHandler;
  },

  checkUnsavedChanges : function(execCallback, execCancelCallback) {
    var _me = this;
    if (_me.hasDirtyEditors()) {
      var unsavedHandler = _me._getUnsavedChangesHandler(execCallback);
      unsavedHandler.setCancelFunction(execCancelCallback);
      unsavedHandler.displayBeforeQuestionDialog();
    } else {
      execCallback();
    }
  },

  saveAndContinue : function(execCallback) {
    var _me = this;
    execCallback = execCallback || function() {};
    if (_me.hasDirtyEditors()) {
      _me._getUnsavedChangesHandler(execCallback).execShowProgressDialog();
    }
   },

   _checkBeforeUnload : function(event) {
    if (this.hasDirtyEditors()) {
      event.preventDefault();
      if (window.celMessages && window.celMessages.structEditor
          && window.celMessages.structEditor.unsavedChangesOnCloseMessage
          && (window.celMessages.structEditor.unsavedChangesOnCloseMessage != '')) {
        return window.celMessages.structEditor.unsavedChangesOnCloseMessage;
      }
      return "WARNING: You have currently unsafed changes. Those changes will be lost if you click OK.";
    }
  },

  getEditorForFormElem : function(formElem){
    var _me = this;
    var editor = null;
    if (formElem) {
      var editorRootElem = formElem.up('.structDataEditor.celStructEditorLoaded');
      console.log('getEditorForFormElem editorRootElem ', editorRootElem);
      editor = _me._allStructEditorMap.get(editorRootElem.id);
    }
    return editor;
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
    return theId;
  },

  _initStructEditors : function(theCheckRoot) {
    var _me = this;
    var checkRoot = theCheckRoot || _me.getRootElem();
    checkRoot.select('.structDataEditor').each(function(structRootElem) {
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
        structRootElem.removeClassName('celStructEditorLoading');
        structRootElem.addClassName('celStructEditorLoaded');
      }
    });
  },

  _initStructEditorHandler : function(event) {
    var _me = this;
    var checkRoot = event.memo.checkRoot || document.body;
    console.log('initStructEditorHandler: run for ', checkRoot);
    _me._initStructEditors(checkRoot);
    console.log('initStructEditorHandler: finish for ', checkRoot);
  }
});
CELEMENTS.structEdit.StructEditorManager.prototype = Object.extend(
    CELEMENTS.structEdit.StructEditorManager.prototype, CELEMENTS.mixins.Observable);

/*****************************************
 * UnsavedChangesHandler class definition *
 *****************************************/
window.CELEMENTS.structEdit.UnsavedChangesHandler = Class.create({
  _execCancelFn : undefined,
  _execFn : undefined,
  _beforeExecFn : undefined,
  _loading : undefined,
  _modalDialog : undefined,

  initialize : function(execFn) {
    var _me = this;
    _me._execCancelFn = function() {};
    _me._beforeExecFn = function(callbackFN) { callbackFN(); };
    _me._execFn = execFn || function() {};
    _me._loading = new CELEMENTS.LoadingIndicator();
  },

  setDoBeforeFunction : function(beforeExecFn) {
    var _me = this;
    _me._beforeExecFn = beforeExecFn || function(callbackFN) { callbackFN(); };
  },

  setCancelFunction : function(execCancelFn) {
    var _me = this;
    _me._execCancelFn = execCancelFn || function() {};
  },

  displayBeforeQuestionDialog : function() {
    var _me = this;
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
            _me._execFn();
          }
        },
        { text: window.celMessages.structEditor.savingDialogButtonCancel,
          handler : function() {
            console.log('cancel button pressed!');
            this.cancel();
            _me._execCancelFn();
          }
        },
        { text: window.celMessages.structEditor.savingDialogButtonSave,
          handler : function() {
            console.log('save button pressed!');
            this.hide();
            _me.execShowProgressDialog();
         }, isDefault:true }
    ]);
    saveBeforeCloseQuestion.render();
    saveBeforeCloseQuestion.show();
  },

  execShowProgressDialog : function() {
    var _me = this;
    var savingDialog = _me._getModalDialog();
    savingDialog.setHeader(window.celMessages.structEditor.savingDialogHeader);
    savingDialog.setBody(_me._loading.getLoadingIndicator(true));
    savingDialog.cfg.queueProperty("buttons", []);
    savingDialog.render();
    savingDialog.show();
    //TODO CELDEV-500: add possibility to add JS-listener which can execute alternative
    //TODO CELDEV-500: save actions
    console.log('execShowProgressDialog: before _beforeExecFn ', _me._beforeExecFn);
    _me._beforeExecFn(function(jsonResponses) {
      console.log('execShowProgressDialog: beginning callback');
      savingDialog.hide();
      console.log('execShowProgressDialog: after hide savingDialog');
      var failed = _me._showErrorMessages(jsonResponses);
      if (failed) {
        _me.celFire('structEdit:failingSaved', { 'jsonResponses' : jsonResponses });
      } else {
        _me.celFire('structEdit:successfulSaved', { 'jsonResponses' : jsonResponses });
      }
      _me._execFn(jsonResponses, failed);
    });
  },

  /**
   * showErrorMessages display errors in jsonResponses to the user
   *
   * @param jsonResponses must be a Hash object with key=formId and value=Array of
   *                      formSaveResponses, which is a object of errorMessages
   *                      and warningMessages
   * @returns true if errors have been displayed
   *          false if no errors have been displayed
   */
  _showErrorMessages : function(jsonResponses) {
    var _me = this;
    var errorMessages = new Array();
    jsonResponses.each(function(response) {
      var formSaveResponse = response.value;
      if (!formSaveResponse.successful) {
        errorMessages.push(formSaveResponse.errorMessages);
        errorMessages = errorMessages.flatten();
      }
    });
    if (errorMessages.length > 0) {
      var errorMesgDialog = _me._getModalDialog();
      errorMesgDialog.setHeader(window.celMessages.structEditor.savingFailedDialogHeader);
      errorMesgDialog.setBody(window.celMessages.structEditor.savingFailedDialogText + "<ul><li>"
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
CELEMENTS.structEdit.UnsavedChangesHandler.prototype = Object.extend(
    CELEMENTS.structEdit.UnsavedChangesHandler.prototype, CELEMENTS.mixins.Observable);

/*********************************
 * StructEditor class definition *
 *********************************/
window.CELEMENTS.structEdit.StructEditor = Class.create({
  _rootElem : undefined,
  _formDiffsMap : undefined,
  _resetOneFormDiffBind : undefined,
  _formSavedSuccessfulHandlerBind : undefined,

  initialize : function(editorRootElem) {
    var _me = this;
    _me._rootElem = editorRootElem;
    _me._resetOneFormDiffBind = _me._resetOneFormDiff.bind(_me);
    _me._formSavedSuccessfulHandlerBind = _me._formSavedSuccessfulHandler.bind(_me);
    console.log('start init StructEditor for ', _me._rootElem);
    _me._resetFormDiffs();
    console.log('finish init StructEditor for ', _me._rootElem);
  },

  _resetFormDiffs : function() {
    var _me = this;
    _me._formDiffsMap = new Hash();
    _me._rootElem.select('form.celementsCheckUnsaved').each(_me._resetOneFormDiffBind);
  },

  getFormIds : function() {
    var _me = this;
    return _me._formDiffsMap.keys().sort();
  },

  getDirtyFormIds : function() {
    var _me = this;
    return _me.getFormIds().filter(function(formId) {
      return _me._formDiffsMap.get(formId).isDirty();
    });
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

  _resetOneFormDiff : function(theForm) {
    var _me = this;
    _me._formDiffsMap = _me._formDiffsMap || new Hash();
    var formDiff = new CELEMENTS.structEdit.FormDiffBuilder(theForm);
    if (formDiff.isValidFormId()) {
      _me._formDiffsMap.set(formDiff.getFormId(), formDiff);
    }
  },

  _formSavedSuccessfulHandler : function(event) {
    var _me = this;
    _me._resetOneFormDiff(event.memo.savedFormId);
  },

  saveAllDirtyFormsAsync : function(execCallback, doNotSaveFormId) {
    var _me = this;
    doNotSaveFormId = doNotSaveFormId || [];
    var dirtyFormIds = _me.getDirtyFormIds();
    dirtyFormIds = dirtyFormIds.without(doNotSaveFormId);
    var saveHandler = new CELEMENTS.structEdit.CelementsFormSaver(execCallback);
    saveHandler.celObserve('structEdit:formSavedSuccessful', _me._formSavedSuccessfulHandlerBind);
    saveHandler.saveAllForms(dirtyFormIds);
  }

});
CELEMENTS.structEdit.StructEditor.prototype = Object.extend(
    CELEMENTS.structEdit.StructEditor.prototype, CELEMENTS.mixins.Observable);

/************************************
 * FormDiffBuilder class definition *
 ************************************/
window.CELEMENTS.structEdit.FormDiffBuilder = Class.create({
  _formElem : undefined,
  _initialValues : undefined,

  initialize : function(theForm) {
    var _me = this;
    _me._formElem = $(theForm);
    _me._initialValues = {};
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

  _updateOneTinyMCETextArea : function(formfield, cbFunc, cbFuncErr) {
    const ed = tinyMCE.get(formfield.id);
    if (!ed) {
      console.error('_updateOneTinyMCETextArea: no editor found! ', formfield.id, formfield);
      return;
    }
    try {
      if (typeof ed.serializer !== 'undefined') {
        formfield.value = ed.getContent();
        console.log('_updateOneTinyMCETextArea: for field ', formfield.id, formfield.value);
      } else {
        console.warn('_updateOneTinyMCETextArea: no serializer -> skip ' + ed.id);
      }
      if (cbFunc) {
        cbFunc(formfield.value);
      }
    } catch (exp) {
      if (cbFuncErr) {
        cbFuncErr(exp);
      } else {
        console.error('_updateOneTinyMCETextArea: failed with exception ' + formfield.id,
          ed.serializer, exp);
      }
    }
  },

  _startGetFieldValueAsync : function(formfield) {
    const _me = this;
    if ((typeof tinyMCE !== 'undefined') && formfield.classList.contains('mceEditor')) {
      if (tinyMCE.get(formfield.id)) {
        _me._updateOneTinyMCETextArea(formfield);
      } else {
        console.debug('_startGetFieldValueAsync: still loading, adding delayed read ',
          formfield.id);
        return new Promise((resolve, reject) => {
          console.debug('_startGetFieldValueAsync: start Promise ', formfield.id);
          const finishLoadingTinymMce = function(event) {
            const editor = event.memo.editor;
            if (editor.id === formfield.id) {
              _me._updateOneTinyMCETextArea(formfield, resolve, reject);
              console.log('finishLoadingTinymMce: finished loading, updated tinyMCE for ',
                editor.id);
            } else {
              console.debug('finishLoadingTinymMce: skip event for ', editor.id, ' waiting for ',
                formfield.id);
            }
          };
          $$('body')[0].observe('celRTE:finishedInit', finishLoadingTinymMce);
        });
      }
    } else {
      console.debug('_startGetFieldValueAsync: no tinyMCE or no mceEditor field ', formfield.id);
    }
    return null;
  },

  _getFieldValue : function(formfield, cblFunc) {
    const _me = this;
    console.debug('_startGetFieldValueAsync: _startGetFieldValueAsync for ', formfield.id);
    const possiblePromise = _me._startGetFieldValueAsync(formfield, cblFunc);
    if (possiblePromise) {
      possiblePromise
      .then(cblFunc)
      .catch(exp => {
        console.error('_getFieldValue: failed with exception ' + formfield.id, exp);
      });
    } else {
      cblFunc(formfield.value);
    }
  },

  _updateTinyMCETextAreas : function() {
    const _me = this;
    const formId = _me.getFormId();
    const mceFields = document.forms[formId].select('textarea.mceEditor');
    if (typeof tinyMCE !== 'undefined') {
      console.log('_updateTinyMCETextAreas: for ', formId, mceFields);
      mceFields.each(function(formfield) {
        _me._updateOneTinyMCETextArea(formfield);
      });
    } else {
      console.debug('_updateTinyMCETextAreas: skip no tinyMCE');
    }
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
    const formId = this.getFormId();
    if (this.isValidFormId()) {
      const formdata = new FormData(document.getElementById(formId));
      const newInitialValues = {};
      [...formdata.keys()].forEach(key => {
        newInitialValues[key] = new Set(formdata.getAll(key));
      });
      this._initialValues = Object.freeze(newInitialValues);
      console.log('retrieveInitialValues: ', formId, this._initialValues);
    }
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
    if (fieldElem.hasClassName('celDirtyOnLoad')) {
      return true;
    }
    const formElem = fieldElem.closest('form');
    const formdata = new FormData(formElem);
    const formElements = formElem.elements;
    const dirtyFields = [...formdata.keys()].filter(key => 
      !formElements[key].classList?.contains('celIgnoreDirty')
      && !this._equalsParamValues(formdata.getAll(key), this._initialValues[key]));
    console.debug('_isDirtyField: dirtyFields found', dirtyFields);
    return dirtyFields.length > 0;
  },

  _equalsParamValues : function(currentValueArr, initValueSet = new Set()) {
    return (new Set(currentValueArr).size === initValueSet.size)
      && currentValueArr.every(val => initValueSet.has(val));
  },

  isDirty : function() {
    var _me = this;
    var isDirty = false;
    if (_me.isValidFormId()) {
      if (_me._formDirtyOnLoad()) {
        console.log('isDirty formDirtyOnLoad found. ');
        isDirty = true;
      } else {
        console.debug('isDirty before _updateTinyMCETextAreas');
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

/************************************
 * CelementsFormSaver class definition *
 ************************************/
window.CELEMENTS.structEdit.CelementsFormSaver = Class.create({
  _jsonResponses : undefined,
  _saveCallback : undefined,

  initialize : function(saveCallback) {
    var _me = this;
    _me._jsonResponses = new Hash();
    _me._saveCallback = saveCallback;
  },

  _saveAndContinueAjax : function(formId, options) {
    const form = document.forms[formId];
    if (form) {
      form.querySelectorAll('textarea.mceEditor').forEach(textarea => {
        console.debug('textarea save tinymce: ', textarea);
        textarea.value = tinyMCE.get(textarea.id).save();
      });
      options.parameters = options.parameters || {};
      // submit enabled select fields without selections
      [...form.querySelectorAll('select')]
        .filter(select => select.name && !select.disabled && select.value === '')
        .forEach(select => options.parameters[select.name] = select.dataset.unselectedValue || '');
      // submit unchecked checkboxes and radio buttons
      [...form.querySelectorAll('input[type="checkbox"], input[type="radio"]')]
        .filter(input => input.name && !input.disabled && !input.checked)
        .forEach(input => options.parameters[input.name] = input.dataset.uncheckedValue || '');
      form.request(options);
    } else {
      console.error('form not found: ', formId);
    }
   },

   _handleSaveAjaxJsonResponse : function(formId, jsonResponse) {
     var _me = this;
     _me._jsonResponses.set(formId, jsonResponse);
     if (jsonResponse.successful) {
       _me.celFire('structEdit:formSavedSuccessful', {
         'savedFormId' : formId
       });
     } else {
       console.warn('_handleSaveAjaxJsonResponse: save failed for [' + formId + ']: ',
           jsonResponse);
       _me.celFire('structEdit:formSavedFailed', {
         'savedFormId' : formId,
         'jsonResponse' : jsonResponse
       });
     }
   },

   _getSavingFailedJson : function(transport) {
     return {
         'successful' : false,
         'errorMessages' : window.celMessages.structEditor.savingFailed,
         'responseText' : transport.responseText,
         'httpStatus': transport.status
       };
   },

   /**
    * @param transport the Ajax transport-object
    * @return jsonResponses must be a Hash object with key=formId and value=Array of
    *                      formSaveResponses, which is a object of errorMessages
    *                      and warningMessages
    */
   _parseSaveAjaxResponse : function(transport) {
     var _me = this;
     var jsonResult;
     if (transport.responseText && transport.responseText.isJSON()) {
       jsonResult = transport.responseText.evalJSON();
     } else {
       console.error('_parseSaveAjaxResponse: no JSON ', transport);
       jsonResult = _me._getSavingFailedJson(transport);
     }
     return jsonResult;
   },

   saveAllForms : function(allDirtyFormIds) {
     var _me = this;
     if (allDirtyFormIds.size() > 0) {
       var formId = allDirtyFormIds.pop();
       console.debug('saving form:', formId);
       var remainingDirtyFormIds = allDirtyFormIds;
       _me._saveAndContinueAjax(formId, {
         onComplete : function(transport) {
           _me._handleSaveAjaxJsonResponse(formId, _me._parseSaveAjaxResponse(transport));
           console.log('next saveAllForms with: ', remainingDirtyFormIds);
           _me.saveAllForms(remainingDirtyFormIds);
         }
       });
     } else {
       console.log('save done.');
       _me._saveCallback(_me._jsonResponses.toObject());
     }
  }

});
CELEMENTS.structEdit.CelementsFormSaver.prototype = Object.extend(
    CELEMENTS.structEdit.CelementsFormSaver.prototype, CELEMENTS.mixins.Observable);
    
const initStructEditorContentChangedHandler = function(event) {
  const checkRoot = event.memo.htmlElem || document.body;
  console.log('initStructEditorContentChangedHandler: run for ', checkRoot);
  $(document.body).fire('structEdit:initStructEditor', { 'checkRoot' : checkRoot });
  console.log('initStructEditorContentChangedHandler: finish for ', checkRoot);
};

if (!window.celStructEditorManager) {
  const celStructEditorManager = new CELEMENTS.structEdit.StructEditorManager();
  celStructEditorManager.startEditorManager();
  window.celStructEditorManager = celStructEditorManager;
  $(document.body).observe("celements:contentChanged", initStructEditorContentChangedHandler);
}
export default window.celStructEditorManager;
