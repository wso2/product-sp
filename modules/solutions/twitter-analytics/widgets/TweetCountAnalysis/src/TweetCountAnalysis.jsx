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

class TweetCountAnalysis extends Widget {
    constructor(props) {
        super(props);

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

        this.ChartConfig = {
            x: 'Time',
            charts: [
                {
                    type: 'bar',
                    y: 'TweetsCount',
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
        };

        this.metadata = {
            names: ['Time', 'TweetsCount'],
            types: ['time', 'linear']
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
        this.buttonClicked(this.state.dataType)
    }

    componentWillUnmount() {
        this.channelManager.unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        this.setState({
            aggregateData: [],
        });
        this.setState({
            metadata: setData.metadata,
            aggregateData: setData.data,
        });
    }

    buttonClicked(value) {
        let browserTime = new Date();
        if (value === 'day') {
            browserTime.setTime(browserTime.valueOf() - 1000*60*60*24);
            this.setState({
                dataType: value,
                dataHourBtnClicked:false , 
                dataMinuteBtnClicked:true
            });
            this.providerConfiguration("select AGG_TIMESTAMP as time, AGG_COUNT from TweetAggre_HOURS where AGG_TIMESTAMP > "+ browserTime.valueOf() + "", 'TweetAggre_HOURS')

        } else {
            browserTime.setTime(browserTime.valueOf() - 1000*60*60);
            this.setState({
                dataType: value,
                dataHourBtnClicked:true , 
                dataMinuteBtnClicked:false
            });

            this.providerConfiguration("select AGG_TIMESTAMP as time , AGG_COUNT from TweetAggre_MINUTES where AGG_TIMESTAMP > "+ browserTime.valueOf() + "", 'TweetAggre_MINUTES')

        }
    }

    providerConfiguration(query, tableName) {
        this.providerConfig = {
            type: 'RDBMSBatchDataProvider',
            config: {
                datasourceName: 'Twitter_Analytics',
                query: query,
                tableName: tableName,
                incrementalColumn: 'AGG_TIMESTAMP',
                publishingInterval: 60,
                publishingLimit: 60
            }
        };
        this.channelManager.subscribeWidget(this.props.id, this._handleDataReceived, this.providerConfig);
        this.forceUpdate();
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section style={{paddingTop: 50}}>
                    <RaisedButton
                        label="Last Hour"
                        fullWidth={false}
                        backgroundColor={'#1d85d3'}
                        hoverColor={'#000080'}
                        disabled={this.state.dataHourBtnClicked}
                        style={{position: 'absolute', top: 0, left: 0}}
                        onClick={this.buttonClicked.bind(this, 'hour')}/>
                    <RaisedButton
                        label="Last 24hrs"
                        fullWidth={false}
                        backgroundColor={'#1d85d3'}
                        hoverColor={'#000080'}
                        disabled={this.state.dataMinuteBtnClicked}
                        style={{position: 'absolute', top: 0, left: 100}}
                        onClick={this.buttonClicked.bind(this, 'day')}/>
                    <RaisedButton
                        label="Back"
                        fullWidth={false}
                        backgroundColor={'#1d85d3'}
                        hoverColor={'#1a11dd'}
                        style={{position: 'absolute', bottom: 0, right: 0}}
                        onClick={() => {
                            location.href = "/portal/dashboards/twitteranalytics/home";
                        }}/>
                    {this.state.dataType === 'hour' &&
                    <VizG
                        config={this.ChartConfig}
                        metadata={this.metadata}
                        data={this.state.aggregateData.reverse()}
                        append={false}
                        height={this.state.height - this.state.btnGroupHeight}
                        width={this.state.width}
                    />
                    }
                    {this.state.dataType === 'day' &&
                    <VizG
                        config={this.ChartConfig}
                        metadata={this.metadata}
                        data={this.state.aggregateData.reverse()}
                        append={false}
                        height={this.state.height - this.state.btnGroupHeight}
                        width={this.state.width}
                    />
                    }
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("TweetCountAnalysis", TweetCountAnalysis);
