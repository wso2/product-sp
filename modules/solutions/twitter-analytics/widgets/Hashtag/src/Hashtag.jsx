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

import React from 'react';
import Widget from '@wso2-dashboards/widget';

class Hashtag extends Widget {
    constructor(props) {
        super(props);
        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            hashTag: [],
        };

        this.props.glContainer.on('resize', () => {
            this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height
            });
        });
    }

    componentDidMount() {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                super.getWidgetChannelManager()
                    .subscribeWidget(this.props.id, (data) => this.setState({ hashTag: data.data }),
                        message.data.configs.providerConfig);
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    render() {
        if (this.state.hashTag.length === 0) {
            var url = 'https://github.com/wso2/product-sp/blob/master/modules/solutions/twitter-analytics/README.md';
            return (
                <p style={{ textAlign: 'center' }}>
                    For the processing of "Twitter Analytics" please deploy the Siddhi app as per&nbsp;
                    <a
                        target="_blank"
                        href={url}
                        style={{ color: '#f00' }}>instructions.</a>
                </p>
            );
        }

        return (
            <p style={{ textAlign: "center", fontSize: '24px' }}>{this.state.hashTag[0]}</p>
        );
    }
}

global.dashboard.registerWidget('Hashtag', Hashtag);
