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

import React, {Component} from 'react';
import VizG from 'react-vizgrammar';
import Widget from '@wso2-dashboards/widget';
import {MuiThemeProvider, darkBaseTheme, getMuiTheme} from 'material-ui/styles';
import RaisedButton from 'material-ui/RaisedButton';

class TweetCountAnalysis extends Widget {
    constructor(props) {
        super(props);

        this.ChartConfig = {
            x: 'AGG_TIMESTAMP',
            charts: [
                {
                    type: 'bar',
                    y: 'tweetCount',
                    fill: '#00BFFF',
                }
            ],
            style: {
                tickLabelColor: "#778899",
                axisLabelColor: "#778899",
                xAxisTickAngle: -45,
            },
            maxLength: 60,
            gridColor: "#778899",
            yAxisLabel: 'Tweet Count',
            xAxisLabel: 'Time',
            legend: false,
            append: false
        };

        this.metadata = {
            names: ['AGG_TIMESTAMP', 'tweetCount'],
            types: ['time', 'linear']
        };

        this.state = {
            aggregateData: [],
            metadata: this.metadata,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            btnGroupHeight: 100,
            dataType: 'hour',
            dataHourBtnClicked: false,
            dataMinuteBtnClicked: false,
        };

        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.setReceivedMsg = this.setReceivedMsg.bind(this);
        this.assembleQuery = this.assembleQuery.bind(this);
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        super.subscribe(this.setReceivedMsg);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                this.setState({
                    providerConfig: message.data.configs.providerConfig
                });
            })
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        let {metadata, data} = setData;
        metadata.types[0] = 'TIME';
        this.setState({
            metadata: metadata,
            aggregateData: data,
        });
    }

    setReceivedMsg(receivedMsg) {
        this.setState({
            per: receivedMsg.granularity,
            fromDate: receivedMsg.from,
            toDate: receivedMsg.to
        }, this.assembleQuery);
    }

    /**
     * Query is initialised after the user input is received
     */
    assembleQuery() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
        let dataProviderConfigs = _.cloneDeep(this.state.providerConfig);
        let query = dataProviderConfigs.configs.config.queryData.query;
        query = query
            .replace("{{per}}", this.state.per)
            .replace("{{from}}", this.state.fromDate)
            .replace("{{to}}", this.state.toDate);
        dataProviderConfigs.configs.config.queryData.query = query;
        this.setState({
            sentimentData: []
        }, super.getWidgetChannelManager()
            .subscribeWidget(this.props.id, this._handleDataReceived, dataProviderConfigs));
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section style={{paddingTop: 50}}>
                    <RaisedButton
                        label="Back"
                        fullWidth={false}
                        primary={true}
                        style={{position: 'absolute', bottom: 10, right: 10}}
                        onClick={() => {
                            location.href = "/portal/dashboards/twitteranalytics/home";
                        }}/>
                    <VizG
                        config={this.ChartConfig}
                        metadata={this.state.metadata}
                        data={this.state.aggregateData}
                        height={this.state.height - this.state.btnGroupHeight}
                        width={this.state.width}
                        theme={this.props.muiTheme.name}
                    />
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("TweetCountAnalysis", TweetCountAnalysis);
