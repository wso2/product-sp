/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { Component } from 'react';
import { MenuItem, SelectField } from 'material-ui';
import moment from 'moment';
import '../OpenTracingSearch.css';

export default class DateTimePicker extends Component {
    constructor(props) {
        super(props);
        
        let dt = new Date();
        this.state = {
            year: dt.getFullYear(),
            month: dt.getMonth(),
            days: dt.getDate(),
            time: moment().format('HH:mm:ss.SSS')
        };

        this.onDateChanged = this.onDateChanged.bind(this);
    }

    onDateChanged(attr, value) {
        let state = this.state;
        state[attr] = value;

        if (this.props.onChange) {
            let date = moment(`${state.year}:${(state.month + 1)}:${state.days} ${state.time}`, 'YYYY-MM-DD HH:mm:ss.SSS').toDate();
            this.props.onChange(date);
        }

        this.setState(state);
    }

    generateYears() {
        let years = [];
        for (let i = 1970; i <= 2099; i++) {
            years.push(<MenuItem key={i} value={i} primaryText={i} />);
        }
        return years;
    }

    generateMonths() {
        let months = [];
        let monthArray = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September',
            'October', 'November', 'December'];
        for (let i = 0; i < monthArray.length; i++) {
            months.push(<MenuItem key={i} value={i} primaryText={monthArray[i]} />);
        }
        return months;
    }

    generateDays(year, month) {
        let dayComponents = [];
        let days = 0;

        if (month === 1) {
            if (this.isLeapYear(year)) days = 29;
            else days = 28;
        } else if ((month < 7 && ((month + 1) % 2 === 1)) || (month > 6 && ((month + 1) % 2 === 0))) {
            days = 31;
        } else {
            days = 30;
        }

        for (let i = 1; i <= days; i++) {
            dayComponents.push(<MenuItem key={i} value={i} primaryText={i} />);
        }
        return dayComponents;
    }

    isLeapYear(year) {
        return ((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0);
    }

    render() {
        let { year, month, days, time } = this.state;
        time = moment(time, 'HH:mm:ss').format('HH:mm:ss.000');

        return (
            <div className="date-time-picker">
                <div>
                    <SelectField value={year} onChange={(e, v) => this.onDateChanged('year', v)}>
                        {this.generateYears()}
                    </SelectField>
                    <SelectField value={month} onChange={(e, v) => this.onDateChanged('month', v)}>
                        {this.generateMonths()}
                    </SelectField>
                    <SelectField value={days} onChange={(e, v) => this.onDateChanged('days', v)}>
                        {this.generateDays(year, month)}
                    </SelectField>
                    <div className="time-field">
                        <input type="time" step="60" value={time} onChange={e => this.onDateChanged('time', e)} />
                    </div>
                </div>
            </div>
        );
    }
}
