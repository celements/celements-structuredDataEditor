import { StructEntryHandler } from './cel-table.mjs?version=20230806'

class CelStructObjectList extends HTMLUListElement {

  #handler;

  constructor() {
    super();
    this.#handler = new StructEntryHandler(this, this.template,'struct_object_created');
  }

  get template() {
    return this.querySelector('template.cel_template');
  }

  connectedCallback() {
    console.debug('connectedCallback', this);
    this.#observeCreate();
    this.#observeDelete();
    this.#handler.observeSave();
  }

  #observeCreate() {
    this.querySelectorAll('a.struct_object_create')
      .forEach(link => this.#handler.observeClick(link,
        event => this.createEntry()));
  }

  #observeDelete(entry) {
    (entry || this).querySelectorAll('a.struct_object_delete')
      .forEach(link => this.#handler.observeClick(link,
        event => this.delete(event.target.closest('li'))));
  }

  async createEntry(data) {
    const entry = await this.#handler.create(data);
    this.#observeDelete(entry);
    return entry;
  }

  delete(entry) {
    return this.#handler.delete(entry);
  }
}

if (!customElements.get('cel-struct-object-list')) {
  customElements.define('cel-struct-object-list', CelStructObjectList, { extends: 'ul' });
}
