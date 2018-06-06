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

import React from 'react';
import Widget from '@wso2-dashboards/widget';
import VizG from 'react-vizgrammar';
import _ from 'lodash';
import WidgetChannelManager from './utils/WidgetChannelManager';
import dataProviderConf from './resources/dataProviderConf.json';

// Initial Metadata
let metadata = {
    names: ['AGG_TIMESTAMP', 'serverName', 'avgRespTime'],
    types: ['TIME', 'ORDINAL', 'LINEAR']
};

//Initial Chart config
let chartConfigTemplate = {
    x: 'AGG_TIMESTAMP',
    charts: [
        {
            type: 'line',
            y: 'avgRespTime',
            fill: '#00e1d6',
            color: 'serverName',
            style: {strokeWidth: 2, markRadius: 3}
        }
    ],
    legend: true,
    animate: true,
    style: {
        legendTitleColor: "#5d6e77",
        legendTextColor: "#5d6e77",
        tickLabelColor: "#5d6e77",
        axisLabelColor: "#5d6e77"
    },
    gridColor: "#5d6e77",
    brush: true,
    xAxisLabel: 'Time',
    yAxisLabel: 'Average Latency',
    append: false
};

/**
 * HTTPAnalyticsLatencyOverTime Widget which plots a line graph for latency overtime
 */
class HTTPAnalyticsLatencyOverTime extends Widget {

    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            chartConfig: chartConfigTemplate,
            data: [],
            metadata: metadata,
        };

        this.channelManager = new WidgetChannelManager();
        this.handleDataReceived = this.handleDataReceived.bind(this);
        this.setReceivedMsg = this.setReceivedMsg.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);
        this.props.glContainer.on('resize', () => {
            this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
        });
    }

    componentDidMount() {
        super.subscribe(this.setReceivedMsg);
    }

    componentWillUnmount() {
        this.channelManager.unsubscribeWidget(this.props.id);
    }

    /**
     * Set the state of the widget after metadata and data is received from SiddhiAppProvider
     * @param message
     */
    handleDataReceived(message) {
        let configClone = _.cloneDeep(chartConfigTemplate);
        configClone.charts[0].color = message.metadata.names[1];
        message.metadata.types[0] = "TIME";

        this.setState({
            chartConfig: configClone,
            metadata: message.metadata,
            data: message.data
        });
        window.dispatchEvent(new Event('resize'));
    }

    /**
     * Set state based on received user input from Filter widget and Date Range Picker widget
     * @param receivedMsg
     */
    setReceivedMsg(receivedMsg) {
        if (typeof receivedMsg.perspective === "number") {
            this.setState({
                perspective: receivedMsg.perspective,
                selectedServerValues: receivedMsg.selectedServerValues,
                selectedServiceValues: receivedMsg.selectedServiceValues,
                selectedSingleServiceValue: receivedMsg.selectedSingleServiceValue
            }, this.assembleQuery);
        } else {
            this.setState({
                per: receivedMsg.granularity,
                fromDate: receivedMsg.from,
                toDate: receivedMsg.to
            }, this.assembleQuery);
        }
    }

    /**
     * Query is initialised after the user input is received
     */
    assembleQuery() {
        if (typeof this.state.perspective === "number" && typeof this.state.per === "string") {
            this.channelManager.unsubscribeWidget(this.props.id);
            let filterBy = "";
            let filterCondition = "on (";
            let groupBy = "server";
            switch (this.state.perspective) {
                case 0 :
                    groupBy = "serverName";
                    if (!this.state.selectedServiceValues.some(value => value.value === 'All') ||
                        this.state.selectedServiceValues.length !== 1) {
                        this.state.selectedServiceValues.map((value) => {
                            filterCondition += "serviceName=='" + value.value + "' or "
                        })
                    }
                    filterBy = "serviceName,";
                    break;
                case 1:
                    groupBy = "serviceName";
                    if (!this.state.selectedServerValues.some(value => value.value === 'All') ||
                        this.state.selectedServerValues.length !== 1) {
                        this.state.selectedServerValues.map((value) => {
                            filterCondition += "serverName=='" + value.value + "' or "
                        })
                    }
                    filterBy = "serverName,";
                    break;
                case 2:
                    if (this.state.selectedSingleServiceValue.value !== 'All') {
                        filterCondition += "serviceName=='" + this.state.selectedSingleServiceValue.value + "' or "
                    }
                    filterBy = "serviceName,";
                    groupBy = "serviceMethod";
                    break;
                default:
                    groupBy = "serverName";
                    break;
            }

            if (filterCondition.endsWith("on (")) {
                filterCondition = "";
                filterBy = "";
            } else {
                filterCondition = filterCondition.slice(0, -3) + ")";
            }

            let dataProviderConfigs = _.cloneDeep(dataProviderConf);
            let query = dataProviderConfigs.config.queryData.query;
            query = query
                .replace("{{filterCondition}}", filterCondition)
                .replace("{{groupBy}}", groupBy)
                .replace("{{groupBy}}", groupBy)
                .replace("{{filterBy}}", filterBy)
                .replace("{{per}}", this.state.per)
                .replace("{{from}}", this.state.fromDate)
                .replace("{{to}}", this.state.toDate);
            dataProviderConfigs.config.queryData.query = query;

            this.setState({
                data: []
            }, this.channelManager.subscribeWidget(this.props.id, this.handleDataReceived, dataProviderConfigs));
        }
    }

    render() {
        return (
            <div
                style={{
                    marginTop: "5px",
                    width: this.state.width,
                    height: this.state.height
                }}
            >
                <VizG
                    config={this.state.chartConfig}
                    metadata={this.state.metadata}
                    data={this.state.data}
                    width={this.state.width}
                    height={this.state.height}
                />
            </div>
        );
    }
}

global.dashboard.registerWidget("HTTPAnalyticsLatencyOverTime", HTTPAnalyticsLatencyOverTime);
