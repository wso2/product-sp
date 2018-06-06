import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import Timeline from 'vis/lib/timeline/Timeline';
import DataSet from 'vis/lib/DataSet';
import 'vis/dist/vis.min.css';
import {Scrollbars} from 'react-custom-scrollbars';

class OpenTracingVisTimeline extends Widget {

    constructor(props) {
        super(props);
        this.myRef = React.createRef();
        this.state = {
            data: [],
            metadata: null,
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            traceId: null
        };
        this.chartUpdated = false;
        this._handleDataReceived = this._handleDataReceived.bind(this);
        this.setReceivedMsg = this.setReceivedMsg.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.addToTheGrandParentGroup = this.addToTheGrandParentGroup.bind(this);
        this.testFunction = this.testFunction.bind(this);
        this.clickHandler = this.clickHandler.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.timeline = null;
        this.tempItems = [];
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentWillMount() {
        super.subscribe(this.setReceivedMsg);
    }

    _handleDataReceived(data) {
        if (!this.chartUpdated) {
            this.testFunction(data.data);
            this.chartUpdated = true;
        }
        window.dispatchEvent(new Event('resize'));
    }

    setReceivedMsg(receivedMsg) {
        this.chartUpdated = false;
        super.getWidgetChannelManager().unsubscribeWidget(this.props.widgetID);
        if (receivedMsg.clearData && receivedMsg.clearData.indexOf('timeline') > -1) {
            this.testFunction([]);
        }
        if (receivedMsg.row) {
            let providerConfig = {
                configs: {
                    type: 'RDBMSBatchDataProvider',
                    config: {
                        datasourceName: 'Activity_Explorer_DB',
                        tableName: 'SpanTable',
                        queryData: {
                            query: `select componentName, traceId, spanId, parentId, serviceName, startTime, endTime, 
                            duration from SpanTable where traceId = '${receivedMsg.row.TRACEID}'`,
                        },
                        incrementalColumn: 'traceId',
                        publishingInterval: '5',
                        purgingInterval: '60',
                        publishingLimit: '30',
                        purgingLimit: '60',
                        isPurgingEnable: false
                    }
                }
            };
            super.getWidgetChannelManager().subscribeWidget(this.props.widgetID, this._handleDataReceived, providerConfig);
        }

    }

    testFunction(data) {
        let groupList = [];
        let itemList = [];
        this.tempItems = [];
        let lowestDate = -1;
        let highestDate = -1;
        if (data.length === 0) {
            if (this.timeline) {
                this.timeline.setGroups(new DataSet([]));
                this.timeline.setItems(new DataSet([]));
            }
        } else {
            for (let i = 0; i < data.length; i++) {
                let startTime = new Date(data[i][5]);
                let endTime;
                if (-1 !== data[i][6]) {
                    endTime = new Date(data[i][6]);
                } else {
                    endTime = new Date(data[i][5] + 1000);
                }
                if (lowestDate > startTime.getTime() || lowestDate === -1) {
                    lowestDate = startTime.getTime();
                }
                if (highestDate < endTime.getTime() || highestDate === -1) {
                    highestDate = endTime.getTime();
                }
                let item = {
                    start: startTime,
                    end: endTime,
                    content: data[i][4],
                    id: i + 1,
                    group: i + 1
                };
                let tempItem = {
                    content: data[i][4],
                    group: i + 1,
                    parent: data[i][3],
                    span: data[i][2],
                    id: i + 1
                };
                itemList.push(item);
                this.tempItems.push(tempItem);
                let group = {
                    content: "",
                    value: i + 1,
                    id: i + 1,
                    title: i + 1
                };
                groupList.push(group);
            }
            for (let i = 0; i < this.tempItems.length; i++) {
                for (let j = 0; j < this.tempItems.length; j++) {
                    if (this.tempItems[i]["parent"] === this.tempItems[j]["span"]) {
                        groupList = this.addToTheGrandParentGroup(groupList, this.tempItems, this.tempItems[j], this.tempItems[i]["id"]);
                    }
                }
            }
            let scale, step, addingLimits;
            if ((highestDate - lowestDate) < 1000) {
                scale = "millisecond";
                step = 100;
                addingLimits = 100;
            } else if ((highestDate - lowestDate) < 60000 && (highestDate - lowestDate) >= 1000) {
                scale = "second";
                step = 10;
                addingLimits = 10;
            } else if ((highestDate - lowestDate) >= 3600000 && (highestDate - lowestDate) >= 60000) {
                //during an hour
                scale = "minute";
                step = 10;
                addingLimits = 10;
            } else if ((highestDate - lowestDate) >= 86400000 && (highestDate - lowestDate) >= 3600000) {
                //during a day
                scale = "hour";
                step = 1;
                addingLimits = 1;
            } else if ((highestDate - lowestDate) >= 604800000 && (highestDate - lowestDate) >= 86400000) {
                //during a week
                scale = "day";
                step = 1;
                addingLimits = 1;
            } else if ((highestDate - lowestDate) >= 2592000000 && (highestDate - lowestDate) >= 604800000) {
                //during a month
                scale = "week";
                step = 1;
                addingLimits = 1;
            } else if ((highestDate - lowestDate) >= 31104000000 && (highestDate - lowestDate) >= 2592000000) {
                //during a month
                scale = "month";
                step = 1;
                addingLimits = 1;
            }
            let options = {
                groupOrder: function (a, b) {
                    return a.value - b.value;
                },
                groupOrderSwap: function (a, b, groups) {
                    let v = a.value;
                    a.value = b.value;
                    b.value = v;
                },
                groupTemplate: function (group) {
                    let container = document.createElement('div');
                    let label = document.createElement('span');
                    label.innerHTML = group.content + ' ';
                    container.insertAdjacentElement('afterBegin', label);
                    return container;
                },
                orientation: 'both',
                editable: false,
                groupEditable: false,
                start: new Date(lowestDate - addingLimits),
                end: new Date(highestDate + addingLimits),
                timeAxis: {scale: scale, step: step}
            };

            if (!this.timeline) {
                this.timeline = new Timeline(this.myRef.current);
                this.timeline.on('select', this.clickHandler);
            }
            this.timeline.setOptions(options);
            this.timeline.setGroups(new DataSet(groupList));
            this.timeline.setItems(new DataSet(itemList));

        }
    }

    clickHandler(properties) {
        for (let i = 0; i < this.tempItems.length; i++) {
            if (parseInt(properties.items) === this.tempItems[i]["id"]) {
                super.publish({data: this.tempItems[i]});
            }
        }
    }

    render() {
        return (
            <Scrollbars style={{height: this.state.height}}>
                <div ref={(ref) => {
                    this.myRef.current = ref;
                }}/>
            </Scrollbars>
        );
    }

    addToTheGrandParentGroup(groupList, tempItems, parentSpan, addingGroupId) {
        let groupListId = -1;
        for (let i = 0; i < tempItems.length; i++) {
            if (groupList[i]["id"] === parentSpan.id) {
                groupListId = i;
                break;
            }
        }
        if (!groupList[groupListId]["subgroupStack"]) {
            groupList[groupListId]["subgroupStack"] = {};
            groupList[groupListId]["nestedGroups"] = [];
        }
        groupList[groupListId]["subgroupStack"][addingGroupId] = false;
        groupList[groupListId]["nestedGroups"].push(addingGroupId);
        for (let j = 0; j < tempItems.length; j++) {
            if (tempItems[groupListId]["parent"] === tempItems[j]["span"]) {
                this.addToTheGrandParentGroup(groupList, tempItems, tempItems[j], addingGroupId);
            }
        }
        return groupList;
    }
}

global.dashboard.registerWidget('OpenTracingVisTimeline', OpenTracingVisTimeline);
