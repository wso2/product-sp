import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import VizG from 'react-vizgrammar';
import {Scrollbars} from 'react-custom-scrollbars';

class OpenTracingEvent extends Widget {

    constructor(props) {
        super(props);

        this.state = {
            data: [],
            metadata: null,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height
        };

        this.tableConfig = {
            charts: [
                {
                    type: 'table',
                    columns: [
                        {
                            "name": "attributeName",
                            "title": "Attribute Name"
                        },
                        {
                            "name": "attributeValue",
                            "title": "Attribute Value"
                        }
                    ]
                },
            ],
            append: false
        };
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.setReceivedMsg= this.setReceivedMsg.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentWillMount() {
        super.subscribe(this.setReceivedMsg);
    }

    _handleDataReceived(data) {
        var metadata_object = {};
        metadata_object.names = ["attributeName", "attributeValue"];
        metadata_object.types = ["ORDINAL", "ORDINAL"]
        if (data.data[0]) {
            var data = data.data[0];
            var data_object = [];
            if (data) {
                var dataString = data[0].replace(/'/g, '"');
                var dataArray = JSON.parse(dataString);
                for (var i = 0; i < dataArray.length; i++) {
                    var data_element = [];
                    data_element.push(Object.keys(dataArray[i])[0]);
                    data_element.push(dataArray[i][Object.keys(dataArray[i])[0]]);
                    data_object.push(data_element);
                }
            }
        }

        this.setState({
            metadata: metadata_object,
            data: data_object,
        });
        window.dispatchEvent(new Event('resize'));
    }

    setReceivedMsg(receivedMsg) {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.widgetID);
        if(receivedMsg.data && receivedMsg.data.span) {
            this.providerConfig = {
                configs: {
                    type: "RDBMSBatchDataProvider",
                    config: {
                        datasourceName: 'Activity_Explorer_DB',
                        tableName: 'SpanTable',
                        queryData: {
                            query: `select tags from SpanTable where spanId ='${receivedMsg.data.span}'`
                        },
                        incrementalColumn: 'tags',
                        publishingInterval: '5',
                        purgingInterval: '60',
                        publishingLimit: '30',
                        purgingLimit: '60',
                        isPurgingEnable: false,
                    }
                }
            };
            super.getWidgetChannelManager().subscribeWidget(this.props.widgetID, this._handleDataReceived, this.providerConfig);
        }

        if(receivedMsg.clearData && receivedMsg.clearData.indexOf('event') > -1) {
            this.setState({ data: [] });
        }

    }

    render() {
        if (this.state.data[0]) {
            return (
                <Scrollbars style={{height: this.state.height}}>
                    <div
                        style={{
                            marginTop: "5px",
                            width: this.props.glContainer.width,
                            height: this.props.glContainer.height,
                        }}
                    >
                        <VizG
                            config={this.tableConfig}
                            metadata={this.state.metadata}
                            data={ this.state.data}
                            append={false}
                            height={this.props.glContainer.height}
                            width={this.props.glContainer.width}
                        />
                    </div>
                </Scrollbars>
            );
        } else {
            return null;
        }
    }
}

global.dashboard.registerWidget('OpenTracingEvent', OpenTracingEvent);