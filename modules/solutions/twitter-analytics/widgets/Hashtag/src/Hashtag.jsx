/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, {Component} from 'react';
import Widget from '@wso2-dashboards/widget';
import WidgetChannelManager from './utils/WidgetChannelManager';

class Hashtag extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            hashTag: [],
        };

        this.providerConfig = {
            type: 'RDBMSBatchDataProvider',
            config: {
                datasourceName: 'Twitter_Analytics',
                queryData:{
                    query: "select trackwords from hashTag"
                },
                tableName: 'hashTag',
                incrementalColumn: 'id',
                publishingInterval: 20,
            }
        };

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

    render() {
        return (
            <h1 style={{textAlign: "center"}}>{this.state.hashTag[0]}</h1>
        );
    }
}

global.dashboard.registerWidget('Hashtag', Hashtag);