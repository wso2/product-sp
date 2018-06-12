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

class TopCountries extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            countryData: [],
            metadata: this.metadata,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            selectedCountry: "All",
            selectedTweets: 0
        };

        this.mapConfig = {
            x: 'Country',
            charts: [{type: 'map', y: 'Tweets', mapType: 'world', colorScale: ['#1E90FF', '#00008B']}],
            width: this.state.width,
            height: this.state.height,
            style: {
                legendTitleColor: '#5d6e77',
                legendTextColor: '#5d6e77',
            },
        };

        this.metadata = {
            names: ['Country', 'Tweets'],
            types: ['ordinal', 'linear']
        };

        this.providerConfig = {
            configs: {
                type: 'RDBMSBatchDataProvider',
                config: {
                    datasourceName: 'Twitter_Analytics',
                    queryData: {
                        query: "select country, count(TweetID) as Tweets from sentiment where PARSEDATETIME(timestamp, 'yyyy-mm-dd hh:mm:ss','en') > CURRENT_TIMESTAMP()-86400 group by country"
                    },
                    tableName: 'sentiment',
                    incrementalColumn: 'country',
                    publishingInterval: 30,
                    publishingLimit: 252
                }
            }
        };

        this.setSelectedCountry = this.setSelectedCountry.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this._handleDataReceived = this._handleDataReceived.bind(this);

    }

    setSelectedCountry(selected) {
        this.setState({selectedCountry: selected.givenName, selectedTweets: selected.y});
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        super.getWidgetChannelManager().subscribeWidget(this.props.id, this._handleDataReceived, this.providerConfig);
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    _handleDataReceived(setData) {
        this.setState({
            metadata: setData.metadata,
            countryData: setData.data,
            selectedTweets: this.sumOfAllTweets(setData.data),
            selectedCountry: "All",
        });
    }

    sumOfAllTweets(countData) {
        let count = 0;
        countData && countData.map((ele, key) => {
            count = count + ele[1];
        });
        return count
    }

    render() {
        return (
            <div className="sample-dashboard-content" style={{height: this.state.height}}>
                <div className="sample-dashboard-content-rev-text" style={{height: '100%', color: '#5d6e77'}}
                     onClick={this.setSelectedCountry.bind(this, {
                         givenName: 'ALL',
                         y: this.sumOfAllTweets(this.state.countryData)
                     })}>
                    <div style={{
                        margin: 'auto',
                        textAlign: 'center',
                        fontSize: '1.2em',
                        overflow: 'hidden',
                        paddingTop: 15
                    }}>
                        <div style={{width: '40%', display: 'inline-block', float: 'left'}}>
                            <strong style={{display: 'block'}}>Country</strong>
                            <span style={{display: 'block'}}>{this.state.selectedCountry}</span>
                        </div>
                        <div style={{width: '40%', display: 'inline-block'}}>
                            <strong style={{display: 'block'}}>Tweets</strong>
                            <span style={{display: 'block'}}>{this.state.selectedTweets}</span>
                        </div>
                    </div>
                </div>
                <h5 style={{position: 'absolute', bottom: 0, paddingRight: 5}}>Tweets from countries over last
                    24hrs</h5>
                <div className="sample-dashboard-content-map" style={{height: '100%', width: '60%'}}>
                    <VizG
                        config={this.mapConfig}
                        metadata={this.metadata}
                        data={this.state.countryData}
                        onClick={this.setSelectedCountry}
                    />
                </div>
            </div>
        );
    }
}

global.dashboard.registerWidget("TopCountries", TopCountries);
