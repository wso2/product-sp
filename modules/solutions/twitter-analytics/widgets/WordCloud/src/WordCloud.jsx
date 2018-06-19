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
import TagCloud from 'react-tag-cloud';
import randomColor from 'randomcolor';
import './resources/wordCloud.css';
import {MuiThemeProvider, getMuiTheme} from 'material-ui/styles';
import {Tabs, Tab} from 'material-ui/Tabs';

class WordCloud extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            dataText: [],
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            wordsType: 'text'
        };

        this.styles = {
            tagCloud: {
                fontFamily: 'Roboto, sans-serif',
                fontSize: () => Math.round(Math.random() * 1.8 * (this.state.width) / 40),
                color: () => randomColor({hue: 'blue'}),
                position: 'absolute',
                top: 0,
                right: 0,
                bottom: 0,
                left: 0
            }
        };

        this.props.glContainer.on('resize', () => this.setState({
            width: this.props.glContainer.width, 
            height: this.props.glContainer.height
        }));
    }

    componentDidMount() {
        this.tabChanged(this.state.wordsType);
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    tabChanged(value) {
        let browserTime = new Date();
        browserTime.setTime(browserTime.valueOf() - 1000 * 60 * 60);

        if (value === 'mention') {
            this.providerConfiguration('select mention as cloudWords, count(id) as Count from mentionCloud where timestamp> ' + browserTime.getTime() + ' group by mention', 'mentionCloud')
        } else if (value === 'hashtag') {
            this.providerConfiguration('select hashtag as cloudWords, count(id) as Count from hashtagCloud where timestamp > ' + browserTime.getTime() + ' group by hashtag', 'hashtagCloud')
        } else {
            this.providerConfiguration('select words as cloudWords, count(id) as Count from textCloud where timestamp > ' + browserTime.getTime() + ' group by words', 'textCloud')
        }

        this.setState({
            wordsType: value
        });
    }

    providerConfiguration(queryData, tableName) {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                let query = message.data.configs.providerConfig.configs.config.queryData.query;
                query = query.replace("{{query}}", queryData);
                message.data.configs.providerConfig.configs.config.queryData.query = query;

                let table = message.data.configs.providerConfig.configs.config.tableName;
                table = table.replace("{{tableName}}", tableName);
                message.data.configs.providerConfig.configs.tableName = table;
                super.getWidgetChannelManager().subscribeWidget(this.props.id, d => this.setState({dataText: d.data}), 
                    message.data.configs.providerConfig);
            })
        this.forceUpdate();
    }

    render() {
        const { muiTheme } = this.props;
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(muiTheme)}>
                <div className="word-cloud-container">
                    <Tabs
                        value={this.state.wordsType}
                        onChange={e => this.tabChanged(e)}
                        inkBarStyle={{backgroundColor: muiTheme.palette.primary1Color}}
                        tabItemContainerStyle={{backgroundColor: '#1d3b4a'}}
                        style={{height: 'calc(100% - 5px)'}}
                        contentContainerClassName="word-cloud-tab-container"
                    >
                        <Tab label="Text Cloud" value="text">
                            <TagCloud className='tag-cloud' style={this.styles.tagCloud}>
                                {
                                    this.state.dataText.map((words, index) => {
                                        if (words[1] > 80) {
                                            return <div rotate={-45}
                                                        style={{fontSize: words[1] * 0.4}}>{words[0]}</div>
                                        } else {
                                            return <div rotate={45} style={{fontSize: words[0.5]}}>{words[0]}</div>
                                        }
                                    })
                                }
                            </TagCloud>
                        </Tab>
                        <Tab label="Hashtag Cloud" value="hashtag">
                            <TagCloud className='tag-cloud' style={this.styles.tagCloud}>
                                {
                                    this.state.dataText.map((words) => {
                                        if (words[0].length > 6) {
                                            return <div rotate={Math.random()}>{words[0]}</div>
                                        } else {
                                            return <div rotate={90}>{words[0]}</div>
                                        }
                                    })
                                }
                            </TagCloud>
                        </Tab>
                        <Tab label="Mention Cloud" value="mention">
                            <TagCloud className='tag-cloud' style={this.styles.tagCloud}>
                                {
                                    this.state.dataText.map((words) => {
                                        return <div rotate={Math.random()}>{words[0]}</div>
                                    })
                                }
                            </TagCloud>
                        </Tab>
                    </Tabs>
                </div>
            </MuiThemeProvider>
        );
    }
}

global.dashboard.registerWidget("WordCloud", WordCloud);
