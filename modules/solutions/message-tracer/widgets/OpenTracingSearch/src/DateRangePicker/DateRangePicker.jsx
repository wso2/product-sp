/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import React, { Component } from 'react';
import GranularityModeSelector from './GranularityModeSelector';
import CustomTimeRangeSelector from './CustomTimeRangeSelector';
import Moment from 'moment';
import PropTypes from 'prop-types';

export default class DateRangePicker extends Component {

    constructor(props) {
        super(props);
        this.state = {
            granularityMode: null
        };
        this.onGranularityChanged = this.onGranularityChanged.bind(this);
        this.onTimeChanged = this.onTimeChanged.bind(this);
        this.renderPredefinedTimeRangeSelector = this.renderPredefinedTimeRangeSelector.bind(this);
    }

    onGranularityChanged(granularityMode) {
        this.setState({ granularityMode });
        if (granularityMode === 'custom') {
            return;
        }
        let from = '';
        switch (granularityMode) {
            case '1 Min':
                from = Moment().subtract(1, 'minutes').toDate();
                break;
            case '15 Min':
                from = Moment().subtract(15, 'minutes').toDate();
                break;
            case '1 Hour':
                from = Moment().subtract(1, 'hours').toDate();
                break;
            case '1 Day':
                from = Moment().subtract(1, 'days').toDate();
                break;
            case '7 Days':
                from = Moment().subtract(7, 'days').toDate();
                break;
            case '1 Month':
                from = Moment().subtract(1, 'months').toDate();
                break;
            case '3 Months':
                from = Moment().subtract(3, 'months').toDate();
                break;
            case '6 Months':
                from = Moment().subtract(6, 'months').toDate();
                break;
            case '1 Year':
                from = Moment().subtract(1, 'years').toDate();
                break;
        }

        this.onTimeChanged(from, new Date());
    }

    onTimeChanged(from, to) {
        if (this.props.onChange) {
            this.props.onChange(from, to);
        }
    }

    renderPredefinedTimeRangeSelector(mode) {
        let startTime = null, endTime = null;

        switch (mode) {
            case '1 Min':
                startTime = Moment().subtract(1, 'minutes').format("DD-MMM-YYYY hh:mm A");
                endTime = Moment().format("DD-MMM-YYYY hh:mm A");
                break;
            case '15 Min':
                startTime = Moment().subtract(15, 'minutes').format("DD-MMM-YYYY hh:mm A");
                endTime = Moment().format("DD-MMM-YYYY hh:mm A");
                break;
            case '1 Hour':
                startTime = Moment().subtract(1, 'hours').format("DD-MMM-YYYY hh:mm A");
                endTime = Moment().format("DD-MMM-YYYY hh:mm A");
                break;
            case '1 Day':
                startTime = Moment().subtract(1, 'days').format("DD-MMM-YYYY");
                endTime = Moment().format("DD-MMM-YYYY");
                break;
            case '7 Days':
                startTime = Moment().subtract(7, 'days').format("DD-MMM-YYYY");
                endTime = Moment().format("DD-MMM-YYYY");
                break;
            case '1 Month':
                startTime = Moment().subtract(1, 'months').format("MMM-YYYY");
                endTime = Moment().format('MMM-YYYY');
                break;
            case '3 Months':
                startTime = Moment().subtract(3, 'months').format('MMM-YYYY');
                endTime = Moment().format('MMM-YYYY');
                break;
            case '6 Months':
                startTime = Moment().subtract(6, 'months').format('MMM-YYYY');
                endTime = Moment().format('MMM-YYYY');
                break;
            case '1 Year':
                startTime = Moment().subtract(1, 'years').format('YYYY');
                endTime = Moment().format('YYYY');
                break;
        }

        let styles = {
            color: this.context.muiTheme.palette.textColor
        };

        if (startTime && endTime) {
            return <div className="predefined-date-wrapper" style={styles}>{startTime} To {endTime}</div>;
        } 
        return <div className="predefined-date-placeholder" />;
    }

    render() {
        return (
            <div className="date-range-picker-container">
                <div className="label-row">
                    <label>{this.props.labelText}</label>
                    <GranularityModeSelector onChange={this.onGranularityChanged} />
                    <div className="date-range-selector-container">
                        {
                            this.state.granularityMode === 'custom' ? 
                                <CustomTimeRangeSelector onChange={this.onTimeChanged} /> : 
                                this.renderPredefinedTimeRangeSelector(this.state.granularityMode)
                        }
                    </div>
                </div>
            </div>
        );
    }
}

DateRangePicker.contextTypes = {
    muiTheme: PropTypes.object
};
