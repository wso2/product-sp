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
import { RaisedButton } from 'material-ui';
import DateTimePicker from "./DateTimePicker";

export default class CustomTimeRangeSelector extends Component {
    constructor(props) {
        super(props);

        this.selectedDate = {
            from: new Date(),
            to: new Date()
        };
        this.onDateChanged = this.onDateChanged.bind(this);
    }

    onDateChanged(attr, value) {
        this.selectedDate[attr] = value;
        if (this.props.onChange) {
            this.props.onChange(this.selectedDate.from, this.selectedDate.to);
        }
    }

    render() {
        let { onChange } = this.props;
        return (
            <div>
                <div>
                    <DateTimePicker onChange={d => this.onDateChanged('from', d)} style={{float: 'left'}} />
                    <div style={{float: 'left', margin: '15px 20px auto 20px'}}> To </div>
                    <DateTimePicker onChange={d => this.onDateChanged('to', d)} />
                </div>
            </div>
        );
    }
}