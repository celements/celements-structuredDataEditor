const FORM_ELEM_TAGS = ['input', 'select', 'textarea', 'cel-input-date', 'cel-input-time', 'cel-input-date-time'];
const REGEX_OBJ_NB = /^(.+_)(-1)(_.*)?$/; // name="Space.Class_-1_field"
const START_CREATE_OBJ_NB = -2; // skip -1 in case it's already used statically in an editor

class CelTable {

  #nextCreateObjectNb = START_CREATE_OBJ_NB;

  connectedCallback() {
    console.debug('connectedCallback', this.isConnected, this);
    this.#observeCreate();
    this.#observeDelete();
    this.#observeSaveAndContinue();
  }

  #observeCreate() {
    this.#observe('a.struct_table_create', () => this.create());
  }

  #observeDelete(entry) {
    this.#observe('a.struct_table_delete', e => this.delete(e), entry);
  }

  #observe(selector, action, entry) {
    (entry || this).querySelectorAll(selector)
      .forEach(link => link.addEventListener('click', event => {
        event.preventDefault();
        event.stopPropagation();
        action(entry || event.target.closest('li'));
      }));
  }

  get template() {
    return this.querySelector('template.cel_template');
  }
  
  create() {
    const newEntry = this.#newEntry();
    if (newEntry) {
      this.appendChild(newEntry);
      requestAnimationFrame(() => newEntry.style.opacity = '1');
      this.#observeDelete(newEntry);
      newEntry.fire('celements:contentChanged', { 'htmlElem' : newEntry });
      console.debug('create - new object for ', this, ': ', newEntry);
      return newEntry;
    }
  }

  #newEntry() {
    if (this.template) {
      const entry = document.createElement("li");
      entry.classList.add('struct_table_created');
      entry.style.opacity = '0';
      entry.style.transition = 'opacity .5s ease-out';
      entry.appendChild(this.template.content.cloneNode(true));
      const objectNb = this.#nextCreateObjectNb;
      if (this.#setObjectNbIn(entry, FORM_ELEM_TAGS.join(','), 'name', objectNb)) {
        this.#setObjectNbIn(entry, '.cel_cell', 'id', objectNb);
        this.#setObjectNbIn(entry, 'label', 'for', objectNb);
        this.#nextCreateObjectNb--;
        return entry;
      }
    } else {
      console.warn('create - illegal template for ', this);
    }
  }

  #setObjectNbIn(entry, selector, key, objectNb) {
    let changed = false;
    entry.querySelectorAll(selector).forEach(elem => {
      const oldValue = (elem.getAttribute(key) || '');
      const newValue = oldValue.replace(REGEX_OBJ_NB, '$1' + objectNb + '$3');
      elem.setAttribute(key, newValue);
      changed = (changed || (oldValue !== newValue));
    });
    return changed;
  }

  delete(entry) {
    const fields = entry ? this.#extractFields(entry) : [];
    if (fields.length === 0) {
      console.warn('delete - unable to extract field data from', entry);
    } else if (confirm(window.celMessages.structEditor.objectRemoveConfirm)) {
      fields.forEach(this.#markObjectAsDeleted);
      entry.style.transition = 'opacity .4s ease-in';
      requestAnimationFrame(() => entry.style.opacity = '0');
      entry.addEventListener('transitionend', entry.classList.contains('struct_table_created')
          ? () => entry.remove()
          : () => entry.style.display = 'none');
      console.debug('delete - removed: ', entry);
    }
  }

  #extractFields(entry) {
    return [...entry.querySelectorAll(FORM_ELEM_TAGS.join(','))].map(formElem => {
      // name="Space.Class_1_field"
      const nameParts = (formElem?.getAttribute('name') || '').split('_');
      return {
        formElem: formElem,
        nameParts: nameParts,
        objNb: parseInt(nameParts[1], 10),
      };
    }).filter(f => !isNaN(f.objNb));
  }

  #markObjectAsDeleted(field) {
    if (field.objNb >= 0) {
      const nameParts = [... field.nameParts];
      // ^ in front of the object number is the delete marker
      nameParts[1] = "^" + nameParts[1];
      field.formElem.setAttribute("name", nameParts.join('_'));
    }
  }

  #observeSaveAndContinue() {
    const structManager = window.celStructEditorManager;
    if (structManager) {
      structManager.isStartFinished()
        ? structManager.celObserve('structEdit:saveAndContinueButtonSuccessful', event => this.#markReload(event))
        : structManager.celObserve('structEdit:finishedLoading', event => this.#observeSaveAndContinue());
    }
  }

  #markReload(event) {
    const detail = (event.detail || event.memo);
    detail.reload = detail.reload || (this.#nextCreateObjectNb < START_CREATE_OBJ_NB);
  }
}

if (!customElements.get('cel-table')) {
  customElements.define('cel-table', CelTable);
}
