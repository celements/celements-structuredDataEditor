import structManager from './StructEditor.mjs?version=20230806';

const FORM_ELEM_TAGS = ['input', 'select', 'textarea', 'cel-input-date', 'cel-input-time', 'cel-input-date-time'];
const REGEX_OBJ_NB = /^(.+_)(-1)(_.*)?$/; // name="Space.Class_-1_field"
const START_CREATE_OBJ_NB = -2; // skip -1 in case it's already used statically in an editor

export class StructEntryHandler {

  #nextCreateObjectNb = START_CREATE_OBJ_NB;
  #rootElem;
  #cssClassCreated;

  constructor(rootElem, cssClassCreated) {
    this.#rootElem = rootElem;
    this.#cssClassCreated = cssClassCreated;
  }

  hasCreatedEntry() {
    return this.#nextCreateObjectNb < START_CREATE_OBJ_NB;
  }

  create(template, data, preInserter = (entry) => undefined) {
    const entry = this.#createEntry(template);
    if (preInserter(entry) !== false) { // strictly false to skip insertion
      this.#rootElem.appendChild(entry);
      requestAnimationFrame(() => entry.style.opacity = '1');
      if (data) {
        entry.dispatchEvent(new CustomEvent('celData:update', { detail: data }));
      }
      entry.fire('celements:contentChanged', { 'htmlElem' : entry });
      console.debug('create - new row in', this.#rootElem, ':', entry);
    } else {
      console.debug('create - skipped row in', this.#rootElem, ':', entry);
    }
    return entry;
  }

  #createEntry(template) {
    const fragment = template?.content.cloneNode(true);
    let entry = fragment?.firstElementChild;
    if (fragment?.childElementCount != 1 || entry?.tagName !== 'LI') {
      entry = document.createElement('li');
      entry.appendChild(fragment);
    }
    entry.classList.add(this.#cssClassCreated);
    entry.style.opacity = '0';
    entry.style.transition = 'opacity .5s ease-out';
    const objectNb = this.#nextCreateObjectNb;
    if (this.#setObjectNbIn(entry, FORM_ELEM_TAGS.join(','), 'name', objectNb)) {
      this.#setObjectNbIn(entry, '.cel_cell', 'id', objectNb);
      this.#setObjectNbIn(entry, 'label', 'for', objectNb);
      this.#nextCreateObjectNb--;
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
      entry.addEventListener('transitionend', entry.classList.contains(this.#cssClassCreated)
          ? () => entry.remove()
          : () => entry.style.display = 'none');
      console.debug('delete - removed:', entry);
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

  observeClick(link, action) {
    link?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      action(event);
    });
  }

  observeSave() {
    structManager.isStartFinished()
      ? structManager.celObserve('structEdit:saveAndContinueButtonSuccessful', event => this.#markReload(event))
      : structManager.celObserve('structEdit:finishedLoading', event => this.observeSave());
  }

  #markReload(event) {
    const detail = (event.detail || event.memo);
    detail.reload = detail.reload || this.hasCreatedEntry();
  }
}

export class CelTable extends HTMLElement {

  #handler;

  constructor() {
    super();
    this.#handler = new StructEntryHandler(this.#dataList, 'struct_table_created');
  }

  get template() {
    return this.querySelector('template.cel_template');
  }

  get #dataList() {
    return this.querySelector('ul.struct_table_data');
  }

  connectedCallback() {
    console.debug('connectedCallback', this);
    this.#observeCreate();
    this.#observeDelete();
    this.#handler.observeSave();
  }

  #observeCreate() {
    this.querySelectorAll('a.struct_table_create')
      .forEach(link => this.#handler.observeClick(link,
        event => this.createEntry()));
    if (this.getAttribute('type') === 'OBJLINK') {
      this.#observeCreateForLinkType();
    }
  }

  #observeCreateForLinkType() {
    const select = this.querySelector('.struct_table_header select.structAutocomplete');
    select.addEventListener('structEdit:autocomplete:selected', event => {
      const data = event.detail;
      // TODO only call if fullName doesn't exist in the table already
      this.createEntry(data, entry => {
        entry.dataset.ref = data.fullName;
        // unable to inject cel-data value into input field value, thus manually inject it
        const linkInput = entry.querySelector('input.struct_table_link_ref');
        if (linkInput) {
          linkInput.value = data.fullName;
        } else {
          console.warn('link input missing for new entry', entry);
        }
      });
      select.value = '';
      select.dispatchEvent(new Event('change', { bubbles: true }));
    });
  }

  #observeDelete(entry) {
    (entry || this).querySelectorAll('a.struct_table_delete')
      .forEach(link => this.#handler.observeClick(link,
        event => this.delete(event.target.closest('li'))));
  }

  createEntry(data, preInserter) {
    const entry = this.#handler.create(this.template, data, preInserter);
    this.#observeDelete(entry);
    return entry;
  }

  delete(entry) {
    return this.#handler.delete(entry);
  }
}

if (!customElements.get('cel-table')) {
  customElements.define('cel-table', CelTable);
}
