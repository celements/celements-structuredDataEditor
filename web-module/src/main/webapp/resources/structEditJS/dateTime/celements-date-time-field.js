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

  class CelementsDateTimePicker {

    #inputField;
    #defaultFormat;
    #fieldValidator;
    #openPickerNow;
    #pickerConfig;

    constructor(inputField, buttonCssSelector, defaultFormat, fieldValidator, pickerConfig = {}) {
      if (!inputField) {
        throw new Error('no inputField provided');
      }
      this.#inputField = inputField;
      this.#defaultFormat = defaultFormat;
      this.#fieldValidator = fieldValidator;
      this.#pickerConfig = Object.freeze(Object.assign(this.#getDefaultPickerConfig(), pickerConfig));
      this.#openPickerNow = false;
      Object.assign(this, CELEMENTS.mixins.Observable);
      this.#initField(buttonCssSelector, pickerConfig);
    }

    #initField(buttonCssSelector) {
      $j(this.#inputField).datetimepicker(this.#pickerConfig)
      this.#observeChange(this.#inputField);
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
        'onChangeDateTime': this.#onChangeField.bind(this),
        'onShow': this.#onShow.bind(this),
        'onClose': function() { }
      };
    }

    #observeChange(elem) {
      const onChangedBind = this.#onChanged.bind(this);
      elem.stopObserving('change', onChangedBind);
      elem.observe('change', onChangedBind);
    }

    #observePickerButton(buttonCssSelector) {
      const pickerButton = this.#inputField.next(buttonCssSelector)
          || this.#inputField.previous(buttonCssSelector);
      if (pickerButton) {
        const openPickerBind = this.openPicker.bind(this);
        pickerButton.stopObserving('click', openPickerBind);
        pickerButton.observe('click', openPickerBind);
      } else {
        console.warn('not pickerButton found for ', this.#inputField);
      }
    }

    getHtmlElem() {
      return this.#inputField;
    }

    get value() {
      return this.#inputField.value;
    }

    set value(newValue) {
      this.#inputField.value = newValue;
    }

    openPicker(event) {
      event?.stop();
      this.#openPickerNow = true;
      $j(this.#inputField).trigger('open');
    }

    setPickerConfig(config) {
      console.debug('setPickerConfig', this, config);
      $j(this.#inputField).datetimepicker('setOptions',
          Object.assign({}, config, this.#pickerConfig));
    }

    #onShow(currentTime, data) {
      const showNow = this.#openPickerNow;
      this.#openPickerNow = false;
      console.debug('#onShow: ', showNow, currentTime, data);
      return showNow;
    }

    #onChanged() {
      console.debug("#onChanged", this.value);
      const validatedValue = this.#fieldValidator(this.value, this.#inputField.dataset);
      this.#inputField.classList.toggle('validation-failed', !validatedValue);
      if (this.value !== validatedValue) {
        this.#inputField.value = validatedValue;
      } else {
        this.celFire(EVENT_FIELD_CHANGED, {
          'dateOrTimeFieldPicker': this,
          'newValue': this.value
        });
      }
    }

    #onChangeField(currentValue, data) {
      const value = currentValue ? $j.format.date(currentValue, this.#defaultFormat) : "";
      let prototypejsEle = $(data[0]);
      prototypejsEle.value = value;
      console.debug('#onChangeField: ', value);
      this.#onChanged();
    }

  }

  class CelementsDateTimePickerFactory {

    createDatePickerField(dateInputField, config = {}) {
      return new CelementsDateTimePicker(dateInputField, '.CelDatePicker', 'dd.MM.y', this.#dateFieldValidator, Object.assign({
        'allowBlank': true,
        'dayOfWeekStart': 1,
        'format': 'd.m.Y',
        'formatDate': 'd.m.Y',
        'timepicker': false
      }, config));
    }

    #dateFieldValidator(value, data = {}) {
      console.debug("dateFieldValidator - from", value);
      value = (value || '').toString().trim().replace(/[,-]/g, '.');
      const split = value.split('.').filter(Boolean);
      const day = Number(split[0]);
      const month = Number(split[1]);
      let year = Number(split[2]);
      if (year < 100) {
        year += Math.floor(new Date().getFullYear() / 100) * 100; // 21 -> 2021
      }
      let validated = '';
      if (value
          && (!split[0] || (!isNaN(day) && (day > 0) && (day <= 31)))
          && (!split[1] || (!isNaN(month) && (month > 0) && (month <= 12)))
          && (!split[2] || (!isNaN(year) && (year > 100) && (year <= 9999)))) {
        const curDate = new Date();
        const date = new Date(
          (year || curDate.getFullYear()),
          (month || (curDate.getMonth() + 1)) - 1,
          (day || curDate.getDate()));
        const minDate = $j.format.date(data.min || '', 'dd.MM.y');
        const maxDate = $j.format.date(data.max || '', 'dd.MM.y');
        if (minDate && minDate > date) {
          console.info('date before defined minimum');
        } else if (maxDate && maxDate < date) {
          console.info('date after defined maximum');
        } else {
          validated = $j.format.date(date, 'dd.MM.y');
        }
      }
      console.debug("dateFieldValidator - to", validated);
      return validated || '';
    }

    createTimePickerField(timeInputField, config = {}) {
      return new CelementsDateTimePicker(timeInputField, '.CelTimePicker', 'HH:mm', this.#timeFieldValidator, Object.assign({
          'allowBlank': true,
          'datepicker': false,
          'format': 'H:i',
          'formatTime': 'H:i'
        }, config));
    }

    #timeFieldValidator(value, data = {}) {
      console.debug("timeFieldValidator - from", value);
      value = (value || '').toString().trim().replace(/[\.,]/g, ':');
      const split = value.split(':').filter(Boolean);
      const hours = Number(split[0]);
      let minutes = Number(split[1]);
      if (minutes < 6 && split[1].trim().length == 1) {
        minutes *= 10; // :5 -> 50 minutes
      }
      let validated = '';
      if (value
          && (!split[0] || (!isNaN(hours) && (hours >= 0) && (hours < 24)))
          && (!split[1] || (!isNaN(minutes) && (minutes >= 0) && (minutes < 60)))) {
        let date = new Date();
        date.setHours(hours || 0);
        date.setMinutes(minutes || 0);
        const timeStr = $j.format.date(date, 'HH:mm');
        const isMidnight = date.getHours() == 0 && date.getMinutes() == 0;
        if (!isMidnight && data.min > timeStr) {
          console.info('time before defined minimum');
        } else if (!isMidnight && data.max < timeStr) {
          console.info('time after defined maximum');
        } else {
          validated = timeStr;
        }
      }
      console.debug("timeFieldValidator - to", validated);
      return validated;
    }

  }

  class CelementsDateTimeController {

    #dateTimeComponent;
    #inputDateField;
    #inputTimeField;
    #dateTimePickerFactory;

    constructor(dateTimeComponent) {
      this.#dateTimeComponent = dateTimeComponent;
      this.#dateTimePickerFactory = new CelementsDateTimePickerFactory();
    }

    initFields() {
      const pickerConfig = this.#collectPickerConfig();
      if (!this.#inputDateField) {
        this.#initDateField(pickerConfig);
      }
      if (!this.#inputTimeField && this.#dateTimeComponent.hasTimeField()) {
        this.#initTimeField(pickerConfig);
      }
      this.#setMinMax(this.#dateTimeComponent.siblings);
    }

    #initDateField(pickerConfig) {
      try {
        this.#inputDateField = this.#dateTimePickerFactory
            .createDatePickerField(this.#dateTimeComponent.datePart, pickerConfig);
        this.#inputDateField.celObserve(EVENT_FIELD_CHANGED, this.#onDateTimeChange.bind(this));
        this.#inputDateField.value = this.#dateTimeComponent.date || '';
      } catch (exp) {
        console.error('#initDateField: failed to initialize dateField.', this.#dateTimeComponent, exp);
      }
    }

    #initTimeField(pickerConfig) {
      try {
        this.#inputTimeField = this.#dateTimePickerFactory
            .createTimePickerField(this.#dateTimeComponent.timePart, pickerConfig);
        this.#inputTimeField.celObserve(EVENT_FIELD_CHANGED, this.#onDateTimeChange.bind(this));
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
        minTime: ((component.date === component.minDate) ? component.minTime : null) || false,
        maxDate: component.maxDate || false,
        maxTime: ((component.date === component.maxDate) ? component.maxTime : null) || false,
        step: component.timeStep,
      });
    }

    #onDateTimeChange() {
      this.#updateComponentValuesFromInput();
      this.#setMinMax(this.#dateTimeComponent.siblings);
    }

    onAttributeChange() {
      const config = this.#collectPickerConfig();
      this.#inputDateField?.setPickerConfig(config);
      this.#inputTimeField?.setPickerConfig(config);
    }

    #updateComponentValuesFromInput() {
      this.#dateTimeComponent.date = this.#inputDateField?.value;
      this.#dateTimeComponent.time = this.#inputTimeField?.value;
      console.debug("#updateComponentValuesFromInput", this.#dateTimeComponent.value);
    }
  
    /**
     * set the date/time of this.#dateTimeComponent as maxDate/Time on all components before this and as minDate/Time after.
     */
    #setMinMax(dateTimeComponents) {
      let attribute = 'max';
      const date = this.#dateTimeComponent.date;
      const time = this.#dateTimeComponent.time;
      for (let component of dateTimeComponents) {
        if (component === this.#dateTimeComponent) {
          attribute = 'min';
        } else {
          component.setAttribute(attribute + '-date', date);
          component.setAttribute(attribute + '-time', time);
          console.debug('setMinMax: set ', attribute, '=', date + ' ' + time, ' on:', component);
        }
      }
    }

  }

  class CelementsDateTimeField extends HTMLElement {

    datePart;
    timePart;
    #datePickerIcon;
    #timePickerIcon;
    #hiddenInputElem;
    #dateTimeFieldController;

    constructor() {
      super();
      this.attachShadow({ mode: 'open' });
      this.datePart = this.#newDisplayInputElem('date');
      this.timePart = this.#newDisplayInputElem('time');
      this.#hiddenInputElem = document.createElement('input');
      this.#hiddenInputElem.type = 'hidden';
      this.#dateTimeFieldController = new CelementsDateTimeController(this);
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
      this.shadowRoot.appendChild(this.datePart);
      if (this.hasTimeField()) {
        this.shadowRoot.appendChild(this.timePart);
      }
    }

    #addPickerIcons() {
      if (!this.#datePickerIcon) {
        this.#datePickerIcon = document.createElement('i');
        this.#datePickerIcon.title = 'Date Picker';
        this.#datePickerIcon.className = 'CelDatePicker dateInputField halflings halflings-calendar';
      }
      this.shadowRoot.insertBefore(this.#datePickerIcon, this.datePart.nextSibling);
      if (!this.hasTimeField()) {
        return;
      }
      if (!this.#timePickerIcon) {
        this.#timePickerIcon = document.createElement('i');
        this.#timePickerIcon.title = 'Time Picker';
        this.#timePickerIcon.className = 'CelTimePicker timeInputField halflings halflings-time';
      }
      this.shadowRoot.insertBefore(this.#timePickerIcon, this.timePart.nextSibling);  
    }

    #addHiddenInput() {
      this.parentElement.insertBefore(this.#hiddenInputElem, this);
    }

    connectedCallback() {
      console.debug('connectedCallback', this.isConnected, this.hasTimeField(), this);
      this.#addHiddenInput();
      this.#addInputFields();
      this.#addPickerIcons();
      this.#dateTimeFieldController.initFields();
    }

    disconnectedCallback() {
      console.debug('disconnectedCallback', this.isConnected, this);
      this.#hiddenInputElem.remove();
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
          this.timePart.dataset.min = (this.date === this.minDate) ? this.minTime : '';
          this.timePart.dataset.max = (this.date === this.maxDate) ? this.maxTime : '';
          break;
        case 'min-date':
          this.datePart.dataset.min = newValue;
          this.timePart.dataset.min = (this.date === newValue) ? this.minTime : '';
          break;
        case 'min-time':
          this.timePart.dataset.min = (this.date === this.minDate) ? newValue : '';
          break;
        case 'max-date':
          this.datePart.dataset.max = newValue;
          this.timePart.dataset.max = (this.date === newValue) ? this.maxTime : '';
          break;
        case 'max-time':
          this.timePart.dataset.max = (this.date === this.maxDate) ? newValue : '';
          break;
        case 'placeholder-date':
          this.datePart.setAttribute('placeholder', newValue);
          break;
        case 'placeholder-time':
          this.timePart.setAttribute('placeholder', newValue);
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

    get date() {
      return this.#getValuePart(0) || this.defaultDate;
    }

    set date(newValue) {
      this.value = ((newValue || this.defaultDate) + " " + this.time).trim();
    }

    get time() {
      return this.#getValuePart(1) || this.defaultTime;
    }

    set time(newValue) {
      if (this.date && this.hasTimeField()) {
        this.value = (this.date + " " + (newValue || this.defaultTime || '00:00')).trim();
      }
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

    /**
     * the maximum date to be set (default none)
     */
    get maxDate() {
      return this.getAttribute('max-date');
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
      return this.getAttribute('min-time');
    }

    /**
     * the maximum time to be set if the selected date is the maximum date (default none)
     */
    get maxTime() {
      return this.getAttribute('max-time');
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

    /**
     * list of all CelementsDateTimeField siblings of this (including this) within the defined interdependence-wrapper
     */
    get siblings() {
      try {
        const wrapper = this.closest(this.getAttribute('interdependence-wrapper'));
        return [...wrapper?.querySelectorAll('cel-input-date, cel-input-date-time') || []];
      } catch (exp) {
        return [];
      }
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

})(window)
