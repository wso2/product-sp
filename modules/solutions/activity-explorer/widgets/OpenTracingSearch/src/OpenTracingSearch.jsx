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

import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import SelectField from 'material-ui/SelectField';
import MenuItem from "material-ui/MenuItem";
import getMuiTheme from "material-ui/styles/getMuiTheme";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import darkBaseTheme from "material-ui/styles/baseThemes/darkBaseTheme";
import TextField from 'material-ui/TextField';
import FlatButton from 'material-ui/FlatButton';
import {Scrollbars} from 'react-custom-scrollbars';
import Axios from 'axios';

const COOKIE = 'DASHBOARD_USER';

class OpenTracingSearch extends Widget {

    constructor(props) {
        super(props);
        this.handleResize = this.handleResize.bind(this);
        this.publishSearchOptions = this.publishSearchOptions.bind(this);
        this.handleComponentNameChange = this.handleComponentNameChange.bind(this);
        this.handleServiceNameChange = this.handleServiceNameChange.bind(this);
        this.startTimeSelected = this.startTimeSelected.bind(this);
        this.endTimeSelected = this.endTimeSelected.bind(this);
        this.minDurationChanged=this.minDurationChanged.bind(this);
        this.maxDurationChanged=this.maxDurationChanged.bind(this);
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.state = {
            selectedComponentName: "All",
            selectedServiceName: "All",
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
        };
        this.props.glContainer.on('resize', this.handleResize);
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        let httpClient = Axios.create({
            baseURL: window.location.origin + window.contextPath,
            timeout: 2000,
            headers: {"Authorization": "Bearer " + OpenTracingSearch.getUserCookie().SDID},
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient
            .get(`/apis/widgets/${this.props.widgetID}`)
            .then((message) => {
                super.getWidgetChannelManager().subscribeWidget(this.props.id, this._handleDataReceived, message.data.configs.providerConfig);
            })
            .catch((error) => {
                console.log("error", error);
            });
        }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    componentWillMount() {
        this.setState({
            components : [],
            services : [],
            selectedComponentName: 'All',
            selectedServiceName: 'All',
            selectedStartTime: 0,
            selectedEndTime: 0,
            selectedMinDuration:0,
            selectedMaxDuration:0
        });
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

    publishSearchOptions(e){
        e.preventDefault();
        var publishEvent = {
            selectedComponentName: this.state.selectedComponentName,
            selectedServiceName: this.state.selectedServiceName,
            selectedStartTime: this.state.selectedStartTime,
            selectedEndTime: this.state.selectedEndTime,
            selectedMinDuration: this.state.selectedMinDuration,
            selectedMaxDuration: this.state.selectedMaxDuration,
            clearData: ['timeline','event', 'list']
        };
        super.publish(publishEvent);
    }

    _handleDataReceived(data){
        let components = [];
        let services = [];
        data.data.map((data) => {
            components.push(data[0]);
            services.push(data[1]);
            }
        );
        components = components.filter((elem, pos, arr) => {
            return arr.indexOf(elem) == pos;
        });

        this.setState({
            components : components,
            services : services
        })
    }

    handleComponentNameChange(e){
        this.setState({
            selectedComponentName : e.target.textContent
        });
    }

    handleServiceNameChange(e){
        this.setState({
            selectedServiceName : e.target.textContent
        });
    }

    startTimeSelected(e){
        this.setState({
            selectedStartTime : e.target.value
        });
    }

    endTimeSelected(e){
        e.preventDefault();
        this.setState({
            selectedEndTime : e.target.value
        });
    }

    minDurationChanged(e){
        this.setState({
            selectedMinDuration : e.target.value
        });
    }

    maxDurationChanged(e){
        this.setState({
            selectedMaxDuration : e.target.value
        });
    }

    render(){
            var adjustedIndex = 0;
            return (
                <div>
                    <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)} >
                        <Scrollbars style={{height: this.state.height}}>
                            <div>
                            <label>Component</label>
                            </div>
                            <div>
                            <SelectField
                                floatingLabelText="Component"
                                value={this.state.selectedComponentName}
                                onChange={this.handleComponentNameChange}>
                                <MenuItem key={0} value={"All"} primaryText='All'/>
                                {
                                    this.state.components.length &&
                                        this.state.components.map((componentName, index) => {
                                        adjustedIndex = index + 1 ;
                                        return <MenuItem key={adjustedIndex}
                                                         value={componentName}
                                                         primaryText={componentName}/>
                                    })
                                }
                            </SelectField>
                        </div>
                            <div>
                            <label>Service</label>
                            </div>
                        <div>
                            <SelectField
                                floatingLabelText="Service"
                                value={this.state.selectedServiceName}
                                onChange={this.handleServiceNameChange}>
                                <MenuItem key={0} value={"All"} primaryText='All'/>
                                {this.state.services.length &&
                                this.state.services.map((service, index) => {
                                    adjustedIndex = index + 1 ;
                                    return <MenuItem key={adjustedIndex}
                                                     value={service}
                                                     primaryText={service}/>
                                })
                                }
                            </SelectField>
                        </div>
                        <div>
                            <label>Start Time</label>
                        </div>
                        <div>
                           <TextField floatingLabelText="Start Time (Unix)" onChange={this.startTimeSelected}/>
                        </div>
                        <div>
                            <label>End Time</label>
                        </div>
                        <div>
                            <TextField floatingLabelText="End Time (Unix)" onChange={this.endTimeSelected}/>
                        </div>
                        <div>
                            <label>Duration</label>
                        </div>
                        <div>
                            <TextField floatingLabelText="Minimum (ms)" onChange={this.minDurationChanged}/>
                            <TextField floatingLabelText="Maximum (ms)" onChange={this.maxDurationChanged}/>
                        </div>
                        <div>
                            <FlatButton
                            label="Search"
                            onClick={this.publishSearchOptions}
                            />
                        </div>
                        </Scrollbars>
                    </MuiThemeProvider>
                </div>
            )
        }
    }

    global.dashboard.registerWidget("OpenTracingSearch",OpenTracingSearch);