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
import { MuiThemeProvider, getMuiTheme } from 'material-ui/styles';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import { IconButton } from 'material-ui';
import RefreshIcon from 'material-ui/svg-icons/navigation/refresh';
import Tweet from 'react-tweet-embed';
import { Scrollbars } from 'react-custom-scrollbars';
import './resources/tweet.css';

const MAX_UNREAD_TWEET_COUNT = 100;

class LiveTweets extends Widget {
    constructor(props) {
        super(props);
        this.state = {
            tweetData: [],
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            unreadTweets: [],
            lastTweetCount: 0
        };

        this.props.glContainer.on('resize', () => this.setState({
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
        }));

        this.styles = {
            unreadTweetsLabel: {
                display: 'inline-block',
                position: 'absolute',
                top: 14,
                right: 36,
                borderRadius: 2,
                padding: '3px 5px',
                fontSize: 12,
                backgroundColor: props.muiTheme.palette.primary2Color,
                marginRight: 5
            }
        };

        this.publishTweetCount = this.publishTweetCount.bind(this);
        this.renderTweets = this.renderTweets.bind(this);
        this.showUnreadMessages = this.showUnreadMessages.bind(this);
    }

    componentDidMount() {
        super.publish(this.tweetCount);

        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                super.getWidgetChannelManager().subscribeWidget(this.props.id,
                    (data) => {
                        // publish latest Tweet counts
                        for (let i = 0; i < data.data.length; i++) {
                            this.publishTweetCount();
                        }

                        if (this.state.tweetData.length === 0) {
                            // No Tweets are showing. Hence add the received Tweets to tweetData and re-render the page.
                            this.setState({
                                tweetData: data.data,
                                lastTweetCount: data.data.length,
                                unreadTweets: []
                            });
                        } else {
                            // Tweets are showing. Hence append the received Tweets to unreadTweets list.
                            data.data.map(t => this.state.unreadTweets.push(t));
                            // Keep only the latest MAX_UNREAD_TWEET_COUNT Tweets in the unreadTweets queue.
                            let numberOfRemovableTweets = this.state.unreadTweets.length - MAX_UNREAD_TWEET_COUNT;
                            this.state.unreadTweets.splice(0, numberOfRemovableTweets < 0 ? 0 : numberOfRemovableTweets);
                            this.setState({
                                lastTweetCount: data.data.length,
                                unreadTweets: this.state.unreadTweets
                            });
                        }
                    },
                    message.data.configs.providerConfig);
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    publishTweetCount() {
        this.tweetCount = this.state.tweetData.length;
        super.publish(this.tweetCount);
    }

    renderTweets() {
        return this.state.tweetData.slice().reverse().map(t => {
            return <Tweet id={t[1]} options={{ height: "10%", width: '100%', cards: 'hidden' }} />
        });
    }

    showUnreadMessages() {
        this.setState({
            tweetData: this.state.unreadTweets.slice(),
            unreadTweets: []
        });
    }

    render() {
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section>
                    <div style={{ height: 36, textAlign: 'right' }}>
                        <span style={this.styles.unreadTweetsLabel}>{this.state.unreadTweets.length}</span>
                        <IconButton tooltip="Refresh Tweets" onClick={this.showUnreadMessages}>
                            <RefreshIcon />
                        </IconButton>
                    </div>
                    <Scrollbars style={{ height: this.state.height }}>
                        <div className='tweet-stream'>
                            {this.renderTweets()}
                        </div>
                    </Scrollbars>
                </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("LiveTweets", LiveTweets);
