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

  //TODO [CELDEV-937] Struct autocomplete.js refactoring to Class 

  if(typeof window.CELEMENTS.structEdit=="undefined"){window.CELEMENTS.structEdit={}}
  if(typeof window.CELEMENTS.structEdit.autocomplete=="undefined"){window.CELEMENTS.structEdit.autocomplete={};}
  if(typeof window.CELEMENTS.structEdit.autocomplete.templates=="undefined"){window.CELEMENTS.structEdit.autocomplete.templates={};}

  const checkInitAutocomplete = function() {
    if (!celMessages.progMsg) {
      console.debug('observe cel:messagesLoaded for initAutocomplete ');
      $(document.body).stopObserving('cel:messagesLoaded',  initAutocomplete);
      $(document.body).observe('cel:messagesLoaded',  initAutocomplete);
    } else {
      initAutocomplete();
    }
  };

  const initAutocomplete = function() {
    //TODO: lazily load i18n/de.js"
    const language = ((window.celMessages && window.celMessages.celmeta)
        ? window.celMessages.celmeta.language : '')
        || 'de';
    document.querySelectorAll('.structAutocomplete').forEach(selectElem => {
      console.log('initAutocomplete:', selectElem, language)
      const type = selectElem.dataset.autocompleteType || '';
      const cellRef = selectElem.dataset.cellRef || '';
      $j(selectElem).select2(buildSelect2Config(type, language, cellRef))
      $j(selectElem).on('select2:unselect', clearSelectOptions);
    });
  };

  /**
   * parse the results into the format expected by Select2
   * since we are using custom formatting functions we do not need to
   * alter the remote JSON data, except to indicate that infinite
   * scrolling can be used
   */
  const processResults = function (response, params) {
    params.page = params.page || 1;
    return {
      results: response.results.map(elem => {
          elem.id = elem.fullName;
          elem.text = elem.name;
          return elem;
        }).filter(elem => elem.id && elem.text),
      pagination: {
        more: response.hasMore
      }
    };
  };

  const templateSelection = function(data) {
    return data.text || data.id;
  };

  const getTemplateSupplier = function(type) {
    return function(data) {
      if (data.loading) return data.text;
      const templateBuilder = window.CELEMENTS.structEdit.autocomplete.templates[type];
      return templateBuilder ? templateBuilder(data) : templateSelection(data);
    };
  };

  const clearSelectOptions = function(event) {
    const selectElem = event.target;
    console.debug('clearSelectOptions', selectElem);
    selectElem.innerHTML = '<option selected="selected" value="">delete</option>';
  };

  const buildSelect2Config = function(type, language, cellRef) {
    return {
      language: language,
      placeholder: celMessages.progMsg.select2["cel_select2_autocomplete_placeholder_" + type],
      allowClear: true,
      selectionCssClass: "structSelectContainer " + type + "SelectContainer",
      dropdownCssClass: "structSelectDropDown " + type + "SelectDropDown",
      ajax: buildSelect2Request(cellRef),
      escapeMarkup: function (markup) {
        // default Utils.escapeMarkup is HTML-escaping the value. Because
        // we formated the value using HTML it must not be further escaped.
        return markup;
      },
      minimumInputLength: 3,
      templateResult: getTemplateSupplier(type),
      templateSelection: templateSelection
    };
  };

  const buildSelect2Request = function(cellRef, limit = 10) {
    return {
      url: "/OrgExport/REST",
      dataType: 'json',
      delay: 250,
      cache: true,
      timeout: 30000,
      processResults : processResults,
      data: function(params) {
        const page = params.page || 1;
        const offset = (page - 1 ) * limit;
        return {
          'ajax' : 1,
          'xpage' : 'celements_ajax',
          'ajax_mode' : 'struct/autocomplete/search',
          'cellRef' : cellRef,
          'searchterm' : params.term,
          'page' : page,
          'limit' : limit,
          'offset' : offset
        }
      },
      error: function(e) {
        //TODO: PROGON-1088 - handle errors in requesting data (e.g. timeout) appropriately
        console.log("lookup exception:", e.statusText);
      }
    };
  };

  celAddOnBeforeLoadListener(checkInitAutocomplete);

})(window);
