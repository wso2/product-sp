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
import Widget from '@wso2-dashboards/widget';
import FlatButton from 'material-ui/FlatButton';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import VizG from 'react-vizgrammar';
import WidgetChannelManager from './utils/WidgetChannelManager';

class TweetCounter extends Widget {
    constructor(props) {
        super(props);

        this.time = new Date().toLocaleString();
        this.state = {
            receivedMsg: 0,
            metadata: this.metadata,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            btnHeight: 150,
            hashTag: [],
        };
        this.set = [];

        this.metadata = {
            names: ['count'],
            types: ['linear']
        };

        this.providerConfig = {
            type: 'RDBMSBatchDataProvider',
            config: {
                datasourceName: 'Twitter_Analytics',
                query: "select trackwords from hashTag",
                tableName: 'hashTag',
                incrementalColumn: 'id',
                publishingInterval: 20
            }
        };

        this.clearMsgs = this.clearMsgs.bind(this);
        this.setReceivedMsg = this.setReceivedMsg.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.channelManager = new WidgetChannelManager();
        this._handleDataReceived = this._handleDataReceived.bind(this);
    }

    componentDidMount() {
        this.channelManager.subscribeWidget(this.props.id, this._handleDataReceived, this.providerConfig);
    }

    componentWillUnmount() {
        this.channelManager.unsubscribeWidget(this.props.id);
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    _handleDataReceived(setData) {
        this.setState({
            hashTag: setData.data,
        });
    }

    componentWillMount() {
        super.subscribe(this.setReceivedMsg);
    }

    setReceivedMsg(receivedMsg) {
        this.set.push({value: receivedMsg});
        this.setState({receivedMsg});
    }

    generateOutput() {
        const output = [];
        this.set.forEach(d => {
            output.push(d.value);
        });
        console.info(this.set, output.length);
        return [[output.length]];
    }

    clearMsgs() {
        this.setState({receivedMsg: 0});
        this.set = [];
    }

    configSetUp(hashTag) {
        let Config = {
            x: 'count',
            title: hashTag,
            charts: [
                {
                    type: 'number'
                }
            ]
        };
        return Config
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section>
                    <FlatButton
                        label="Historical Data"
                        fullWidth={false}
                        backgroundColor={'#1d85d3'}
                        hoverColor={'#1a619d'}
                        style={{position: 'absolute', bottom: 0, right: 0}}
                        onClick={() => {
                            location.href = "/portal/dashboards/twitteranalytics/TweetAnalysis";
                        }}/>
                    <h5 style={{position: 'absolute', bottom: 10, paddingRight: 5}}>From  {this.time}</h5>
                    <VizG
                        config={this.configSetUp(this.state.hashTag[0])}
                        metadata={this.metadata}
                        data={this.generateOutput()}
                        append={false}
                        height={this.state.height}
                        width={this.state.width}
                    />
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("TweetCounter", TweetCounter);
