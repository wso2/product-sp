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
import WidgetChannelManager from './utils/WidgetChannelManager';
import {MuiThemeProvider, darkBaseTheme, getMuiTheme} from 'material-ui/styles';
import RaisedButton from 'material-ui/RaisedButton';

class Emotions extends Widget {
    constructor(props) {
        super(props);

        this.donutChartConfig = {
            charts: [{
                type: 'arc',
                x: 'Rate',
                color: 'Sentiment',
                mode: 'donut',
                colorScale: ['#009933', '#ff884d', '#ff0000']
            }],
            innerRadius: 20,
            animate: true,
            style: {legendTitleColor: "#778899", legendTextColor: "#778899"}
        };

        this.metadata = {
            names: ['Sentiment', 'Rate'],
            types: ['ordinal', 'linear']
        };

        this.state = {
            sentimentData: [],
            metadata: this.metadata,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            btnHeight: 100
        };

        this.providerConfig = {
            type: 'RDBMSBatchDataProvider',
            config: {
                datasourceName: 'Twitter_Analytics',
                query: "select type as Sentiment, count(TweetID) as Rate from sentiment where PARSEDATETIME(timestamp, 'yyyy-mm-dd hh:mm:ss','en') > CURRENT_TIMESTAMP()-86400 group by type",
                tableName: 'sentiment',
                incrementalColumn: 'Sentiment',
                publishingInterval: 60,
            }
        };

        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.channelManager = new WidgetChannelManager();
        this._handleDataReceived = this._handleDataReceived.bind(this);

    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        this.channelManager.subscribeWidget(this.props.id, this._handleDataReceived, this.providerConfig);
    }

    componentWillUnmount() {
        this.channelManager.unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        this.setState({
            metadata: setData.metadata,
            sentimentData: setData.data,
        });
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section>
                    <RaisedButton
                        label="Last Hour"
                        fullWidth={false}
                        backgroundColor={'#1d85d3'}
                        hoverColor={'#1a619d'}
                        style={{position: 'absolute', bottom: 0, right: 0}}
                        onClick={() => {
                            location.href = "/portal/dashboards/twitteranalytics/EmotionAnalysis?type=a";
                        }}/>
                    <VizG
                        config={this.donutChartConfig}
                        metadata={this.metadata}
                        data={this.state.sentimentData}
                        append={false}
                        height={this.state.height}
                        width={this.state.width}
                    />
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("Emotions", Emotions);
