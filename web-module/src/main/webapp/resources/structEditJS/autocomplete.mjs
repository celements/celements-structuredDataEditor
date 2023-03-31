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

if(typeof window.CELEMENTS.structEdit==="undefined"){window.CELEMENTS.structEdit={}}
if(typeof window.CELEMENTS.structEdit.autocomplete==="undefined"){window.CELEMENTS.structEdit.autocomplete={};}

// templates for rendering autocomplete results may be defined within this object
if(typeof window.CELEMENTS.structEdit.autocomplete.templates==="undefined"){window.CELEMENTS.structEdit.autocomplete.templates={};}

class AutocompleteTemplates {
  #templates;

  constructor() {
    this.#templates = {};
    this.register('default', data => data.html || `<div class="result">${data.name}</div>`);
  }
  
  exists(type) {
    return typeof this.#templates[type] !== "undefined";
  }

  register(name, templateFunc) {
    if (typeof(templateFunc) === 'function') {
      this.#templates[name] = templateFunc;
    } else {
      console.error('cannot register template', name, templateFunc);
    }
  }  
  
  getTemplateSupplier(type) {
    return data => {
      if (data.loading) return data.text;
      const templateBuilder = this.#templates[type] || this.#templates.default;
      return templateBuilder(data);
    };
  }

}

class CelAutocompleteInitialiser {
  static #renderTemplates = new AutocompleteTemplates();
  
  constructor() {
    (document.readyState === 'loading')
        ? document.addEventListener('DOMContentLoaded', () => this.#checkInitAutocomplete())
        : this.#checkInitAutocomplete();
  }

  registerTemplate(name, templateFunc) {
    CelAutocompleteInitialiser.#renderTemplates.register(name, templateFunc);
  }  

  #checkInitAutocomplete() {
    $(document.body).observe('structEdit:initAutocomplete',  () => this.#initAutocomplete());
    $(document.body).observe("celements:contentChanged", () => this.#initAutocomplete());
    $(document.body).observe("cel_yuiOverlay:contentChanged", () => this.#initAutocomplete());
    if (!celMessages.isLoaded) {
      console.debug('observe cel:messagesLoaded for initAutocomplete');
      $(document.body).observe('cel:messagesLoaded', () => this.#initAutocomplete());
    } else {
      this.#initAutocomplete();
    }
  }

  #initAutocomplete() {
    //TODO: lazily load i18n/de.js"
    const language = ((window.celMessages && window.celMessages.celmeta)
        ? window.celMessages.celmeta.language : '')
        || 'de';
    document.querySelectorAll('.structAutocomplete:not(.initialised)').forEach(selectElem => {
      try {
        selectElem.classList.add('initialised');
        $j(selectElem).select2(this.#buildSelect2Config(selectElem, language))
        $j(selectElem).on('select2:unselect', (ev) => this.clearSelectOptions(ev));
        console.debug('initAutocomplete: done', selectElem, language)
      } catch (exc) {
        console.error('initAutocomplete: failed', selectElem, exc);
      }
    });
    this.#registerDeprecatedTemplates();
  }

  #registerDeprecatedTemplates() {
    const templates = window.CELEMENTS.structEdit.autocomplete.templates;
    for(const type in templates) {
      if (!CelAutocompleteInitialiser.#renderTemplates.exists(type)) {
        console.warn('Deprecated registering of autocomplete template.'
          + ' Instead use celAutocompleteInstance.registerTemplate', type);
        this.registerTemplate(type, templates[type]);
      }
    }
  }

  #buildSelect2Config(selectElem, language) {
    const cellRef = selectElem.dataset.cellRef || '';
    const classField = selectElem.dataset.classField || ''; 
    const type = selectElem.dataset.autocompleteType || '';
    const cssClasses = selectElem.dataset.autocompleteCss || '';
    return {
      language: language,
      placeholder: window.celMessages.structEditor.autocomplete['placeholder_' + classField]
                || window.celMessages.structEditor.autocomplete['placeholder_' + type]
                || window.celMessages.structEditor.autocomplete['placeholder_default'],
      allowClear: true,
      selectionCssClass: "structSelectContainer " + type + "SelectContainer " + cssClasses,
      dropdownCssClass: "structSelectDropDown " + type + "SelectDropDown " + cssClasses,
      ajax: this.buildSelect2Request(type, cellRef, selectElem),
      escapeMarkup: function (markup) {
        // default Utils.escapeMarkup is HTML-escaping the value. Because
        // we formated the value using HTML it must not be further escaped.
        return markup;
      },
      minimumInputLength: 3,
      templateResult: CelAutocompleteInitialiser.#renderTemplates.getTemplateSupplier(type),
      templateSelection: (data) => data.text || data.id
    };
  }

  clearSelectOptions(event) {
    const selectElem = event.target;
    console.debug('clearSelectOptions', selectElem);
    selectElem.innerHTML = '<option selected="selected" value="">delete</option>';
  }
  
  #handleAddNewElementMessage(event, selectElem) {
    console.debug('urlToNewElementEditorButton popup returned', event.data);
    event.source?.close();
    selectElem.replaceChildren(new Option(event.data.text, event.data.id, true, true));
    selectElem.dispatchEvent(new Event("change", {
      view: window,
      bubbles: true,
      cancelable: true,
    }));
  }

  #urlToNewElementEditorButtonClickHandler(event, selectElem) {
    const buttonElem = event.target.closest('.view_cel_buttonLink');
    if (buttonElem) {
      const urlToNewElementEditor = buttonElem.dataset.url;
      console.debug('urlToNewElementEditorButtonClickHandler start', event, selectElem,
        urlToNewElementEditor);
      const theAddNewPopup = window.open(urlToNewElementEditor, '_blank', 'popup=true');
      theAddNewPopup.addEventListener('message',
        (ev) => this.#handleAddNewElementMessage(ev, selectElem));
    }
  }

  #getUrlToNewElementEditorButton(selectElem, urlToNewElementEditor) {
    const classField = selectElem.dataset.classField || ''; 
    const type = selectElem.dataset.autocompleteType || '';
    const cellRef = selectElem.dataset.cellRef || '';
    const buttonText = window.celMessages.structEditor.autocomplete['UrlToNewElementEditorButtonText_' + cellRef]
                || window.celMessages.structEditor.autocomplete['UrlToNewElementEditorButtonText_' + classField]
                || window.celMessages.structEditor.autocomplete['UrlToNewElementEditorButtonText_' + type]
                || window.celMessages.structEditor.autocomplete['UrlToNewElementEditorButtonText_default']
                || 'nothing found? add new';
    const iconElem = document.createElement('span');
    iconElem.classList.add('halflings', 'halflings-plus-sign');
    const buttonInnerElem = document.createElement('div');
    buttonInnerElem.appendChild(iconElem);
    buttonInnerElem.classList.add('view_cel_buttonLink');
    buttonInnerElem.setAttribute('data-url', urlToNewElementEditor);
    buttonInnerElem.insertAdjacentText('beforeend', buttonText)
    buttonInnerElem.addEventListener('click', 
        (ev) => this.#urlToNewElementEditorButtonClickHandler(ev, selectElem));
    const buttonElem = document.createElement('div');
    buttonElem.appendChild(buttonInnerElem);
    buttonElem.classList.add('box', 'cel_button', 'struct_autocomplete_addnew');
    const itemElem = document.createElement('div');
    itemElem.appendChild(buttonElem);
    itemElem.classList.add('result', 'clearfix');
    return itemElem;
  }

  #createUrlToNewElementEditorButton(selectElem, response) {
    if (!response.hasMore && response.urlToNewElementEditor) {
      console.debug('createUrlToNewElementEditorButton: start', selectElem, response);
      return {
          'html' : this.#getUrlToNewElementEditorButton(selectElem, response.urlToNewElementEditor)
      };
    }
    return undefined;
  }

  /**
   * parse the results into the format expected by Select2
   * since we are using custom formatting functions we do not need to
   * alter the remote JSON data, except to indicate that infinite
   * scrolling can be used
   */
  processResultsFunc(selectElem, response, params) {
    params.page = params.page || 1;
    console.debug('processResultsFunc', selectElem, response, params);
    return {
      results: (response.results || [])
      .map(elem => {
          elem.id = elem.fullName;
          elem.text = elem.name;
          return elem;
      }).filter(elem => elem.html || elem.id && elem.text)
      .concat([this.#createUrlToNewElementEditorButton(selectElem, response)]
        .filter(Boolean)),
      pagination: {
        more: response.hasMore
      }
    };
  }

  buildSelect2Request(type, cellRef, selectElem, limit = 10) {
    return {
      url: "/OrgExport/REST",
      dataType: 'json',
      delay: 250,
      cache: true,
      timeout: 30000,
      processResults : (response, params) => this.processResultsFunc(selectElem, response, params),
      data: function(params) {
        const page = params.page || 1;
        const offset = (page - 1 ) * limit;
        return {
          'ajax' : 1,
          'xpage' : 'celements_ajax',
          'ajax_mode' : 'struct/autocomplete/search',
          'type' : type,
          'cellRef' : cellRef,
          'searchterm' : params.term,
          'page' : page,
          'limit' : limit,
          'offset' : offset
        }
      },
      error: function(e) {
        //TODO: PROGON-1088 - handle errors in requesting data (e.g. timeout) appropriately
        console.error("lookup exception:", e.statusText);
      }
    };
  }

}
const instance = new CelAutocompleteInitialiser();
export default instance;
window.CELEMENTS.structEdit.autocomplete.initaliserInstance = instance;

