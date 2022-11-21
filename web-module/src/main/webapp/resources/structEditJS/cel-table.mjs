const FORM_ELEM_TAGS = ['input', 'select', 'textarea', 'cel-input-date', 'cel-input-time', 'cel-input-date-time'];
const REGEX_OBJ_NB = /^(.+_)(-1)(_.*)?$/; // name="Space.Class_-1_field"
const START_CREATE_OBJ_NB = -2; // skip -1 in case it's already used statically in an editor

class CelTable extends HTMLElement {

  #nextCreateObjectNb = START_CREATE_OBJ_NB;

  constructor() {
    super();
  }

  connectedCallback() {
    console.debug('connectedCallback', this.isConnected, this);
    this.#observeCreate();
    this.#observeDelete();
    this.#observeSaveAndContinue();
  }

  #observeCreate() {
    this.#observe('a.struct_table_create', () => this.createEntry());
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

  get #dataList() {
    return this.querySelector('ul.struct_table_data');
  }

  createEntry(data) {
    const entry = this.#newEntry();
    if (entry) {
      this.#dataList.appendChild(entry);
      requestAnimationFrame(() => entry.style.opacity = '1');
      this.#observeDelete(entry);
      if (data) {
        entry.dispatchEvent(new CustomEvent('celData:update', { detail: data }));
      }
      entry.fire('celements:contentChanged', { 'htmlElem' : entry });
      console.debug('createEntry - new row for ', this, ': ', entry);
      return entry;
    } else {
      console.warn('createEntry - illegal template in ', this);
    }
  }

  #newEntry() {
    const fragment = this.template?.content.cloneNode(true);
    const entry = fragment?.querySelector('li');
    if (entry) {
      entry.classList.add('struct_table_created');
      entry.style.opacity = '0';
      entry.style.transition = 'opacity .5s ease-out';
      const objectNb = this.#nextCreateObjectNb;
      if (this.#setObjectNbIn(entry, FORM_ELEM_TAGS.join(','), 'name', objectNb)) {
        this.#setObjectNbIn(entry, '.cel_cell', 'id', objectNb);
        this.#setObjectNbIn(entry, 'label', 'for', objectNb);
        this.#nextCreateObjectNb--;
      }
    }
    return entry;
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
