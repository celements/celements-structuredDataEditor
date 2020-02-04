(function(window, undefined) {
  "use strict";

  var nextCreateObjectNb = -1;

  var observeCreateObject = function() {
    $$('.struct_object_list .struct_object_creation a').each(function(link) {
      link.stopObserving('click', createObject);
      link.observe('click', createObject);
    });
  };

  var createObject = function(event) {
    event.stop();
    var element = event.element().up('.struct_object_list');
    var template = element.down('.struct_object_creation .cel_template');
    var newEntry = createEntryFrom(template);
    if (newEntry) {
      var objectList = element.down('ul');
      if (objectList) {
        objectList.appendChild(newEntry);
        observeDeleteObject();
        newEntry.fire('celements:contentChanged', { 'htmlElem' : newEntry });
        console.debug('createObject - new object: ', newEntry);
      } else {
        console.warn('createObject - missing ul in: ', element);
      }
    } else {
      console.warn('createObject - unable to create entry from template ', template);
    }
  };

  var createEntryFrom = function(template) {
    var entry = document.createElement("li");
    entry.addClassName('struct_object_created');
    entry.innerHTML = template.innerHTML;
    var setObjNbOnce = false;
    entry.select('input,select,textarea').each(function(objElem) {
      // name="Space.Class_-1_field"
      var regex = /^(.+_)(-[1-9][0-9]*)(_.*)?$/;
      var name = (objElem.getAttribute('name') || '');
      objElem.setAttribute("name", name.replace(regex, '$1' + nextCreateObjectNb + '$3'));
      setObjNbOnce = setObjNbOnce || (name !== objElem.getAttribute('name'));
    });
    if (setObjNbOnce) {
      entry.select('.cel_cell').each(function(cellElem) {
        // id="cell:wiki..Space.cell_-1"
        var regex = /^(.+_)(-[1-9][0-9]*)$/;
        cellElem.id = (cellElem.id || '').replace(regex, '$1' + nextCreateObjectNb);
      });
      nextCreateObjectNb--;
      return entry;
    }
  };


  var observeDeleteObject = function() {
    $$('.struct_object_list ul li .struct_object_delete a').each(function(link) {
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
    } else if (objNb >= 0) {
      entry.hide();
      console.debug('deleteObject - marked: ', entry);
    } else {
      entry.remove();
      console.debug('deleteObject - removed: ', entry);
    }
  };

  var extractAndMarkObjectNb = function(entry) {
    var objNb = NaN;
    var objElem = entry.down('input,select,textarea');
    if (objElem) {
      // name="Space.Class_1_field"
      var nameParts = (objElem.getAttribute('name') || '').split('_');
      objNb = parseInt(nameParts[1], 10);
      if (objNb >= 0) {
        // ^ in front of the object number is the delete marker
        nameParts[1] = "^" + nameParts[1];
        objElem.setAttribute("name", nameParts.join('_'));
      }
    }    
    return objNb;
  };

  $j(document).ready(observeCreateObject);
  $j(document).ready(observeDeleteObject);
  $j(document).ready(function() {
    $(document.body).observe('celements:contentChanged', observeCreateObject);
    $(document.body).observe('celements:contentChanged', observeDeleteObject);
    $(document.body).observe('tabedit:successfulSaved', function(event) {
      location.reload();
    });
  });
  

})(window);
