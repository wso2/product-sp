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
import WidgetChannelManager from './utils/WidgetChannelManager';
import {Scrollbars} from 'react-custom-scrollbars';

class LiveTweets extends Widget {
    constructor(props) {
        super(props);
        this.state = {
            tweetData: [],
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            publishedMsg: '',
            countData: 0,
            storageArray: []
        };

        this.providerConfig = {
            type: 'RDBMSStreamingDataProvider',
            config: {
                datasourceName: 'Twitter_Analytics',
                query: "select id,TweetID from sentiment",
                tableName: 'sentiment',
                incrementalColumn: 'id',
                publishingInterval: 5,
                publishingLimit: 5,
                purgingInterval: 6,
                purgingLimit: 6,
                isPurgingEnable: false,
            }
        };

        this.publishMsg = this.publishMsg.bind(this);
        this.getPublishedMsgsOutput = this.getPublishedMsgsOutput.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.channelManager = new WidgetChannelManager();
    }

    publishMsg() {
        this.state.countData = this.state.tweetData.length;
        super.publish(this.state.countData);
    }

    componentDidMount() {
        super.publish(this.state.countData);
        this.channelManager.subscribeWidget(this.props.id, this._handleDataReceived, this.providerConfig);
    }

    getPublishedMsgsOutput() {
        this.state.tweetData.map((t) => {
                this.publishMsg();
                this.state.storageArray.push(t);

            }
        )
        return this.state.storageArray.reverse().map((t) => {
                return <Tweet id={t[1]} options={{height: "10%", width: '100%', cards: 'hidden'}}/>
            }
        )
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentWillUnmount() {
        this.channelManager.unsubscribeWidget(this.props.id);
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
