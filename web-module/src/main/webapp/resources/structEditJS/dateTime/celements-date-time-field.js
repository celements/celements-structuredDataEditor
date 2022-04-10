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

  if (typeof window.CELEMENTS.structEdit == "undefined") { window.CELEMENTS.structEdit = {}; };

  if (typeof window.CELEMENTS.structEdit.DateOrTimeFieldPicker === 'undefined') {
    window.CELEMENTS.structEdit.DateOrTimeFieldPicker = Class.create({
      FIELD_CHANGED: 'celements:fieldChanged',
      _inputField: undefined,
      _buttonCssSelector: undefined,
      _defaultFormat: undefined,
      _pickerConfigObj: undefined,
      _fieldValidator: undefined,
      _onShowBind: undefined,
      _onChangedBind: undefined,
      _onChangeFieldBind: undefined,
      _openPickerNow: undefined,
      _pickerButtonClickHandlerBind: undefined,
      _pickerButton: undefined,

      initialize: function(inputField, buttonCssSelector,
        defaultFormat, pickerConfigObj, fieldValidator) {
        const _me = this;
        _me._inputField = inputField;
        _me._buttonCssSelector = buttonCssSelector;
        _me._defaultFormat = defaultFormat;
        _me._fieldValidator = fieldValidator;
        _me._onChangedBind = _me._onChanged.bind(_me);
        _me._onChangeFieldBind = _me._onChangeField.bind(_me);
        _me._onShowBind = _me._onShow.bind(_me);
        _me._pickerButtonClickHandlerBind = _me._pickerButtonClickHandler.bind(_me);
        _me._initPickerConfig(pickerConfigObj);
        _me._registerInputField();
        _me._registerPickerButton();
      },

      _initPickerConfig: function(configObj) {
        const _me = this;
        // FIXME [CELDEV-904] DateTimePicker Language timing issue
        const lang = Validation.messages.get("admin-language");
        console.debug('lang: ', lang);
        _me._pickerConfigObj = Object.assign({
          'lang': lang || 'de',
          'closeOnDateSelect': true,
          'scrollInput': false,
          'onChangeDateTime': _me._onChangeFieldBind,
          'onShow': _me._onShowBind,
          'onClose': function() { }
        }, configObj);
      },

      _registerInputField: function() {
        const _me = this;
        _me._inputField.stopObserving('change', _me._onChangedBind);
        _me._inputField.observe('change', _me._onChangedBind);
      },

      _registerPickerButton: function() {
        const _me = this;
        if (!_me._inputField) {
          console.warn('_registerPickerButton no inputField');
          return;
        }
        _me._pickerButton = _me._inputField.next(_me._buttonCssSelector)
          || _me._inputField.previous(_me._buttonCssSelector);
        if (_me._pickerButton) {
          _me._openPickerNow = false;
          _me._pickerButton.stopObserving('click', _me._pickerButtonClickHandlerBind);
          _me._pickerButton.observe('click', _me._pickerButtonClickHandlerBind);
          $j(_me._inputField).datetimepicker(_me._pickerConfigObj);
        } else {
          console.warn('not pickerButton found for ', _me._inputField);
        }
      },

      getHtmlElem: function() {
        const _me = this;
        return _me._inputField;
      },

      getValue: function() {
        const _me = this;
        return _me._inputField.value;
      },

      setValue: function(newValue) {
        const _me = this;
        _me._inputField.value = newValue;
      },

      _pickerButtonClickHandler: function(event) {
        const _me = this;
        event.stop();
        _me.openPicker();
      },

      openPicker: function() {
        const _me = this;
        _me._openPickerNow = true;
        $j(_me._inputField).trigger('open');
      },

      _onShow: function(currentTime, data) {
        const _me = this;
        const showNow = _me._openPickerNow;
        console.debug('_onShow: ', showNow, currentTime, data);
        _me._openPickerNow = false;
        return showNow;
      },

      _onChanged: function() {
        const _me = this;
        const newValue = _me.getValue();
        console.debug("_onChanged", newValue);
        const validatedValue = _me._fieldValidator(newValue);
        _me._inputField.classList.toggle('validation-failed', !validatedValue);
        if (newValue !== validatedValue) {
          _me._inputField.value = validatedValue;
        } else {
          _me.celFire(_me.FIELD_CHANGED, {
            'dateOrTimeFieldPicker': _me,
            'newValue': newValue
          });
        }
      },

      _onChangeField: function(currentValue, data) {
        const _me = this;
        const value = currentValue ? $j.format.date(currentValue, _me._defaultFormat) : "";
        let prototypejsEle = $(data[0]);
        prototypejsEle.value = value;
        console.debug('_onChangeField: ', value);
        _me._onChanged();
      }

    });
    window.CELEMENTS.structEdit.DateOrTimeFieldPicker.prototype = Object.extend(
      window.CELEMENTS.structEdit.DateOrTimeFieldPicker.prototype, CELEMENTS.mixins.Observable);
  }

  if (typeof window.CELEMENTS.structEdit.DateOrTimePickerFactory === 'undefined') {
    window.CELEMENTS.structEdit.DateOrTimePickerFactory = Class.create({

      createDatePickerField: function(dateInputField, configObj = {}) {
        const _me = this;
        const pickerConfigObj = Object.assign({
          'allowBlank': true,
          'dayOfWeekStart': 1,
          'format': 'd.m.Y',
          'timepicker': false
        }, configObj);
        return new CELEMENTS.structEdit.DateOrTimeFieldPicker(dateInputField, '.CelDatePicker',
          "dd.MM.y", pickerConfigObj, _me._dateFieldValidator);
      },

      _dateFieldValidator: function(value) {
        console.debug("dateFieldValidator - from", value);
        value = (value || "").toString().trim().replace(/[,-]/g, '.');
        const split = value.split(".")
          .filter(function(elem) { return elem; }); // filter falsy elements
        const day = Number(split[0]);
        const month = Number(split[1]);
        let year = Number(split[2]);
        if (year < 100) {
          year += Math.floor(new Date().getFullYear() / 100) * 100; // 21 -> 2021
        }
        let validated = "";
        if (value
          && (!split[0] || (!isNaN(day) && (day > 0) && (day <= 31)))
          && (!split[1] || (!isNaN(month) && (month > 0) && (month <= 12)))
          && (!split[2] || (!isNaN(year) && (year > 100) && (year <= 9999)))) {
          const curDate = new Date();
          const date = new Date(year || curDate.getFullYear(), (month || (curDate.getMonth() + 1)) - 1,
            day || curDate.getDate());
          validated = $j.format.date(date, "dd.MM.y");
        }
        console.debug("dateFieldValidator - to", validated);
        return validated;
      },

      createTimePickerField: function(timeInputField, configObj = {}) {
        const _me = this;
        const pickerConfigObj = Object.assign({
          'allowBlank': true,
          'datepicker': false,
          'format': 'H:i',
          'step': 30
        }, configObj);
        return new CELEMENTS.structEdit.DateOrTimeFieldPicker(timeInputField, '.CelTimePicker',
          "HH:mm", pickerConfigObj, _me._timeFieldValidator);
      },

      _timeFieldValidator: function(value) {
        console.debug("timeFieldValidator - from", value);
        value = (value || "").toString().trim().replace(/[\.,]/g, ':');
        const split = value.split(":").filter(Boolean);
        const hours = Number(split[0]);
        let minutes = Number(split[1]);
        if (minutes < 6 && split[1].trim().length == 1) {
          minutes *= 10; // :5 -> 50 minutes
        }
        let validated = "";
        if (value
          && (!split[0] || (!isNaN(hours) && (hours >= 0) && (hours < 24)))
          && (!split[1] || (!isNaN(minutes) && (minutes >= 0) && (minutes < 60)))) {
          let date = new Date();
          date.setHours(hours || 0);
          date.setMinutes(minutes || 0);
          validated = $j.format.date(date, "HH:mm");
        }
        console.debug("timeFieldValidator - to", validated);
        return validated;
      },

    });
  }

  if (typeof CelementsDateTimeController === 'undefined') {
    class CelementsDateTimeController {

      #dateTimeComponent;
      #inputDateField;
      #inputTimeField;
      #dateTimePickerFactory;

      #updateHiddenFromVisibleBind;

      constructor(dateTimeComponent) {
        this.#dateTimeComponent = dateTimeComponent;
        this.#dateTimePickerFactory = new CELEMENTS.structEdit.DateOrTimePickerFactory();
        this.#updateHiddenFromVisibleBind = this.#updateHiddenFromVisible.bind(this);
      }

      initFields() {
        if (!this.#inputDateField) {
          this.#initDateField();
        }
        if (!this.#inputTimeField && this.#dateTimeComponent.hasTimeField()) {
          this.#initTimeField();
        }
        this.#updateVisibleFromHidden();
      }

      #initDateField() {
        try {
          this.#inputDateField = this.#dateTimePickerFactory
            .createDatePickerField(this.#dateTimeComponent.datePart);
          this.#inputDateField.celStopObserving(this.#inputDateField.FIELD_CHANGED,
            this.#updateHiddenFromVisibleBind);
          this.#inputDateField.celObserve(this.#inputDateField.FIELD_CHANGED,
            this.#updateHiddenFromVisibleBind);
        } catch (exp) {
          console.error('#initDateField: failed to initialize dateField.', this.#dateTimeComponent, exp);
        }
      }

      #initTimeField() {
        try {
          this.#inputTimeField = this.#dateTimePickerFactory
            .createTimePickerField(this.#dateTimeComponent.timePart);
          this.#inputTimeField.celStopObserving(this.#inputTimeField.FIELD_CHANGED,
            this.#updateHiddenFromVisibleBind);
          this.#inputTimeField.celObserve(this.#inputTimeField.FIELD_CHANGED,
            this.#updateHiddenFromVisibleBind);
        } catch (exp) {
          console.error('#initTimeField: failed to initialize timeField.', this.#dateTimeComponent, exp);
        }
      }

      isFromDate() {
        return this.#dateTimeComponent.hasClassName('fromDateInput');
      }

      getDateValue() {
        return this.#getValuePart(0) || '';
      }

      getTimeValue() {
        return this.#getValuePart(1) || '00:00';
      }

      #getValuePart(idx) {
        return this.#dateTimeComponent.value?.split(' ')[idx];
      }

      #updateVisibleFromHidden() {
        const dateValue = this.getDateValue();
        this.#inputDateField.setValue(dateValue);
        let timeValue;
        if (this.#dateTimeComponent.hasTimeField()) {
           timeValue = this.getTimeValue();
           this.#inputTimeField.setValue(timeValue);
        }
        console.debug("#updateVisibleFromHidden", this.#dateTimeComponent, dateValue, timeValue);
        this.#updateHiddenFromVisible();
      }

      #updateHiddenFromVisible() {
        const dateValue = this.#inputDateField.getValue();
        const timeValue = this.#dateTimeComponent.hasTimeField() ? this.#inputTimeField.getValue() : "";
        const dateTimeValues = (dateValue + " " + timeValue).trim();
        this.#dateTimeComponent.value = dateTimeValues;
        console.debug("#updateHiddenFromVisible", dateTimeValues);
      }

    };
  }

  if (typeof CelementsDateTimeField === 'undefined') {
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
        return ['name', 'value', 'placeholder-date', 'placeholder-time'];
      }

      attributeChangedCallback(name, oldValue, newValue) {
        console.debug('attributeChangedCallback', this, name, newValue);
        switch (name) {
          case 'name':
          case 'value':
            this.#hiddenInputElem.setAttribute(name, newValue);
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

      hasTimeField() {
        return !this.hasAttribute('no-time-field');
      }

    }

    class CelementsDateField extends CelementsDateTimeField {

      constructor() {
        super();
      }

      hasTimeField() {
        return false;
      }

    }

    customElements.define('cel-input-date-time', CelementsDateTimeField);
    customElements.define('cel-input-date', CelementsDateField);
  }

})(window)
