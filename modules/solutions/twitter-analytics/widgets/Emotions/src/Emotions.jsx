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
            animate: false,
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

        this.styles = {
            historicalDataButton: {
                position: 'absolute',
                right: 10,
                bottom: 10
            }
        };

        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this._handleDataReceived = this._handleDataReceived.bind(this);

    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                super.getWidgetChannelManager().subscribeWidget(this.props.id, this._handleDataReceived, message.data.configs.providerConfig);
            })
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
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
                        label="Historical Data"
                        fullWidth={false}
                        backgroundColor={'#1c3b4a'}
                        hoverColor={'#1a619d'}
                        style={this.styles.historicalDataButton}
                        onClick={() => {
                            location.href = "/portal/dashboards/twitteranalytics/EmotionAnalysis";
                        }}/>
                    <VizG
                        config={this.donutChartConfig}
                        metadata={this.metadata}
                        data={this.state.sentimentData}
                        append={false}
                        height={this.state.height}
                        width={this.state.width}
                        theme={this.props.muiTheme.name}
                    />
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("Emotions", Emotions);
