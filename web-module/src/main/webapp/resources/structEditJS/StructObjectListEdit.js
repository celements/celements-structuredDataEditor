(function(window, undefined) {
  "use strict";

  const _REGEX_OBJ_NB = /^(.+_)(-1)(_.*)?$/; // name="Space.Class_-1_field"
  var _nextCreateObjectNbMap = {};
  
  var init_structObjectListEdit = function() {
    moveTemplatesOutOfForm();
    observeCreateObject();
    observeDeleteObject();
  };

  var moveTemplatesOutOfForm = function() {
    document.querySelectorAll('ul.struct_object li.struct_object_creation').forEach(element => {
      var template = element.querySelector('.cel_template');
      var form = element.closest('form');
      if (template && form) {
        var objectClassName = element.closest('ul.struct_object')
            .getAttribute('data-struct-class') || '';
        template.classList.add(objectClassName.replace('.', '_'));
        form.after(template);
        console.debug('moveTemplatesOutOfForm - moved ', template);
      }
    });
  };

  var observeCreateObject = function() {
    $$('ul.struct_object li.struct_object_creation a').each(function(link) {
      link.stopObserving('click', createObject);
      link.observe('click', createObject);
    });
  };

  var createObject = function(event) {
    event.stop();
    var objectList = event.element().up('ul.struct_object');
    if (objectList) {
      var objectClassName = objectList.getAttribute('data-struct-class');
      var newEntry = createEntryFor(objectClassName);
      if (newEntry) {
          objectList.appendChild(newEntry);
          $j(newEntry).fadeIn();
          observeDeleteObject();
          newEntry.fire('celements:contentChanged', { 'htmlElem' : newEntry });
          console.debug('createObject - new object for ', objectClassName, ': ', newEntry);
      } else {
        console.warn('createObject - illegal template for ', objectClassName);
      }
    } else {
      console.warn('createObject - missing list ', objectList);
    }
  };

  var createEntryFor = function(objectClassName) {
    var objCssClass = '.' + (objectClassName.replace('.', '_') || 'none');
    var template = document.querySelector('.cel_template' + objCssClass);
    if (template) {
      var entry = document.createElement("li");
      entry.addClassName('struct_object_created');
      entry.style.display = "none";
      entry.innerHTML = template.innerHTML;
      var objectNb = _nextCreateObjectNbMap[objectClassName] || -2;
      var anyObjNbSet = setObjectNbIn(entry, 'input,select,textarea', 'name', objectNb);
      if (anyObjNbSet) {
        setObjectNbIn(entry, '.cel_cell', 'id', objectNb);
        setObjectNbIn(entry, 'label', 'for', objectNb);
        _nextCreateObjectNbMap[objectClassName] = --objectNb;
        return entry;
      }
    }
  };

  var setObjectNbIn = function(entry, selector, key, objectNb) {
    var changed = false;
    entry.select(selector).each(function(elem) {
      var oldValue = (elem.getAttribute(key) || '');
      var newValue = oldValue.replace(_REGEX_OBJ_NB, '$1' + objectNb + '$3');
      elem.setAttribute(key, newValue);
      changed = (changed || (oldValue !== newValue));
    });
    return changed;
  };


  var observeDeleteObject = function() {
    $$('ul.struct_object li a.struct_object_delete').each(function(link) {
      link.stopObserving('click', deleteObject);
      link.observe('click', deleteObject);
    });
  };

  var deleteObject = function(event) {
    event.stop();
    var entry = event.element().up('li');
    var objNb = extractAndMarkObjectNb(entry);
    if (isNaN(objNb)) {
      console.warn('deleteObject - unable to extract objNb on: ', entry);
    } else if (confirm(window.celMessages.structEditor.objectRemoveConfirm)) {
      $j(entry).fadeOut(400, function() {
        if (objNb < 0) {
          entry.remove();
        }
      });
      console.debug('deleteObject - removed: ', entry);
    }
  };

  var extractAndMarkObjectNb = function(entry) {
    var objNb = NaN;
    var formElem = entry.down('input,select,textarea');
    if (formElem) {
      // name="Space.Class_1_field"
      var nameParts = (formElem.getAttribute('name') || '').split('_');
      objNb = parseInt(nameParts[1], 10);
      if (objNb >= 0) {
        // ^ in front of the object number is the delete marker
        nameParts[1] = "^" + nameParts[1];
        formElem.setAttribute("name", nameParts.join('_'));
      }
    }    
    return objNb;
  };

  $j(document).ready(init_structObjectListEdit);
  $j(document).ready(function() {
    $(document.body).observe('celements:contentChanged', init_structObjectListEdit);
    $(document.body).observe('tabedit:successfulSaved', function(event) {
      location.reload();
    });
  });
  

})(window);
