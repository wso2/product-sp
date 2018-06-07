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
import VizG from 'react-vizgrammar';
import {Scrollbars} from 'react-custom-scrollbars';
import Axios from 'axios';
import _ from 'lodash';
import './OpenTracingEvent.css';

const COOKIE = 'DASHBOARD_USER';

class OpenTracingEvent extends Widget {

    constructor(props) {
        super(props);

        this.state = {
            data: [],
            metadata: null,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
        };

        this.tableConfig = {
            charts: [
                {
                    type: 'table',
                    columns: [
                        {
                            "name": "attributeName",
                            "title": "Attribute Name"
                        },
                        {
                            "name": "attributeValue",
                            "title": "Attribute Value"
                        }
                    ]
                },
            ],
            append: false
        };
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.setReceivedMsg= this.setReceivedMsg.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentWillMount() {
        super.subscribe(this.setReceivedMsg);
    }

    componentDidMount() {
        let httpClient = Axios.create({
            baseURL: window.location.origin + window.contextPath,
            timeout: 2000,
            headers: {"Authorization": "Bearer " + OpenTracingEvent.getUserCookie().SDID},
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient
            .get(`/apis/widgets/${this.props.widgetID}`)
            .then((message) => {
                this.setState({
                    dataProviderConf :  message.data.configs.providerConfig
                });
            })
            .catch((error) => {
                console.log("error", error);
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

    _handleDataReceived(data) {
        var metadata_object = {};
        metadata_object.names = ["attributeName", "attributeValue"];
        metadata_object.types = ["ORDINAL", "ORDINAL"]
        if (data.data[0]) {
            var data = data.data[0];
            var data_object = [];
            if (data) {
                var dataString = data[0].replace(/'/g, '"');
                var dataArray = JSON.parse(dataString);
                for (var i = 0; i < dataArray.length; i++) {
                    var data_element = [];
                    data_element.push(Object.keys(dataArray[i])[0]);
                    data_element.push(dataArray[i][Object.keys(dataArray[i])[0]]);
                    data_object.push(data_element);
                }
            }
        }

        this.setState({
            metadata: metadata_object,
            data: data_object,
        });
        window.dispatchEvent(new Event('resize'));
    }

    setReceivedMsg(receivedMsg) {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.widgetID);
        if(receivedMsg.data && receivedMsg.data.span) {
            let providerConfig = _.cloneDeep(this.state.dataProviderConf);
            providerConfig.configs.config.queryData.query =
                providerConfig.configs.config.queryData.query.replace("${receivedMsg.data.span}",
                    receivedMsg.data.span);
            super.getWidgetChannelManager().subscribeWidget(this.props.widgetID, this._handleDataReceived,
                providerConfig);
        }

        if(receivedMsg.clearData && receivedMsg.clearData.indexOf('event') > -1) {
            this.setState({ data: [] });
        }

    }

    render() {
        if (this.state.data[0]) {
            return (
                <Scrollbars style={{height: this.state.height}}>
                    <div
                        style={{
                            width: this.props.glContainer.width,
                            height: this.props.glContainer.height,
                        }}
                        className="event-table-wrapper"
                    >
                        <VizG
                            config={this.tableConfig}
                            metadata={this.state.metadata}
                            data={ this.state.data}
                            append={false}
                            height={this.props.glContainer.height}
                            width={this.props.glContainer.width}
                            theme={this.props.muiTheme.name}
                        />
                    </div>
                </Scrollbars>
            );
        } else {
            return null;
        }
    }
}

global.dashboard.registerWidget('OpenTracingEvent', OpenTracingEvent);