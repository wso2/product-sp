import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import VizG from 'react-vizgrammar';
import {Scrollbars} from 'react-custom-scrollbars';

class OpenTracingList extends Widget {

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
                    deduplicationColumn: "traceId",
                    columns: [
                        {
                            "name": "TRACEID",
                            "title": "Trace"
                        },
                        {
                            "name": "COMPONENTNAME",
                            "title": "Component"
                        },
                        {
                            "name": "COUNT",
                            "title": "Count"
                        },
                        {
                            "name": "ELAPSED_TIME",
                            "title": "Elapsed Time"
                        },
                        {
                            "name": "START_TIME",
                            "title": "Start Time"
                        },
                        {
                            "name": "END_TIME",
                            "title": "End Time"
                        }
                    ]
                },
            ],
            pagination: true
        };


        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.setReceivedMsg= this.setReceivedMsg.bind(this);
        this.onClickHandler = this.onClickHandler.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.tableIsUpdated = false;

        this.providerConfig = {
            configs: {
                type: "RDBMSBatchDataProvider",
                config: {
                    datasourceName: 'Activity_Explorer_DB',
                    tableName: 'SpanTable',
                    queryData: {
                        query: 'select traceId, componentName, count(*) as count, (MAX(endTime) - MIN(startTime)) as elapsed_time, MIN(startTime) as start_time, MAX(endTime) as end_time from SpanTable {{query}} GROUP BY traceId, componentName'
                    },
                    incrementalColumn: 'traceId',
                    publishingInterval: '5',
                    purgingInterval: '60',
                    publishingLimit: '1000',
                    purgingLimit: '60',
                    isPurgingEnable: false,
                }
            },
        };

    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentWillMount() {
        super.subscribe(this.setReceivedMsg);
    }

    _handleDataReceived(data) {
        for (var i = 0; i < data["data"].length; i++) {
            var date = new Date(data["data"][i][4]);
            var date2 = new Date(data["data"][i][5]);
            data["data"][i][4] = date.toLocaleDateString() + " " + date.toLocaleTimeString();
            data["data"][i][5] = date2.toLocaleDateString() + " " + date2.toLocaleTimeString();
        }
        data["metadata"]["types"][4] = "ORDINAL";
        data["metadata"]["types"][5] = "ORDINAL";
        if (!this.tableIsUpdated) {
            this.setState({
                metadata: data.metadata,
                data: data.data,
            });
            this.tableIsUpdated = true;
            window.dispatchEvent(new Event('resize'));
        }
    }

    setReceivedMsg(receivedMsg) {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.widgetID);
        var initial = true;
        var query = "where";
        for (var item in receivedMsg) {
            if (receivedMsg.hasOwnProperty(item)) {
                if (receivedMsg[item] !== "All" && receivedMsg[item] !== "" && receivedMsg[item] !== 0 && item !== "clearData") {
                    if (!initial) {
                        query = query + " and ";
                    } else {
                        initial = false;
                    }
                    if (item === "selectedComponentName") {
                        query = query + " componentName='" + receivedMsg[item] + "'";
                    } else if (item === "selectedServiceName") {
                        query = query + " serviceName='" + receivedMsg[item] + "'";
                    } else if (item === "selectedStartTime") {
                        query = query + " startTime>=" + receivedMsg[item] + "";
                    } else if (item === "selectedEndTime") {
                        query = query + " endTime<=" + receivedMsg[item] + "";
                    } else if (item === "selectedMinDuration") {
                        query = query + " duration>=" + receivedMsg[item] + "";
                    } else if (item === "selectedMaxDuration") {
                        query = query + " duration<=" + receivedMsg[item] + "";
                    }
                }

            }
        }
        if (query === "where") {
            this.providerConfig.configs.config.queryData.query = this.providerConfig.configs.config.queryData.query.replace("{{query}}", "");
        } else {
            this.providerConfig.configs.config.queryData.query = this.providerConfig.configs.config.queryData.query.replace("{{query}}", query);
        }
        super.getWidgetChannelManager().subscribeWidget(this.props.widgetID, this._handleDataReceived, this.providerConfig);
    }

    render() {
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
                        onClick={this.onClickHandler}
                    />
                </div>
            </Scrollbars>
        );
    }

    onClickHandler(row) {
        super.publish({row: row, clearData: ['timeline','event']});
    }
}

global.dashboard.registerWidget('OpenTracingList', OpenTracingList);