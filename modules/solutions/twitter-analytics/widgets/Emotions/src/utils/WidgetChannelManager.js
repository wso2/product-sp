/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

export default class WidgetChannelManager {

    constructor() {
        this.webSocket = null;
        this.widgetMap = {};
        this.subscribeWidget = this.subscribeWidget.bind(this);
        this.unsubscribeWidget = this.unsubscribeWidget.bind(this);
        this._initializeWebSocket = this._initializeWebSocket.bind(this);
        this._wsOnClose = this._wsOnClose.bind(this);
        this._wsOnError = this._wsOnError.bind(this);
        this._wsOnMessage = this._wsOnMessage.bind(this);
        this._initializeWebSocket();
        this.waitForConn = this.waitForConn.bind(this);

    }

    /**
     * Set a widget to the widget map and send configuration to the provider endpoint.
     * @param widgetId
     * @param callback
     * @param dataConfig
     */
    subscribeWidget(widgetId, callback, dataConfig) {
        this.widgetMap[widgetId] = callback;
        this.waitForConn(this.webSocket, () => {
            let configJSON = {
                providerName: dataConfig.type,
                dataProviderConfiguration: dataConfig.config,
                topic: widgetId,
                action: 'subscribe'
            };
            this.webSocket.send(JSON.stringify(configJSON));
        })
    }

    /**
     * remove a widget from the widget map
     * @param widgetId
     */
    unsubscribeWidget(widgetId) {
        delete this.widgetMap[widgetId];
        let config = {
            topic: widgetId,
            providerName: null,
            dataProviderConfiguration: null,
            action: 'unsubscribe',
        };
        this.webSocket.send(JSON.stringify(config));
    }

    /**
     * Initialize websocket
     * @private
     */
    _initializeWebSocket() {
        this.webSocket = new WebSocket('wss://' + window.location.host + '/data-provider');
        this.webSocket.onmessage = this._wsOnMessage;
        this.webSocket.onerror = this._wsOnError;
        this.webSocket.onclose = this._wsOnClose;
    }

    /**
     * handle web-socket on message event
     * @param message
     * @private
     */
    _wsOnMessage(message) {
        let data = JSON.parse(message.data);
        if (this.widgetMap[data.topic]) {
            this.widgetMap[data.topic](data);
        } else {
        }
    }

    /**
     * handle web-socket on error event
     * @param message
     * @private
     */
    _wsOnError(message) {
    }

    /**
     * handle web-socket on close event
     * @param message
     * @private
     */
    _wsOnClose(message) {
    }

    waitForConn(socket, callback) {
        let that = this;
        setTimeout(() => {
            if (socket.readyState === 1) {
                if (callback !== null) {
                    callback();
                }
            } else {
                that.waitForConn(socket, callback);
            }
        }, 1000)
    }
}
