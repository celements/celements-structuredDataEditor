(function(window, undefined) {
  "use strict";

  const _FORM_ELEM_TAGS = ['input', 'select', 'textarea', 'cel-input-date', 'cel-input-time', 'cel-input-date-time'];
  const _REGEX_OBJ_NB = /^(.+_)(-1)(_.*)?$/; // name="Space.Class_-1_field"
  const _nextCreateObjectNbMap = {};
  
  const init_structObjectListEdit = function() {
    moveTemplatesOutOfForm();
    observeCreateObject();
    observeDeleteObject();
  };

  const moveTemplatesOutOfForm = function() {
    document.querySelectorAll('ul.struct_object li.struct_object_creation').forEach(element => {
      const template = element.querySelector('.cel_template');
      const form = element.closest('form');
      if (template && form) {
        const objectClassName = element.closest('ul.struct_object')
            .getAttribute('data-struct-class') || '';
        template.classList.add(objectClassName.replace('.', '_'));
        form.after(template);
        console.debug('moveTemplatesOutOfForm - moved ', template);
      }
    });
  };

  const observeCreateObject = function() {
    $$('ul.struct_object li.struct_object_creation a').each(function(link) {
      link.stopObserving('click', createObject);
      link.observe('click', createObject);
    });
  };

  const createObject = function(event) {
    event.stop();
    const objectList = event.target.closest('ul.struct_object');
    if (objectList) {
      const objectClassName = objectList.getAttribute('data-struct-class');
      const newEntry = createEntryFor(objectClassName);
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

  const createEntryFor = function(objectClassName) {
    const objCssClass = '.' + (objectClassName.replace('.', '_') || 'none');
    const template = document.querySelector('.cel_template' + objCssClass);
    if (template) {
      const entry = document.createElement("li");
      entry.classList.add('struct_object_created');
      entry.style.display = "none";
      entry.innerHTML = template.innerHTML;
      const objectNb = _nextCreateObjectNbMap[objectClassName] || -2;
      if (setObjectNbIn(entry, _FORM_ELEM_TAGS.join(','), 'name', objectNb)) {
        setObjectNbIn(entry, '.cel_cell', 'id', objectNb);
        setObjectNbIn(entry, 'label', 'for', objectNb);
        _nextCreateObjectNbMap[objectClassName] = objectNb - 1;
        return entry;
      }
    }
  };

  const setObjectNbIn = function(entry, selector, key, objectNb) {
    let changed = false;
    entry.querySelectorAll(selector).forEach(elem => {
      const oldValue = (elem.getAttribute(key) || '');
      const newValue = oldValue.replace(_REGEX_OBJ_NB, '$1' + objectNb + '$3');
      elem.setAttribute(key, newValue);
      changed = (changed || (oldValue !== newValue));
    });
    return changed;
  };


  const observeDeleteObject = function() {
    const selector = 'ul.struct_object li a.struct_object_delete';
    document.querySelectorAll(selector).forEach(link => {
      link.stopObserving('click', deleteObject);
      link.observe('click', deleteObject);
    });
  };

  const deleteObject = function(event) {
    event.preventDefault();
    event.stopPropagation();
    const entry = event.target.closest('li');
    const fields = extractFields(entry);
    if (fields.length === 0) {
      console.warn('deleteObject - unable to extract field data from', entry);
    } else if (confirm(window.celMessages.structEditor.objectRemoveConfirm)) {
      fields.forEach(markObjectAsDeleted);
      $j(entry).fadeOut(400, function() {
        if (entry.classList.contains('struct_object_created')) {
          entry.remove();
          _createObjectCount--;
        }
      });
      console.debug('deleteObject - removed: ', entry);
    }
  };

  const extractFields = function(entry) {
    return [...entry.querySelectorAll(_FORM_ELEM_TAGS.join(','))].map(formElem => {
      // name="Space.Class_1_field"
      const nameParts = (formElem?.getAttribute('name') || '').split('_');
      return {
        formElem: formElem,
        nameParts: nameParts,
        objNb: parseInt(nameParts[1], 10),
      };
    }).filter(f => !isNaN(f.objNb));
  };

  const markObjectAsDeleted = function(field) {
    if (field.objNb >= 0) {
      const nameParts = [... field.nameParts];
      // ^ in front of the object number is the delete marker
      nameParts[1] = "^" + nameParts[1];
      field.formElem.setAttribute("name", nameParts.join('_'));
    }
  };

  const reloadPage = () => {
    for (const objNb of Object.values(_nextCreateObjectNbMap)) {
      if (objNb < -2) {
        window.onbeforeunload = null;
        location.reload();
        break;
      }
    }
  };

  const reloadOnSaveHandler = () => {
    const structManager = window.celStructEditorManager;
    if (structManager) {
      if (structManager.isStartFinished()) {
        structManager.celObserve('structEdit:saveAndContinueButtonSuccessful', reloadPage);
      } else {
        structManager.celObserve('structEdit:finishedLoading', reloadOnSaveHandler);
      }
    } else {
      $(document.body).observe('tabedit:saveAndContinueButtonSuccessful', reloadPage);
    }
  };

  const onReady = callback => (document.readyState === 'loading')
      ? document.addEventListener('DOMContentLoaded', callback)
      : callback();

  onReady(init_structObjectListEdit);
  onReady(reloadOnSaveHandler);
  $(document.body).observe('celements:contentChanged', init_structObjectListEdit);

})(window);
