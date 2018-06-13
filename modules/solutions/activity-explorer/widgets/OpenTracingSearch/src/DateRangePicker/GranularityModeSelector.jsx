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
// import { FlatButton, IconButton } from 'material-ui';
import HighGranularityMode from '@material-ui/icons/KeyboardArrowRight';
import LowGranularityMode from '@material-ui/icons/KeyboardArrowLeft';
import PropTypes from 'prop-types';

export default class GranularityModeSelector extends Component {
    constructor(props) {
        super(props);
        this.state = {
            granularityMode: 'high',
            granularityModeValue: 'none',
        };

        this.granularityOptions = {
            low: ['1 Min', '15 Mins', '1 Hour', '1 Day'],
            high: ['1 Day', '7 Days', '1 Month', '3 Months', '6 Months', '1 Year']
        };

        this.onGranularityModeChange = this.onGranularityModeChange.bind(this);
        this.onToggleGranularityMode = this.onToggleGranularityMode.bind(this);
    }

    onGranularityModeChange(e, value) {
        e.preventDefault();
        this.setState({ granularityModeValue: value });
        return this.props.onChange && this.props.onChange(value);
    }

    onToggleGranularityMode(e) {
        e.preventDefault();
        this.setState({
            granularityMode: (this.state.granularityMode === 'low') ? 'high' : 'low'
        });
    }

    render() {
        return (
            <div className="granularity-list">
                <ul>
                    {
                        this.granularityOptions[this.state.granularityMode].map(o => 
                            <li><a href="#" onClick={e => this.onGranularityModeChange(e, o)}>{o}</a></li>
                        )
                    }
                    <li><a href="#" onClick={this.onToggleGranularityMode} className="granularity-switch">
                        {
                            this.state.granularityMode === 'low' ? <HighGranularityMode /> : <LowGranularityMode />
                        }
                    </a></li>
                    <li><a href="#" onClick={e => this.onGranularityModeChange(e, 'custom')}>Custom</a></li>
                </ul>
            </div>
        );
    }
}

GranularityModeSelector.contextTypes = {
    muiTheme: PropTypes.object
};