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
  
//  var initTinyMCE = function(event) {
//    if (tinymce) {
//      tinymce.init({
//        selector: 'textarea.tinyMCE',
//        height: 500,
//        theme: 'modern',
//        plugins: 'print preview searchreplace autolink directionality visualblocks visualchars fullscreen image link media template codesample table charmap hr pagebreak nonbreaking anchor toc insertdatetime advlist lists textcolor wordcount imagetools contextmenu colorpicker textpattern help',
//        toolbar1: 'formatselect | bold italic strikethrough forecolor backcolor | link | alignleft aligncenter alignright alignjustify  | numlist bullist outdent indent  | removeformat',
//        image_advtab: true,
//        templates: [
//          { title: 'Test template 1', content: 'Test 1' },
//          { title: 'Test template 2', content: 'Test 2' }
//        ],
//        content_css: [
//          '//fonts.googleapis.com/css?family=Lato:300,300i,400,400i',
//          '//www.tinymce.com/css/codepen.min.css'
//        ]
//       });
//    }
//  };

  window.celStructEditorManager = new CELEMENTS.structEdit.StructEditorManager();

  celAddOnBeforeLoadListener(function() {
    $(document.body).stopObserving("celements:contentChanged", initStructEditorContentChangedHandler);
    $(document.body).observe("celements:contentChanged", initStructEditorContentChangedHandler);
//    if($$(".cel_cell .tinyMCEV4").size() > 0) {
//      $$(".cel_cell .tinyMCEV4").each(function(elem) {
//        elem.up(".cel_cell").stopObserving("celements:contentChanged", initTinyMCE);
//        elem.up(".cel_cell").observe("celements:contentChanged", initTinyMCE);
//      });
//      initTinyMCE();
//    }
    window.celStructEditorManager.startEditorManager();
  });

})(window);
