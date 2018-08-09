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
import _ from 'lodash';
import './OpenTracingList.css';

const COOKIE = 'DASHBOARD_USER';

class OpenTracingList extends Widget {

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
                    uniquePropertyColumn: "TRACEID",
                    columns: [
                        {
                            "name": "TRACEID",
                            "title": "Trace"
                        },
                        {
                            "name": "COUNT",
                            "title": "Count"
                        },
                        {
                            "name": "ELAPSED_TIME",
                            "title": "Elapsed Time"
                        },
                        {
                            "name": "START_TIME",
                            "title": "Start Time"
                        },
                        {
                            "name": "END_TIME",
                            "title": "End Time"
                        }
                    ]
                },
            ],
            pagination: true,
            append: false
        };
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.setReceivedMsg= this.setReceivedMsg.bind(this);
        this.onClickHandler = this.onClickHandler.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.tableIsUpdated = false;
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        super.subscribe(this.setReceivedMsg);
        super.getWidgetConfiguration(this.props.widgetID)
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
        for (var i = 0; i < data["data"].length; i++) {
            var date = new Date(data["data"][i][3]);
            var date2 = new Date(data["data"][i][4]);
            data["data"][i][3] = date.toLocaleDateString() + " " + date.toLocaleTimeString();
            data["data"][i][4] = date2.toLocaleDateString() + " " + date2.toLocaleTimeString();
        }
        data["metadata"]["types"][3] = "ORDINAL";
        data["metadata"]["types"][4] = "ORDINAL";
        if (!this.tableIsUpdated) {
            this.setState({
                metadata: data.metadata,
                data: data.data,
            });
            this.tableIsUpdated = true;
            window.dispatchEvent(new Event('resize'));
        }
    }

    setReceivedMsg(receivedMsg) {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.widgetID);

        let initial = true;
        let query = "where";
        for (let item in receivedMsg) {
            if (receivedMsg.hasOwnProperty(item)) {
                if (receivedMsg[item] !== "All" && receivedMsg[item] !== "" && receivedMsg[item] !== 0 && item !== "clearData") {
                    if (!initial) {
                        query = query + " and ";
                    } else {
                        initial = false;
                    }
                    if (item === "selectedOperationName") {
                        query = query + " operationName='" + receivedMsg[item] + "'";
                    } else if (item === "selectedServiceName") {
                        query = query + " serviceName='" + receivedMsg[item] + "'";
                    } else if (item === "selectedStartTime") {
                        query = query + " startTime>=" + receivedMsg[item] + "";
                    } else if (item === "selectedEndTime") {
                        query = query + " endTime<=" + receivedMsg[item] + "";
                    } else if (item === "selectedMinDuration") {
                        query = query + " duration>=" + receivedMsg[item] + "";
                    } else if (item === "selectedMaxDuration") {
                        query = query + " duration<=" + receivedMsg[item] + "";
                    }
                }

            }
        }
        let providerConfig = _.cloneDeep(this.state.dataProviderConf);
        if (query === "where") {
            providerConfig.configs.config.queryData.query = providerConfig.configs.config.queryData.query.replace("{{query}}", "");
        } else {
            providerConfig.configs.config.queryData.query = providerConfig.configs.config.queryData.query.replace("{{query}}", query);
        }
        super.getWidgetChannelManager().subscribeWidget(this.props.widgetID, this._handleDataReceived, providerConfig);

        if(receivedMsg.clearData && receivedMsg.clearData.indexOf('list') > -1) {
            this.tableIsUpdated = false;
            this.setState({
                data: []
            });
        }
    }

    render() {
        return (
            <Scrollbars style={{height: this.state.height}}>
                <div
                    style={{
                        width: this.props.glContainer.width,
                        height: this.props.glContainer.height,
                    }}
                    className="list-table-wrapper"
                >
                    <VizG
                        config={this.tableConfig}
                        metadata={this.state.metadata}
                        data={ this.state.data}
                        append={false}
                        height={this.props.glContainer.height}
                        width={this.props.glContainer.width}
                        onClick={this.onClickHandler}
                        theme={this.props.muiTheme.name}
                    />
                </div>
            </Scrollbars>
        );
    }

    onClickHandler(row) {
        // Build the redirecting url with query string data.
        let arr = location.pathname.split('/');
        arr.splice(-1, 1);
        let pathname = arr.join('/');
        let url = location.protocol + '//' + location.hostname + ':' + location.port + pathname + '/timeline?traceid=' +
            encodeURI(row.TRACEID);
        location.replace(url);
        }
}

global.dashboard.registerWidget('OpenTracingList', OpenTracingList);