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
        console.log("_onChanged", newValue);
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

      createDatePickerField: function(dateInputField) {
        const _me = this;
        const pickerConfigObj = {
          'allowBlank': true,
          'dayOfWeekStart': 1,
          'format': 'd.m.Y',
          'timepicker': false
        };
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

      createTimePickerField: function(timeInputField) {
        const _me = this;
        const pickerConfigObj = {
          'allowBlank': true,
          'datepicker': false,
          'format': 'H:i',
          'step': 30
        };
        return new CELEMENTS.structEdit.DateOrTimeFieldPicker(timeInputField, '.CelTimePicker',
          "HH:mm", pickerConfigObj, _me._timeFieldValidator);
      },

      _timeFieldValidator: function(value) {
        console.debug("timeFieldValidator - from", value);
        value = (value || "").toString().trim().replace(/[\.,]/g, ':');
        const split = value.split(":")
          .filter(function(elem) { return elem; }); // filter falsy elements
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

  if (typeof window.CELEMENTS.structEdit.DateTimeInputHandler === 'undefined') {
    window.CELEMENTS.structEdit.DateTimeInputHandler = Class.create({
      _updateVisibleFromHiddenBind: undefined,
      _updateHiddenFromVisibleBind: undefined,
      _allDayChangedHandlerBind: undefined,
      _dateTimeComponent: undefined,
      _hiddenDateTimeField: undefined,
      _inputDateField: undefined,
      _inputTimeField: undefined,
      _dateOrTimePickerFactory: new CELEMENTS.structEdit.DateOrTimePickerFactory(),

      initialize: function(dateTimeComponent) {
        const _me = this;
        _me._dateTimeComponent = dateTimeComponent;
        _me._updateVisibleFromHiddenBind = _me._updateVisibleFromHidden.bind(_me);
        _me._allDayChangedHandlerBind = _me._allDayChangedHandler.bind(_me);
        _me._updateHiddenFromVisibleBind = _me._updateHiddenFromVisible.bind(_me);
        _me._hiddenDateTimeField = dateTimeComponent._hiddenInputElem;
        _me._initDateField();
        _me._initTimeField();
        // dateTimeComponent.celObserve(dateTimeComponent.COMP_ALL_DAY_CHANGED,
        // _me._allDayChangedHandlerBind);
        _me._updateVisibleFromHidden();
        // _me._rootElem.show();
      },

      _initDateField: function() {
        const _me = this;
        try {
          _me._inputDateField = _me._dateOrTimePickerFactory.createDatePickerField(
            _me._dateTimeComponent._datePart);
          _me._inputDateField.celStopObserving(_me._inputDateField.FIELD_CHANGED,
            _me._updateHiddenFromVisibleBind);
          _me._inputDateField.celObserve(_me._inputDateField.FIELD_CHANGED,
            _me._updateHiddenFromVisibleBind);
        } catch (exp) {
          console.error('_initDateField: failed to initialize dateField.', _me._dateTimeComponent, exp);
        }
      },

      _initTimeField: function() {
        const _me = this;
        try {
          _me._inputTimeField = _me._dateOrTimePickerFactory.createTimePickerField(
            _me._dateTimeComponent._timePart);
          _me._inputTimeField.celStopObserving(_me._inputTimeField.FIELD_CHANGED,
            _me._updateHiddenFromVisibleBind);
          _me._inputTimeField.celObserve(_me._inputTimeField.FIELD_CHANGED,
            _me._updateHiddenFromVisibleBind);
        } catch (exp) {
          console.error('_initTimeField: failed to initialize timeField.', _me._dateTimeComponent, exp);
        }
      },

      isFromDate: function() {
        const _me = this;
        return _me._hiddenDateTimeField.hasClassName('fromDateInput');
      },

      getTimeValue: function() {
        const _me = this;
        const dateTimeValues = _me._hiddenDateTimeField.value.split(' ');
        const timeValue = dateTimeValues[1] || "00:00";
        console.log('getTimeValue: ', timeValue);
        return timeValue;
      },

      getDateValue: function() {
        const _me = this;
        const dateTimeValues = _me._hiddenDateTimeField.value.split(' ');
        const dateValue = dateTimeValues[0];
        console.log('getDateValue: ', dateValue);
        return dateValue;
      },

      _updateVisibleFromHidden: function() {
        const _me = this;
        const dateValue = _me.getDateValue();
        _me._inputDateField.setValue(dateValue);
        const timeValue = _me.getTimeValue();
        _me._inputTimeField.setValue(timeValue);
        console.log("_updateVisibleFromHidden", _me._hiddenDateTimeField, dateValue, timeValue);
        _me._updateHiddenFromVisible();
      },

      _updateHiddenFromVisible: function() {
        const _me = this;
        const dateValue = _me._inputDateField.getValue();
        const timeValue = _me._inputTimeField.getValue();
        // if (_me._isAllDayFnc()) {
        // timeValue = "00:00";
        // }
        const dateTimeValues = dateValue + " " + timeValue;
        _me._hiddenDateTimeField.value = dateTimeValues;
        console.log("_updateHiddenFromVisible", dateTimeValues);
      },

      _allDayChangedHandler: function(event) {
        const _me = this;
        _me._updateHiddenFromVisible();
      }

    });
  }

  if (typeof CelementsDateTimeField === 'undefined') {
    class CelementsDateTimeField extends HTMLElement {
      constructor() {
        super();
        const _me = this;
        _me.attachShadow({ mode: 'open' });
        _me.addCssFilesToParent();
        _me.addCssFiles();
        _me.addInputFields();
        _me.addPickerIcons();
      }

      addCssFilesToParent() {
        //HACK be sure to load the glyphicons-halflings.css in the html-page too.
        //HACK Because font-face will not work in shadow dom otherwise.
        const cssFiles = ['celRes/images/glyphicons-halflings/css/glyphicons-halflings.css'];
        cssFiles.forEach(function(cssFile) {
          const cssElem = new Element('link', {
            'rel': 'stylesheet',
            'media': 'all',
            'type': 'text/css',
            'href': '/file/resources/' + cssFile + '?version=' + versionTimeStamp
          });
          document.head.append(cssElem);
        });
      }

      addCssFiles() {
        const _me = this;
        //HACK be sure to load the glyphicons-halflings.css in the html-page too.
        //HACK Because font-face will not work in shadow dom otherwise.
        const cssFiles = ['celRes/images/glyphicons-halflings/css/glyphicons-halflings.css',
          'celJS/jquery%2Ddatetimepicker/jquery.datetimepicker.css'
        ];
        cssFiles.forEach(function(cssFile) {
          const cssElem = new Element('link', {
            'rel': 'stylesheet',
            'media': 'all',
            'type': 'text/css',
            'href': '/file/resources/' + cssFile + '?version=' + versionTimeStamp
          });
          _me.shadowRoot.appendChild(cssElem);
        });
        const dateTimeCssElem = new Element('link', {
          'rel': 'stylesheet',
          'media': 'all',
          'type': 'text/css',
          'href': curScriptDir + 'celements-date-time-field.css' + '?version='
            + versionTimeStamp
        });
        _me.shadowRoot.appendChild(dateTimeCssElem);
      }

      addInputFields() {
        const _me = this;
        _me._datePart = new Element('input', {
          'type': 'text',
          'name': 'datePart',
          'class': 'dateInputField',
          'autocomplete': 'off'
        });
        _me.shadowRoot.appendChild(_me._datePart);
        _me._timePart = new Element('input', {
          'type': 'text',
          'name': 'timePart',
          'class': 'timeInputField',
          'autocomplete': 'off'
        });
        _me.shadowRoot.appendChild(_me._timePart);
      }

      addPickerIcons() {
        const _me = this;
        _me._datePickerIcon = new Element('i', {
          'title': 'Date Picker',
          'class': 'CelDatePicker dateInputField halflings halflings-calendar'
        });
        _me.shadowRoot.insertBefore(_me._datePickerIcon, _me._datePart.nextSibling);
        _me._timePickerIcon = new Element('i', {
          'title': 'Time Picker',
          'class': 'CelTimePicker timeInputField halflings halflings-time'
        });
        _me.shadowRoot.insertBefore(_me._timePickerIcon, _me._timePart.nextSibling);
      }

      connectedCallback() {
        const _me = this;
        console.log('DateTimeFiled connectedCallback: ', _me.isConnected, _me.parentElement);
        if (!_me._hiddenInputElem) {
          _me._hiddenInputElem = new Element('input', {
            'type': 'hidden',
            'name': _me.getAttribute('name'),
            'value': _me.value
          });
        }
        _me.parentElement.insertBefore(_me._hiddenInputElem, _me);
        if (!_me._dateTimeFieldControler) {
          _me._dateTimeFieldControler = new CELEMENTS.structEdit.DateTimeInputHandler(_me);
        }
      }

      disconnectedCallback() {
        const _me = this;
        console.log('DateTimeFiled disconnectedCallback: ', _me.isConnected, _me.parentElement,
          _me._hiddenInputElem);
        _me._hiddenInputElem.remove();
      }

      static get observedAttributes() {
        return ['name'];
      }

      attributeChangedCallback() {
        const _me = this;
        console.log('DateTimeFiled attributeChangedCallback: ', _me._hiddenInputElem);
        _me._hiddenInputElem.setAttribute('name', _me.getAttribute('name'));
      }

      get value() {
        const _me = this;
        console.log('get value: ', _me._value, _me.getAttribute('value'));
        return _me._value || _me.getAttribute('value') || "";
      }

      set value(newValue) {
        const _me = this;
        console.log('set value: ', _me._value, newValue);
        _me._value = newValue;
      }

    }

    customElements.define('cel-input-date-time', CelementsDateTimeField);
  }
})(window)