(function(window, undefined) {
  "use strict";

  
  var observeCreateObject = function() {
    $$('.structObjectList .structCreateObject a').each(function(link) {
      link.stopObserving('click', createObject);
      link.observe('click', createObject);
    });
  };

  var createObject = function(event) {
    event.stop();
    var element = event.element().up('.structObjectList');
    var objectList = element.down('ul');
    var newContent = element.down('.structCreateObject .content');
    if (objectList && newContent) {
      var newEntry = document.createElement("li");
      newEntry.innerHTML = newContent.innerHTML;
      objectList.appendChild(newEntry);
      observeDeleteObject();
      console.debug('createObject - new object: ', newEntry);
    } else {
      console.warn('createObject - unable to create object in list: ', element);
    }
  };


  var observeDeleteObject = function() {
    $$('.structObjectList ul li .structDeleteObject a').each(function(link) {
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
    var objElem = entry.down('select,textarea,input');
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

})(window);
