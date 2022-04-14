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
    #buttonCssSelector;
    #defaultFormat;
    #pickerConfigObj;
    #fieldValidator;
    #onChangedBind;
    #openPickerNow;
    #pickerButtonClickHandlerBind;
    #pickerButton;

    constructor(inputField, buttonCssSelector,
      defaultFormat, pickerConfigObj, fieldValidator) {
      this.#inputField = inputField;
      this.#buttonCssSelector = buttonCssSelector;
      this.#defaultFormat = defaultFormat;
      this.#fieldValidator = fieldValidator;
      this.#onChangedBind = this.#onChanged.bind(this);
      this.#pickerButtonClickHandlerBind = this.#pickerButtonClickHandler.bind(this);
      this.#initPickerConfig(pickerConfigObj);
      this.#registerInputField();
      this.#registerPickerButton();
    }

    #initPickerConfig(configObj) {
      // FIXME [CELDEV-904] DateTimePicker Language timing issue
      const lang = Validation.messages.get("admin-language");
      console.debug('lang: ', lang);
      this.#pickerConfigObj = Object.assign({
        'lang': lang || 'de',
        'closeOnDateSelect': true,
        'scrollInput': false,
        'onChangeDateTime': this.#onChangeField.bind(this),
        'onShow': this.#onShow.bind(this),
        'onClose': function() { }
      }, configObj);
    }

    #registerInputField() {
      this.#inputField.stopObserving('change', this.#onChangedBind);
      this.#inputField.observe('change', this.#onChangedBind);
    }

    #registerPickerButton() {
      if (!this.#inputField) {
        console.warn('#registerPickerButton no inputField');
        return;
      }
      this.#pickerButton = this.#inputField.next(this.#buttonCssSelector)
          || this.#inputField.previous(this.#buttonCssSelector);
      if (this.#pickerButton) {
        this.#openPickerNow = false;
        this.#pickerButton.stopObserving('click', this.#pickerButtonClickHandlerBind);
        this.#pickerButton.observe('click', this.#pickerButtonClickHandlerBind);
        $j(this.#inputField).datetimepicker(this.#pickerConfigObj);
      } else {
        console.warn('not pickerButton found for ', this.#inputField);
      }
    }

    getHtmlElem() {
      return this.#inputField;
    }

    getValue() {
      return this.#inputField.value;
    }

    setValue(newValue) {
      this.#inputField.value = newValue;
    }

    #pickerButtonClickHandler(event) {
      event.stop();
      this.openPicker();
    }

    openPicker() {
      this.#openPickerNow = true;
      $j(this.#inputField).trigger('open');
    }

    #onShow(currentTime, data) {
      const showNow = this.#openPickerNow;
      console.debug('#onShow: ', showNow, currentTime, data);
      this.#openPickerNow = false;
      return showNow;
    }

    #onChanged() {
      const newValue = this.getValue();
      console.debug("#onChanged", newValue);
      const validatedValue = this.#fieldValidator(newValue, {
        min: this.#inputField.dataset.min,
        max: this.#inputField.dataset.max
      });
      this.#inputField.classList.toggle('validation-failed', !validatedValue);
      if (newValue !== validatedValue) {
        this.#inputField.value = validatedValue;
      } else {
        this.celFire(EVENT_FIELD_CHANGED, {
          'dateOrTimeFieldPicker': this,
          'newValue': newValue
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
  CelementsDateTimePicker.prototype = Object.extend(CelementsDateTimePicker.prototype, CELEMENTS.mixins.Observable);


  class CelementsDateTimePickerFactory {

    createDatePickerField(dateInputField, configObj = {}) {
      const pickerConfigObj = Object.assign({
        'allowBlank': true,
        'dayOfWeekStart': 1,
        'format': 'd.m.Y',
        'timepicker': false
      }, configObj);
      return new CelementsDateTimePicker(dateInputField,
        '.CelDatePicker', 'dd.MM.y', pickerConfigObj, this.#dateFieldValidator);
    }

    #dateFieldValidator(value) {
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
        const minDate = $j.format.date(options.min || '', 'dd.MM.y');
        const maxDate = $j.format.date(options.max || '', 'dd.MM.y');
        if (minDate && minDate > date) {
          console.info('date before defined minimum');
        } else if (maxDate && maxDate < date) {
          console.info('date after defined minimum');
        } else {
          validated = $j.format.date(date, 'dd.MM.y');
        }
      }
      console.debug("dateFieldValidator - to", validated);
      return validated || '';
    }

    createTimePickerField(timeInputField, configObj = {}) {
      const pickerConfigObj = Object.assign({
        'allowBlank': true,
        'datepicker': false,
        'format': 'H:i'
      }, configObj);
      return new CelementsDateTimePicker(timeInputField,
        '.CelTimePicker', 'HH:mm', pickerConfigObj, this.#timeFieldValidator);
    }

    #timeFieldValidator(value) {
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
        // TODO min/max validation, needs date, 00:00 needs to be allowed
        validated = $j.format.date(date, 'HH:mm');
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
      if (!this.#inputDateField) {
        this.#initDateField();
      }
      if (!this.#inputTimeField && this.#dateTimeComponent.hasTimeField()) {
        this.#initTimeField();
      }
      this.#updateVisibleFromHidden();
      this.#checkInterdependence();
    }

    #initDateField() {
      try {
        this.#inputDateField = this.#dateTimePickerFactory
          .createDatePickerField(this.#dateTimeComponent.datePart, {
            defaultDate: this.#dateTimeComponent.getDefaultPickerDate() || false,
            // TODO this is static, they need to updated on attribute change
            minDate: this.#dateTimeComponent.getMinDate() || false,
            maxDate: this.#dateTimeComponent.getMaxDate() || false
          });
        this.#inputDateField.celObserve(EVENT_FIELD_CHANGED, this.#onDateTimeChange.bind(this));
      } catch (exp) {
        console.error('#initDateField: failed to initialize dateField.', this.#dateTimeComponent, exp);
      }
    }

    #initTimeField() {
      try {
        this.#inputTimeField = this.#dateTimePickerFactory
          .createTimePickerField(this.#dateTimeComponent.timePart, {
            defaultTime: this.#dateTimeComponent.getDefaultPickerTime() || false,
            step: this.#dateTimeComponent.getTimeStep(),
            // TODO this is static, they need to updated on attribute change
            // TODO only set min/max time if date == min/maxDate
            minTime: this.#dateTimeComponent.getMinTime() || false,
            maxTime: this.#dateTimeComponent.getMaxTime() || false
          });
        this.#inputTimeField.celObserve(EVENT_FIELD_CHANGED, this.#onDateTimeChange.bind(this));
      } catch (exp) {
        console.error('#initTimeField: failed to initialize timeField.', this.#dateTimeComponent, exp);
      }
    }

    getDateValue() {
      return this.#getValuePart(0) || this.#dateTimeComponent.getDefaultDate();
    }

    getTimeValue() {
      return this.#getValuePart(1) || this.#dateTimeComponent.getDefaultTime();
    }

    #getValuePart(idx) {
      return this.#dateTimeComponent.value?.split(' ')[idx];
    }

    #onDateTimeChange() {
      this.#updateHiddenFromVisible();
      this.#checkInterdependence();
    }

    #updateVisibleFromHidden() {
      const dateValue = this.getDateValue() || '';
      this.#inputDateField.setValue(dateValue);
      let timeValue;
      if (this.#dateTimeComponent.hasTimeField()) {
          timeValue = this.getTimeValue() || '';
          this.#inputTimeField.setValue(timeValue);
      }
      console.debug("#updateVisibleFromHidden", this.#dateTimeComponent, dateValue, timeValue);
      this.#updateHiddenFromVisible();
    }

    #updateHiddenFromVisible() {
      let value = this.#inputDateField.getValue()
          || this.#dateTimeComponent.getDefaultDate();
      if (value && this.#dateTimeComponent.hasTimeField()) {
        value += " " + (this.#inputTimeField.getValue()
            || this.#dateTimeComponent.getDefaultTime()
            || '00:00');
      }
      this.#dateTimeComponent.value = value;
      console.debug("#updateHiddenFromVisible", this.#dateTimeComponent.value);
    }

    #checkInterdependence() {
      const selector = this.#dateTimeComponent.getInterdependenceWrapperSelector();
      if (selector) {
        const dateTimeComponents = this.#dateTimeComponent
          .closest(selector)
          ?.querySelectorAll('cel-input-date, cel-input-date-time')
          || [];
        this.#setMinMax(dateTimeComponents);
      }
    }
  
    #setMinMax(dateTimeComponents) {
      let attribute = 'max';
      const dateValue = this.#dateTimeComponent.getDateValue();
      const timeValue = this.#dateTimeComponent.getTimeValue();
      for (let component of dateTimeComponents) {
        if (component === this.#dateTimeComponent) {
          attribute = 'min';
        } else {
          component.setAttribute(attribute + '-date', dateValue);
          component.setAttribute(attribute + '-time', timeValue);
          console.debug('setMinMax: set ', attribute, '=', dateValue + ' ' + timeValue, ' on:', component);
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
    #value;

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
        case 'value':
          this.#hiddenInputElem.setAttribute(name, newValue);
          break;
        case 'min-date':
          this.datePart.dataset.min = newValue;
          break;
        case 'min-time':
          this.timePart.dataset.min = newValue;
          break;
        case 'max-date':
          this.datePart.dataset.max = newValue;
          break;
        case 'max-time':
          this.timePart.dataset.max = newValue;
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
    }

    get value() {
      return this.#value || this.getAttribute('value') || '';
    }

    set value(newValue) {
      this.#value = newValue;
      this.setAttribute('value', this.#value);
    }

    getInterdependenceWrapperSelector() {
      return this.getAttribute('interdependence-wrapper');
    }

    /**
     * the default date to be set if the input is empty (default none)
     */
    getDefaultDate() {
      return this.getAttribute('default-date');
    }

    getMinDate() {
      return this.getAttribute('min-date');
    }

    getMaxDate() {
      return this.getAttribute('max-date');
    }

    /**
     * the default date of the picker if the input is empty (default current)
     */
    getDefaultPickerDate() {
      return this.getAttribute('default-picker-date');
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
    getDefaultTime() {
      return this.getAttribute('default-time');
    }

    getMinTime() {
      return this.getAttribute('min-time');
    }

    getMaxTime() {
      return this.getAttribute('max-time');
    }

    /**
     * the default time of the picker if the input is empty (default current)
     */
    getDefaultPickerTime() {
      return this.getAttribute('default-picker-time');
    }

    /**
     * the time pickers stepping in minutes (default 30)
     */
    getTimeStep() {
      return this.getAttribute('time-step') || 30;
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
