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

class EmotionsAnalysis extends Widget {
    constructor(props) {
        super(props);

        this.ChartConfig = {
            x: 'Time',
            charts: [{type: 'bar', y: 'Average', fill: '#00e1d6', style: {strokeWidth: 2, markRadius: 5}}],
            legend: true,
            style: {
                legendTitleColor: "#778899",
                legendTextColor: "#778899",
                tickLabelColor: "#778899",
                axisLabelColor: "#778899",
                xAxisTickAngle: -45,
            },
            maxLength: 60,
            gridColor: "#778899",
        };

        this.metadata = {
            names: ['Time', 'Average'],
            types: ['time', 'linear']
        };


        this.state = {
            sentimentData: [],
            metadata: this.metadata,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            btnHeight: 100,
        };

        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this._handleDataReceived = this._handleDataReceived.bind(this);
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        let browserTime = new Date();
        browserTime.setTime(browserTime.valueOf() - 1000 * 60 * 60);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                let query = message.data.configs.providerConfig.configs.config.queryData.query;
                query = query
                    .replace("{{filterCondition}}", browserTime.getTime());
                message.data.configs.providerConfig.configs.config.queryData.query = query;
                super.getWidgetChannelManager().subscribeWidget(this.props.id, this._handleDataReceived, message.data.configs.providerConfig);
            })
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        let {metadata, data} = setData;
        data = data.map((datum) => {
            let date = new Date(datum[0]);
            return [date, datum[1]];
        });
        this.setState({
            metadata: metadata,
            sentimentData: data,
        });
    }

    render() {
        return (

            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section>
                    <RaisedButton
                        label="Back"
                        backgroundColor={'#1d85d3'}
                        hoverColor={'#07619d'}
                        fullWidth={false}
                        style={{position: 'absolute', bottom: 0, right: 0}}
                        onClick={() => {
                            location.href = "/portal/dashboards/twitteranalytics/home";
                        }}/>
                    <h5 style={{position: 'absolute', bottom: 0, paddingRight: 5}}>Emotion rate over last hour</h5>
                    <VizG
                        config={this.ChartConfig}
                        metadata={this.metadata}
                        data={this.state.sentimentData.reverse()}
                        append={false}
                        height={this.state.height - this.state.btnHeight}
                        width={this.state.width}
                    />
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("EmotionsAnalysis", EmotionsAnalysis);
