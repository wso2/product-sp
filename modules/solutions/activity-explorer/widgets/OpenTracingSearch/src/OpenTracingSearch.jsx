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

import React, { Component } from "react";
import Widget from "@wso2-dashboards/widget";
import { MenuItem, RaisedButton, SelectField, TextField } from 'material-ui';
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import { Scrollbars } from 'react-custom-scrollbars';
import Axios from 'axios';
import './OpenTracingSearch.css';
import DateRangePicker from './DateRangePicker/DateRangePicker';

const COOKIE = 'DASHBOARD_USER';

class OpenTracingSearch extends Widget {
    constructor(props) {
        super(props);
        this.publishSearchOptions = this.publishSearchOptions.bind(this);
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.state = {
            selectedComponentName: "All",
            selectedServiceName: "All",
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
        };

        this.props.glContainer.on('resize', () => this.setState({
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
        }));
    }

    static getUserCookie() {
        const arr = document.cookie.split(';');
        for (let i = 0; i < arr.length; i++) {
            let c = arr[i];
            while (c.charAt(0) === ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(COOKIE) === 0) {
                return JSON.parse(c.substring(COOKIE.length + 1, c.length));
            }
        }
        return null;
    }

    componentWillMount() {
        this.setState({
            components: [],
            services: [],
            selectedComponentName: 'All',
            selectedServiceName: 'All',
            selectedStartTime: 0,
            selectedEndTime: 0,
            selectedMinDuration: 0,
            selectedMaxDuration: 0
        });
    }

    componentDidMount() {
        let httpClient = Axios.create({
            baseURL: window.location.origin + window.contextPath,
            timeout: 2000,
            headers: { "Authorization": "Bearer " + OpenTracingSearch.getUserCookie().SDID },
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient
            .get(`/apis/widgets/${this.props.widgetID}`)
            .then((message) => {
                super.getWidgetChannelManager().subscribeWidget(
                    this.props.id, this._handleDataReceived, message.data.configs.providerConfig);
            })
            .catch((error) => {
                console.log("error", error);
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    publishSearchOptions(e) {
        e.preventDefault();
        let publishEvent = {
            selectedComponentName: this.state.selectedComponentName,
            selectedServiceName: this.state.selectedServiceName,
            selectedStartTime: this.state.selectedStartTime,
            selectedEndTime: this.state.selectedEndTime,
            selectedMinDuration: this.state.selectedMinDuration,
            selectedMaxDuration: this.state.selectedMaxDuration,
            clearData: ['timeline', 'event', 'list']
        };
        super.publish(publishEvent);
    }

    _handleDataReceived(data) {
        let components = [];
        let services = [];
        data.data.map((data) => {
            components.push(data[0]);
            services.push(data[1]);
        });
        components = components.filter((elem, pos, arr) => {
            return arr.indexOf(elem) === pos;
        });

        this.setState({
            components: components,
            services: services
        })
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={this.props.muiTheme}>
                <Scrollbars style={{ height: this.state.height }}>
                    <div className="activity-search-form">
                        <div className="column-100">
                            <SelectField
                                fullWidth
                                text="Service"
                                floatingLabelText="Service"
                                value={this.state.selectedComponentName}
                                onChange={e => this.setState({selectedComponentName: e.target.textContent})}
                            >
                                <MenuItem key={0} value="All" primaryText="All" />
                                {
                                    this.state.components.length && this.state.components.map((component, index) => {
                                        return <MenuItem key={index + 1} value={component} primaryText={component} />
                                    })
                                }
                            </SelectField>
                        </div>
                        <div className="column-100">
                            <SelectField
                                fullWidth
                                value={this.state.selectedServiceName}
                                onChange={e => this.setState({selectedServiceName: e.target.textContent})}
                                className="fix-top-margin"
                            >
                                <MenuItem key={0} value="All" primaryText="All" />
                                {
                                    this.state.services.length && this.state.services.map((service, index) => {
                                        return <MenuItem key={index + 1} value={service} primaryText={service} />
                                    })
                                }
                            </SelectField>
                        </div>
                        <div className="column-100">
                            <DateRangePicker onChange={(from, to) => {
                                this.setState({
                                    selectedStartTime: new Date(from).getTime(),
                                    selectedEndTime: new Date(to).getTime()
                                })
                            }}/>
                        </div>
                        <div className="clearfix">
                            <div className="column-50">
                                <TextField
                                    fullWidth
                                    floatingLabelText="Minimum Duration (ms)"
                                    onChange={e => this.setState({selectedMinDuration: e.target.value})}
                                />
                            </div>
                            <div className="column-50">
                                <TextField
                                    fullWidth
                                    floatingLabelText="Maximum Duration (ms)"
                                    onChange={e => this.setState({selectedMaxDuration: e.target.value})}
                                />
                            </div>
                        </div>
                        <div className="clearfix action-bar">
                            <RaisedButton primary label="Search" onClick={this.publishSearchOptions} />
                        </div>
                    </div>
                </Scrollbars>
            </MuiThemeProvider>
        )
    }
}

global.dashboard.registerWidget("OpenTracingSearch", OpenTracingSearch);
