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
  'use strict';

  const versionTimeStamp = new Date().getTime();
  const curScriptElement = document.currentScript;
  const curScriptPath = curScriptElement.src.split('?')[0];
  const curScriptDir = curScriptPath.split('/').slice(0, -1).join('/') + '/';
  const EVENT_FIELD_CHANGED = 'celements:fieldChanged';

  const DATE_MARSHALLER_DE = Object.freeze({
    formatter: new Intl.DateTimeFormat('de-CH', {day: '2-digit', month: '2-digit', year: 'numeric' }),
    format: date => date ? DATE_MARSHALLER_DE.formatter.format(date) : '',
    parse: str => $j.format.date(str || '', 'dd.MM.y') || false
  });
  const TIME_MARSHALLER_DE = Object.freeze({
    formatter: new Intl.DateTimeFormat('de-CH', {hour: '2-digit', minute: '2-digit' }),
    format: date => date ? TIME_MARSHALLER_DE.formatter.format(date) : '',
    parse: str => $j.format.date(str || '', 'HH:mm') || false
  });

  class CelementsDateTimePicker {

    #inputField;
    #marshaller;
    #fieldValidator;
    #pickerConfig;
    #openPickerNow;

    #onChangedBind;
    #openPickerBind;

    constructor(inputField, buttonCssSelector, marshaller, fieldValidator, pickerConfig = {}) {
      if (!inputField) {
        throw new Error('no inputField provided');
      }
      this.#inputField = inputField;
      this.#marshaller = marshaller;
      this.#fieldValidator = fieldValidator;
      this.#pickerConfig = Object.freeze(Object.assign(
          this.#getDefaultPickerConfig(), pickerConfig));
      this.#openPickerNow = false;
      this.#onChangedBind = this.#onChanged.bind(this);
      this.#openPickerBind = this.openPicker.bind(this);
      Object.assign(this, CELEMENTS.mixins.Observable);
      this.#initField(buttonCssSelector, pickerConfig);
    }

    #initField(buttonCssSelector) {
      $j(this.htmlElem).datetimepicker(this.#pickerConfig)
      this.htmlElem.stopObserving('change', this.#onChangedBind);
      this.htmlElem.observe('change', this.#onChangedBind);
      this.#observePickerButton(buttonCssSelector);
    }

    #getDefaultPickerConfig() {
      // FIXME [CELDEV-904] DateTimePicker Language timing issue
      const lang = Validation.messages.get("admin-language");
      console.debug('lang: ', lang);
      return {
        'lang': lang || 'de',
        'closeOnDateSelect': true,
        'scrollInput': false,
        'onChangeDateTime': this.#onPickerSelect.bind(this),
        'onShow': this.#onPickerShow.bind(this),
        'onClose': function() { }
      };
    }

    #observePickerButton(buttonCssSelector) {
      const pickerButton = [this.htmlElem.nextElementSibling, this.htmlElem.previousElementSibling]
          .find(elem => elem && elem.matches(buttonCssSelector));
      if (pickerButton) {
        pickerButton.stopObserving('click', this.#openPickerBind);
        pickerButton.observe('click', this.#openPickerBind);
      } else {
        console.warn('not pickerButton found for ', this.htmlElem);
      }
    }

    get htmlElem() {
      return this.#inputField;
    }

    get value() {
      return this.htmlElem.value || '';
    }

    set value(value) {
      this.htmlElem.value = value || '';      
    }

    set pickerConfig(config) {
      $j(this.htmlElem).datetimepicker('setOptions',
          Object.assign({}, this.#pickerConfig, config));
    }

    openPicker(event) {
      event?.stop();
      this.#openPickerNow = true;
      $j(this.htmlElem).trigger('open');
    }

    #onPickerShow() {
      const showNow = this.#openPickerNow;
      this.#openPickerNow = false;
      console.debug('#onPickerShow: ', showNow);
      return showNow;
    }

    #onChanged() {
      console.debug("#onChanged", this.value);
      if (this.validate()) {
        this.celFire(EVENT_FIELD_CHANGED, {
          'dateOrTimeFieldPicker': this,
          'newValue': this.value
        });
      }
    }

    #onPickerSelect(value) {
      this.value = this.#marshaller.format(value);
      this.#onChanged();
    }

    #getValidatedValue() {
      if (!this.#fieldValidator) {
        return this.value;
      }
      const validated = this.#fieldValidator(this.value, {
        min: this.#marshaller.parse(this.htmlElem.dataset.min),
        max: this.#marshaller.parse(this.htmlElem.dataset.max)
      });
      return this.#marshaller.format(validated) || '';
    }

    validate() {
      if (!this.value) {
        return true;
      }
      const validatedValue = this.#getValidatedValue();
      const valid = this.value === validatedValue;
      this.htmlElem.classList.toggle('validation-failed', !valid);
      if (!valid) {
        console.info("validate: invalid", this.htmlElem, this.value, '->', validatedValue);
        this.value = validatedValue;
      }
      return valid;
    }

  }

  class CelementsDateTimePickerFactory {

    constructor() {
      if (this instanceof CelementsDateTimePickerFactory) {
        throw Error('CelementsDateTimePickerFactory is static and cannot be instantiated.');
      }
    }

    static createDatePickerField(dateInputField, config = {}) {
      return new CelementsDateTimePicker(dateInputField, '.CelDatePicker',
        DATE_MARSHALLER_DE, this.dateFieldValidator, Object.assign({
          'allowBlank': true,
          'dayOfWeekStart': 1,
          'format': 'd.m.Y', // JQuery DateTimePicker uses php-date-formatter by default
          'formatDate': 'd.m.Y',
          'timepicker': false
        }, config));
    }

    static dateFieldValidator(value, data = {}) {
      value = (value || '').toString().trim().replace(/[,-]/g, '.');
      const split = value.split('.').filter(Boolean);
      const day = Number(split[0]);
      const month = Number(split[1]);
      let year = Number(split[2]);
      if (year < 100) {
        year += Math.floor(new Date().getFullYear() / 100) * 100; // 21 -> 2021
      }
      if (value
          && (!split[0] || (!isNaN(day) && (day > 0) && (day <= 31)))
          && (!split[1] || (!isNaN(month) && (month > 0) && (month <= 12)))
          && (!split[2] || (!isNaN(year) && (year > 100) && (year <= 9999)))) {
        const curDate = new Date();
        const date = new Date(
          (year || curDate.getFullYear()),
          (month || (curDate.getMonth() + 1)) - 1,
          (day || curDate.getDate()));
        if (data.min && (data.min > date)) {
          console.info(date, 'is before defined minimum', data.min);
        } else if (data.max && (data.max < date)) {
          console.info(date, 'is after defined maximum', data.max);
        } else {
          return date;
        }
      }
      return null;
    }

    static createTimePickerField(timeInputField, config = {}) {
      return new CelementsDateTimePicker(timeInputField, '.CelTimePicker',
        TIME_MARSHALLER_DE, this.timeFieldValidator, Object.assign({
          'allowBlank': true,
          'datepicker': false,
          'format': 'H:i', // JQuery DateTimePicker uses php-date-formatter by default
          'formatTime': 'H:i'
        }, config));
    }

    static timeFieldValidator(value, data = {}) {
      value = (value || '').toString().trim().replace(/[\.,]/g, ':');
      const split = value.split(':').filter(Boolean);
      const hours = Number(split[0]);
      let minutes = Number(split[1]);
      if ((minutes < 6) && (split[1].trim().length == 1)) {
        minutes *= 10; // :5 -> 50 minutes
      }
      if (value
          && (!split[0] || (!isNaN(hours) && (hours >= 0) && (hours < 24)))
          && (!split[1] || (!isNaN(minutes) && (minutes >= 0) && (minutes < 60)))) {
        const time = new Date(0);
        time.setHours(hours || 0);
        time.setMinutes(minutes || 0);
        const asTimeInt = t => (t.getHours() << 6) + t.getMinutes();
        if (data.min && (asTimeInt(data.min) > asTimeInt(time)) && (asTimeInt(time) > 0)) {
          console.info(time, 'is before defined minimum', data.min);
        } else if (data.max && (asTimeInt(time) > asTimeInt(data.max)) && (asTimeInt(data.max) > 0)) {
          console.info(time, 'is after defined maximum', data.max);
        } else {
          return time;
        }
      }
      return null;
    }

  }

  class CelementsDateTimeController {

    #dateTimeComponent;
    #inputDateField;
    #inputTimeField;

    #onDateTimeChangeBind;

    constructor(dateTimeComponent) {
      this.#dateTimeComponent = dateTimeComponent;
      this.#onDateTimeChangeBind = this.#onDateTimeChange.bind(this);
    }

    initFields() {
      const pickerConfig = this.#collectPickerConfig();
      if (!this.#inputDateField && this.#dateTimeComponent.hasDateField()) {
        this.#initDateField(pickerConfig);
      }
      if (!this.#inputTimeField && this.#dateTimeComponent.hasTimeField()) {
        this.#initTimeField(pickerConfig);
      }
    }

    #initDateField(pickerConfig) {
      try {
        this.#inputDateField = CelementsDateTimePickerFactory.createDatePickerField(
            this.#dateTimeComponent.datePart, pickerConfig);
        this.#inputDateField.celStopObserving(EVENT_FIELD_CHANGED, this.#onDateTimeChangeBind);
        this.#inputDateField.celObserve(EVENT_FIELD_CHANGED, this.#onDateTimeChangeBind);
        this.#inputDateField.value = this.#dateTimeComponent.date || '';
      } catch (exp) {
        console.error('#initDateField: failed to initialize dateField.', this.#dateTimeComponent, exp);
      }
    }

    #initTimeField(pickerConfig) {
      try {
        this.#inputTimeField = CelementsDateTimePickerFactory.createTimePickerField(
            this.#dateTimeComponent.timePart, pickerConfig);
        this.#inputTimeField.celStopObserving(EVENT_FIELD_CHANGED, this.#onDateTimeChangeBind);
        this.#inputTimeField.celObserve(EVENT_FIELD_CHANGED, this.#onDateTimeChangeBind);
        this.#inputTimeField.value = this.#dateTimeComponent.time || '';
      } catch (exp) {
        console.error('#initTimeField: failed to initialize timeField.', this.#dateTimeComponent, exp);
      }
    }

    #collectPickerConfig() {
      const component = this.#dateTimeComponent;
      return Object.freeze({
        defaultDate: component.defaultPickerDate || false,
        defaultTime: component.defaultPickerTime || false,
        minDate: component.minDate || false,
        minTime: component.minTime || false,
        maxDate: component.maxDate || false,
        maxTime: component.maxTime || false,
        step: component.timeStep,
      });
    }

    #onDateTimeChange() {
      this.#updateComponentValuesFromInput();
      this.#dateTimeComponent.fireUpdated();
    }

    onAttributeChange() {
      // update picker config and validate in case max/min have changed
      const config = this.#collectPickerConfig();
      if (this.#inputDateField) {
        this.#inputDateField.pickerConfig = config;
        this.#inputDateField.validate();
      }
      if (this.#inputTimeField) {
        this.#inputTimeField.pickerConfig = config;
        this.#inputTimeField.validate();
      }
    }

    #updateComponentValuesFromInput() {
      console.debug("#updateComponentValuesFromInput", this.#dateTimeComponent.value);
      this.#dateTimeComponent.date = this.#inputDateField?.value;
      this.#dateTimeComponent.time = this.#inputTimeField?.value;
    }

  }

  class CelementsDateTimeField extends HTMLElement {

    datePart;
    timePart;
    #datePickerIcon;
    #timePickerIcon;
    #hiddenInputElem;
    #dateTimeFieldController;
    #connectedWrapper;

    #handleConnectionBind = this.#handleConnection.bind(this);
    #handleHailingBind = this.#handleHailing.bind(this);
    #updateBind = (event => this.update(event.memo)).bind(this);

    constructor() {
      super();
      this.attachShadow({ mode: 'open' });
      this.datePart = this.#newDisplayInputElem('date');
      this.timePart = this.#newDisplayInputElem('time');
      this.#hiddenInputElem = document.createElement('input');
      this.#hiddenInputElem.type = 'hidden';
      this.#dateTimeFieldController = new CelementsDateTimeController(this);
      Object.assign(this, CELEMENTS.mixins.Observable);
      this.#addCssFiles(this.shadowRoot, [
        '/file/resources/celRes/images/glyphicons-halflings/css/glyphicons-halflings.css',
        '/file/resources/celJS/jquery%2Ddatetimepicker/jquery.datetimepicker.css',
        curScriptDir + 'celements-date-time-field.css'
      ]);
      //HACK be sure to load the glyphicons-halflings.css in the html-page too.
      //HACK Because font-face will not work in shadow dom otherwise.
      this.#addCssFiles(document.head, [
        '/file/resources/celRes/images/glyphicons-halflings/css/glyphicons-halflings.css'
      ]);
    }

    #addCssFiles(parent, cssFiles) {
      for (let cssFile of cssFiles) {
        const elem = document.createElement('link');
        elem.rel = 'stylesheet';
        elem.media = 'all';
        elem.type = 'text/css';
        elem.href = cssFile + '?version=' + versionTimeStamp;
        parent.appendChild(elem);
      }
    }

    #newDisplayInputElem(type) {
      const elem = document.createElement('input');
      elem.type = 'text';
      elem.name = type + 'Part';
      elem.className = type + 'InputField';
      elem.autocomplete = 'off'
      return elem;
    }

    #addInputFields() {
      if (this.hasDateField()) {
        this.shadowRoot.appendChild(this.datePart);
      }
      if (this.hasTimeField()) {
        this.shadowRoot.appendChild(this.timePart);
      }
    }

    #addPickerIcons() {
      if (this.hasDateField()) {
        if (!this.#datePickerIcon) {
          this.#datePickerIcon = document.createElement('i');
          this.#datePickerIcon.title = 'Date Picker';
          this.#datePickerIcon.className = 'CelDatePicker dateInputField halflings halflings-calendar';
        }
        this.shadowRoot.insertBefore(this.#datePickerIcon, this.datePart.nextSibling);
      }
      if (this.hasTimeField()) {
        if (!this.#timePickerIcon) {
          this.#timePickerIcon = document.createElement('i');
          this.#timePickerIcon.title = 'Time Picker';
          this.#timePickerIcon.className = 'CelTimePicker timeInputField halflings halflings-time';
        }
        this.shadowRoot.insertBefore(this.#timePickerIcon, this.timePart.nextSibling);  
      }
    }

    #addHiddenInput() {
      this.parentElement.insertBefore(this.#hiddenInputElem, this);
    }

    connectedCallback() {
      console.debug('connectedCallback', this.isConnected, this);
      this.#addHiddenInput();
      this.#addInputFields();
      this.#addPickerIcons();
      this.#dateTimeFieldController.initFields();
      this.#connectInterdependence();
    }

    #connectInterdependence() {
      this.#connectedWrapper = this.#interdependenceWrapper;
      if (this.#connectedWrapper) {
        // observe wrapper for newly connected components
        this.#connectedWrapper.observe('celDateTime:connected', this.#handleConnectionBind);
        // observe being hailed by existing components upon connection
        this.celObserve('celDateTime:hail', this.#handleHailingBind);
        this.fire('celDateTime:connected');
        this.fireUpdated();
      }
    }

    disconnectedCallback() {
      console.debug('disconnectedCallback', this.isConnected, this);
      this.#hiddenInputElem.remove();
      this.#disconnectInterdependence();
    }

    #disconnectInterdependence() {
      if (this.#connectedWrapper) {
        this.fireUpdated();
        this.celFire('celDateTime:disconnected', { target: this });
        this.celStopObserving('celDateTime:hail', this.#handleHailingBind);
        this.#connectedWrapper.stopObserving('celDateTime:connected', this.#handleConnectionBind);
        this.#connectedWrapper = null;
      }
    }

    #handleConnection(event) {
      const component = event?.target || event?.memo?.target;
      if (component && component !== this) {
        console.debug('#handleConnection', this, event);
        this.#observeOrStopComponent(component);
        component.celFire('celDateTime:hail', {
          source: this,
          data: this.#collectInterdependenceData(component.isConnected)
        });
      }
    }

    #handleHailing(event) {
      const component = event?.memo?.source;
      if (component && component !== this) {
        console.debug('#handleHailing', this, component, event);
        this.#observeOrStopComponent(component);
        this.update(event.memo.data);
      }
    }

    #observeOrStopComponent(component) {
      const observeOrStop = (this.isConnected && component.isConnected) 
          ? CELEMENTS.mixins.Observable.celObserve 
          : CELEMENTS.mixins.Observable.celStopObserving;
      console.debug(this, observeOrStop.name, component);
      observeOrStop.call(component, 'celDateTime:updated', this.#updateBind);
      observeOrStop.call(component, 'celDateTime:disconnected', this.#handleConnectionBind);
    }

    static get observedAttributes() {
      return ['name', 'value', 'min-date', 'min-time', 'max-date', 'max-time', 'placeholder-date', 'placeholder-time'];
    }

    attributeChangedCallback(name, oldValue, newValue) {
      console.debug('attributeChangedCallback', this, name, newValue);
      switch (name) {
        case 'name':
          this.#hiddenInputElem.setAttribute(name, newValue);
          break;
        case 'value':
          this.#hiddenInputElem.setAttribute(name, newValue);
          this.timePart.dataset.min = this.minTime || '';
          this.timePart.dataset.max = this.maxTime || '';
          break;
        case 'min-date':
          this.datePart.dataset.min = this.minDate || '';
          this.timePart.dataset.min = this.minTime || '';
          break;
        case 'min-time':
          this.timePart.dataset.min = this.minTime || '';
          break;
        case 'max-date':
          this.datePart.dataset.max = this.maxDate || '';
          this.timePart.dataset.max = this.maxTime || '';
          break;
        case 'max-time':
          this.timePart.dataset.max = this.maxTime || '';
          break;
        case 'placeholder-date':
          this.datePart.setAttribute('placeholder', newValue || '');
          break;
        case 'placeholder-time':
          this.timePart.setAttribute('placeholder', newValue || '');
          break;
        default:
          console.warn('attributeChangedCallback not defined for ', name);
      }
      this.#dateTimeFieldController.onAttributeChange();
    }

    /**
     * format: "dd.MM.yyyy HH:mm"
     */
    get value() {
      return this.getAttribute('value') || '';
    }

    set value(newValue) {
      this.setAttribute('value', newValue);
    }

    #getValuePart(idx) {
      return this.value?.split(' ')[idx];
    }

    #setValueParts(...parts) {
      this.value = parts.filter(Boolean).join(' ');
    }

    get date() {
      return this.hasDateField() ? (this.#getValuePart(0) || this.defaultDate) : null;
    }

    set date(newValue) {
      if (this.hasDateField()) {
        this.#setValueParts(newValue || this.defaultDate, this.time);
      }
    }

    get time() {
      return this.hasTimeField() ? (this.#getValuePart(1) || this.defaultTime) : null;
    }

    set time(newValue) {
      if (this.hasTimeField()) {
        this.#setValueParts(this.date, newValue || this.defaultTime || '00:00');
      }
    }

    /**
     * whether the date input field is rendered (default true)
     */
    hasDateField() {
      return !this.hasAttribute('no-date-field');
    }

    /**
     * the default date to be set if the input is empty (default none)
     */
    get defaultDate() {
      return this.getAttribute('default-date');
    }

    /**
     * the default date of the picker if the input is empty (default current)
     */
    get defaultPickerDate() {
      return this.getAttribute('default-picker-date');
    }

    /**
     * the minimum date to be set (default none)
     */
    get minDate() {
      return this.getAttribute('min-date');
    }

    set minDate(newValue) {
      if (newValue !== this.minDate) {
        this.setAttribute('min-date', newValue);
      }
    }

    /**
     * the maximum date to be set (default none)
     */
    get maxDate() {
      return this.getAttribute('max-date');
    }

    set maxDate(newValue) {
      if (newValue !== this.maxDate) {
        this.setAttribute('max-date', newValue);
      }
    }

    /**
     * whether the time input field is rendered (default true)
     */
    hasTimeField() {
      return !this.hasAttribute('no-time-field');
    }

    /**
     * the default time to be set if the input is empty (default none)
     */
    get defaultTime() {
      return this.getAttribute('default-time');
    }

    /**
     * the minimum time to be set if the selected date is the minimum date (default none)
     */
    get minTime() {
      return (!this.hasDateField() || this.date === this.minDate)
             ? this.getAttribute('min-time') : null;
    }

    set minTime(newValue) {
      if (newValue !== this.getAttribute('min-time')) {
        this.setAttribute('min-time', newValue);
      }
    }

    /**
     * the maximum time to be set if the selected date is the maximum date (default none)
     */
    get maxTime() {
      const max = (!this.hasDateField() || this.date === this.maxDate)
             ? this.getAttribute('max-time') : null;
      return (max !== '00:00') ? max : null;
    }

    set maxTime(newValue) {
      if (newValue !== this.getAttribute('max-time')) {
        this.setAttribute('max-time', newValue);
      }
    }

    /**
     * the default time of the picker if the input is empty (default current)
     */
    get defaultPickerTime() {
      return this.getAttribute('default-picker-time');
    }

    /**
     * the time pickers stepping in minutes (default 30)
     */
    get timeStep() {
      return this.getAttribute('time-step') || 30;
    }

    get #interdependenceWrapper() {
      try {
        return this.closest(this.getAttribute('interdependence-wrapper') || 'form');
      } catch (exp) {
        console.debug('interdependenceWrapper: no valid parent defined');
      }
    }

    get #interdependenceRoles() {
      return this.getAttribute('interdependence-role')?.split(',') || [];
    }

    update(data) {
      console.debug('update', this, data);
      data = data || {};
      for (const key in data) {
        this[key] = data[key];
      }
    }

    fireUpdated() {
      this.celFire('celDateTime:updated', this.#collectInterdependenceData(this.isConnected));
    }

    #collectInterdependenceData(withValues = true) {
      const data = {};
      if (this.#interdependenceRoles.includes('min')) {
        data.minDate = (withValues ? this.date : null);
        data.minTime = (withValues ? this.time : null);
      }
      if (this.#interdependenceRoles.includes('max')) {
        data.maxDate = (withValues ? this.date : null);
        data.maxTime = (withValues ? this.time : null);
      }
      return Object.freeze(data);
    }

  }

  if (!customElements.get('cel-input-date-time')) {
    customElements.define('cel-input-date-time', CelementsDateTimeField);
  }

  class CelementsDateField extends CelementsDateTimeField {

    constructor() {
      super();
    }

    /**
     * whether the time input field is rendered (always false)
     */
    hasTimeField() {
      return false;
    }

  }

  if (!customElements.get('cel-input-date')) {
    customElements.define('cel-input-date', CelementsDateField);
  }

  class CelementsTimeField extends CelementsDateTimeField {

    constructor() {
      super();
    }

    /**
     * whether the date input field is rendered (always false)
     */
    hasDateField() {
      return false;
    }

  }

  if (!customElements.get('cel-input-time')) {
    customElements.define('cel-input-time', CelementsTimeField);
  }

})(window)
