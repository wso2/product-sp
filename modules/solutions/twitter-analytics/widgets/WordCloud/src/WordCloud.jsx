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
import TagCloud from 'react-tag-cloud';
import randomColor from 'randomcolor';
import './resources/wordCloud.css';
import WidgetChannelManager from './utils/WidgetChannelManager';
import {MuiThemeProvider, darkBaseTheme, getMuiTheme} from 'material-ui/styles';
import RaisedButton from 'material-ui/RaisedButton';

class WordCloud extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            dataText: [],
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            wordsType: 'text',
            btnGroupHeight: 150,
            textBtnClicked: false,
            hashtagBtnClicked: false,
            mentionBtnClicked: false,
        };
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.channelManager = new WidgetChannelManager();
        this._handleDataReceived = this._handleDataReceived.bind(this);
    }

    changeColor() {
        this.setState({buttonColor: !this.state.buttonColor})
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        this.buttonClicked(this.state.wordsType)
    }

    componentWillUnmount() {
        this.channelManager.unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        this.setState({
            dataText: [],
        });
        this.setState({
            dataText: setData.data,
        });
    }

    buttonClicked(value) {

        let browserTime = new Date();
        browserTime.setTime(browserTime.valueOf() - 1000*60*60);
        if (value === 'mention') {
            this.setState({
                wordsType: value,
                mentionBtnClicked: true,
                hashtagBtnClicked: false,
                textBtnClicked: false,
            });

            this.providerConfiguration('select mention as cloudWords, count(id) as Count from mentionCloud where timestamp> '+ browserTime.getTime() +' group by mention', 'mentionCloud')

        } else if (value === 'hashtag') {

            this.setState({
                wordsType: value,
                mentionBtnClicked: false,
                hashtagBtnClicked: true,
                textBtnClicked: false,
            });

            this.providerConfiguration('select hashtag as cloudWords, count(id) as Count from hashtagCloud where timestamp > '+ browserTime.getTime() +' group by hashtag', 'hashtagCloud')

        } else {
            this.setState({
                wordsType: value,
                mentionBtnClicked: false,
                hashtagBtnClicked: false,
                textBtnClicked: true,
            });

            this.providerConfiguration('select words as cloudWords, count(id) as Count from textCloud where timestamp > '+ browserTime.getTime() +' group by words', 'textCloud')
        }
    }

    providerConfiguration(query, tableName) {
        this.providerConfig = {
            type: 'RDBMSBatchDataProvider',
            config: {
                datasourceName: 'Twitter_Analytics',
                query: query,
                tableName: tableName,
                incrementalColumn: 'Count',
                publishingInterval: 20,
                publishingLimit: 30
            }
        };
        this.channelManager.subscribeWidget(this.props.id, this._handleDataReceived, this.providerConfig);
        this.forceUpdate();
    }

    render() {
        let bgColor = this.state.buttonColor ? "1d85d3" : "#1d85d3"
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                <section style={{paddingTop: 50}}>
                    <div className='app-outer'>
                        <div>
                            <RaisedButton
                                label="Text Cloud"
                                fullWidth={false}
                                backgroundColor={bgColor}
                                hoverColor={'#000080'}
                                disabled={this.state.textBtnClicked}
                                style={{position: 'absolute', top: 0, left: 0}}
                                onClick={this.buttonClicked.bind(this, 'text')}/>
                            <RaisedButton
                                label="Hash tag Cloud"
                                fullWidth={false}
                                backgroundColor={bgColor}
                                hoverColor={'#000080'}
                                disabled={this.state.hashtagBtnClicked}
                                style={{position: 'absolute', top: 0, left: 120}}
                                onClick={this.buttonClicked.bind(this, 'hashtag')}/>
                            <RaisedButton
                                label="Mention Cloud"
                                fullWidth={false}
                                backgroundColor={bgColor}
                                hoverColor={'#000080'}
                                disabled={this.state.mentionBtnClicked}
                                style={{position: 'absolute', top: 0, left: 270}}
                                onClick={this.buttonClicked.bind(this, 'mention')}/>
                        </div>
                        <div className='app-inner' height={this.state.height - this.state.btnGroupHeight}
                             width={this.state.width}>
                            {
                                this.state.wordsType === 'text' &&
                                <TagCloud
                                    className='tag-cloud'
                                    style={{
                                        fontFamily: 'sans-serif',
                                        fontSize: () => Math.round(Math.random() * 1.8 * (this.state.width) / 40),
                                        color: () => randomColor({
                                            hue: 'blue'
                                        }),
                                        padding: 12,
                                    }}>
                                    {
                                        this.state.dataText.map((words, index) => {
                                            if (words[1] > 80) {
                                                return <div rotate={-45} style={{fontSize:words[1]* 0.4}}>{words[0]}</div>
                                            }
                                            else {
                                                return <div rotate={45} style={{fontSize: words[0.5]}}>{words[0]}</div>
                                            }
                                        })
                                    }
                        </TagCloud>
                        }
                        {
                            this.state.wordsType === 'hashtag' &&
                            <TagCloud
                                className='tag-cloud'
                                style={{
                                    fontFamily: 'sans-serif',
                                    fontSize: () => Math.round(Math.random() * 1.8 * (this.state.width) / 40),
                                    color: () => randomColor(),
                                    padding: 12,
                                }}>
                                {
                                    this.state.dataText.map((words) => {
                                        console.log('hashtag' + this.state.dataText)
                                        if (words[0].length > 6) {
                                            return <div rotate={Math.random()}>{words[0]}</div>
                                        }
                                        else {
                                            return <div rotate={90}>{words[0]}</div>
                                        }
                                    })
                                }
                            </TagCloud>
                        }
                        {
                            this.state.wordsType === 'mention' &&
                            <TagCloud
                                className='tag-cloud'
                                style={{
                                    fontFamily: 'sans-serif',
                                    fontSize: () => Math.round(Math.random() * 1.8 * (this.state.width) / 40),
                                    color: () => randomColor(),
                                    padding: 12,
                                }}>
                                {
                                    this.state.dataText.map((words) => {
                                        return <div rotate={Math.random()}>{words[0]}</div>
                                    })
                                }
                            </TagCloud>
                        }
                    </div>
                </div>
            </section>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("WordCloud", WordCloud);
