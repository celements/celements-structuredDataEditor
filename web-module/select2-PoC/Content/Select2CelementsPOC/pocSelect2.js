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

  var formatRepo = function(data) {
    console.log('formatRepo 1: ', data);
    if (data.loading) return data.text;

    var markup = "<div class='select2-result-repository clearfix'>" +
      "<div class='select2-result-repository__avatar'><img src='" + data.imageURL + "' /></div>" +
      "<div class='select2-result-repository__meta'>" +
        "<div class='select2-result-repository__title'>" + data.title + "</div>";

    if (data.description) {
      markup += "<div class='select2-result-repository__description'>" + data.description + "</div>";
    }

    markup += "<div class='select2-result-repository__statistics'>" +
      "<div class='select2-result-repository__forks'><i class='fa fa-flash'></i> " + data.locName + "</div>" +
      "<div class='select2-result-repository__stargazers'><i class='fa fa-star'></i> " + data.categoryName + "</div>" +
    "</div>" +
    "</div></div>";
    return markup;
  };

  var formatRepoSelection = function(data, container) {
    console.log('formatRepoSelection: ', data, container);
    return data.text || data.id;
  };

  var processData = function (response, params) {
    // parse the results into the format expected by Select2
    // since we are using custom formatting functions we do not need to
    // alter the remote JSON data, except to indicate that infinite
    // scrolling can be used
    params.page = params.page || 1;
    
    $A(response.results).each(function(elem){
      elem.id = elem.progonEventId || elem.performanceId;
      elem.text = elem.title;
    });

    return {
      results: response.results,
      pagination: {
        more: response.countAfter > 0
      }
    };
  };

  var initSelect2Test = function() {
    //TODO: lazily load i18n/de.js"
    var language = "de";
    if (window.celMessages && window.celMessages.celmeta && window.celMessages.celmeta.language) {
      language = window.celMessages.celmeta.language;
    }
    console.log('start initSelect2Test: ', language);
    $j(".celSelectAjax").select2({
      language: language,
      placeholder: "Bitte einen Veranstaltungsort wählen/suchen",
      ajax: {
        url: "http://programmzeitung.programmonline.ch/Content/Webseite?xpage=celements_ajax&ajax_mode=JSONsearch&showfields=description&fromdate=06.08.2017&startDate=06-08-2017",
        dataType: 'json',
        delay: 250,
        data: function (params) {
          return {
            searchterm: params.term, // search term
            page: params.page
          };
        },
        processResults: processData,
        cache: true
      },
      escapeMarkup: function (markup) {
        // default Utils.escapeMarkup is HTML-escaping the value. Because
        // we formated the value using HTML it must not be further escaped.
        return markup;
      },
      minimumInputLength: 3,
      templateResult: formatRepo,
      templateSelection: formatRepoSelection
    });
  };

  var checkInitSelect2Test = function() {
    if (!window.celMessages) {
      console.log('observe cel:messagesLoaded for initSelect2Test ', window.celMessages);
      $(document.body).stopObserving('cel:messagesLoaded',  initSelect2Test);
      $(document.body).observe('cel:messagesLoaded',  initSelect2Test);
    } else {
      console.log('direct call of initSelect2Test ', window.celMessages);
      initSelect2Test();
    }
  };

  celAddOnBeforeLoadListener(checkInitSelect2Test);

})(window);