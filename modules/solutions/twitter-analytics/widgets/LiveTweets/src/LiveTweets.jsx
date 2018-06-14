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
import {MuiThemeProvider} from 'material-ui/styles';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import Tweet from 'react-tweet-embed';
import './resources/tweet.css';
import {Scrollbars} from 'react-custom-scrollbars';

class LiveTweets extends Widget {
    constructor(props) {
        super(props);
        this.state = {
            tweetData: [],
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            publishedMsg: '',
            countData: 0
        };

        this.publishMsg = this.publishMsg.bind(this);
        this.getPublishedMsgsOutput = this.getPublishedMsgsOutput.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this._handleDataReceived = this._handleDataReceived.bind(this);
    }

    publishMsg() {
        this.state.countData = this.state.tweetData.length;
        super.publish(this.state.countData);
    }

    componentDidMount() {
        super.publish(this.state.countData);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                super.getWidgetChannelManager().subscribeWidget(this.props.id, this._handleDataReceived, message.data.configs.providerConfig);
            })
    }

    getPublishedMsgsOutput() {
        let storageArray = [];
        this.state.tweetData.map((t) => {
                this.publishMsg();
                storageArray.push(t);
            }
        )
        return storageArray.reverse().map((t) => {
                return (
                    <Tweet id={t[1]} options={{height: "10%", width: '100%', cards: 'hidden'}}/>
                )
            }
        )
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        this.setState({
            tweetData: setData.data,
        });
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section>
                    <Scrollbars style={{height: this.state.height}}>
                        <div className='tweet-stream'>
                            {this.getPublishedMsgsOutput()}
                        </div>
                    </Scrollbars>
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("LiveTweets", LiveTweets);
