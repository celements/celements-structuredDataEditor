import structManager from './StructEditor.mjs?version=20230806';
import CelDataRenderer from '/file/resources/celDynJS/celData/cel-data-renderer.mjs?ver=20230806'

const FORM_ELEM_TAGS = ['input', 'select', 'textarea', 'cel-input-date', 'cel-input-time', 'cel-input-date-time'];
const REGEX_OBJ_NB = /^(.+_)(-1)(_.*)?$/; // name="Space.Class_-1_field"
const START_CREATE_OBJ_NB = -2; // skip -1 in case it's already used statically in an editor

export class StructEntryHandler {

  #nextCreateObjectNb = START_CREATE_OBJ_NB;
  #rootElem;
  #renderer;
  #cssClassCreated;

  constructor(rootElem, template, cssClassCreated) {
    this.#rootElem = rootElem;
    this.#renderer = new CelDataRenderer(rootElem, template)
      .withEntryRoot('li')
      .withAnimation({
        create: 'struct_table_row_create',
        remove: 'struct_table_row_remove'
      });
    this.#cssClassCreated = cssClassCreated;
  }

  hasCreatedEntry() {
    return this.#nextCreateObjectNb < START_CREATE_OBJ_NB;
  }

  async create(data, preInserter = (entry) => undefined) {
    if (typeof preInserter !== 'function') {
      throw new TypeError('preInserter must be a function');
    }
    const entry = await this.#renderer.append(data || {}, entry => {
      entry.classList.add(this.#cssClassCreated);
      this.#setObjectNb(entry);
      preInserter(entry);
    })[0]; // this renderer only creates one li entry 
    console.debug('create - new row in', this.#rootElem, ':', entry);
    return entry;
  }

  #setObjectNb(entry) {
    const objectNb = this.#nextCreateObjectNb;
    if (this.#setObjectNbIn(entry, FORM_ELEM_TAGS.join(','), 'name', objectNb)) {
      this.#setObjectNbIn(entry, '.cel_cell', 'id', objectNb);
      this.#setObjectNbIn(entry, 'label', 'for', objectNb);
      this.#nextCreateObjectNb--;
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
      const hide = !entry.classList.contains(this.#cssClassCreated);
      this.#renderer.removeEntry(entry, hide);
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
    this.#handler = new StructEntryHandler(this.#dataList, this.template, 'struct_table_created');
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

  async createEntry(data, preInserter) {
    const entry = await this.#handler.create(data, preInserter);
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
