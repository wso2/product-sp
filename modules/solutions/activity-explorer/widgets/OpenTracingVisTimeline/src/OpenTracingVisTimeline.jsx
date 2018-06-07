import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import Timeline from 'vis/lib/timeline/Timeline';
import DataSet from 'vis/lib/DataSet';
import 'vis/dist/vis.min.css';
import {Scrollbars} from 'react-custom-scrollbars';
import Axios from 'axios';
import _ from 'lodash';

const COOKIE = 'DASHBOARD_USER';

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
        this.populateTimeline = this.populateTimeline.bind(this);
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
        let httpClient = Axios.create({
            baseURL: window.location.origin + window.contextPath,
            timeout: 2000,
            headers: {"Authorization": "Bearer " + OpenTracingVisTimeline.getUserCookie().SDID},
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient
            .get(`/apis/widgets/${this.props.widgetID}`)
            .then((message) => {
                this.setState({
                    dataProviderConf :  message.data.configs.providerConfig
                });
            })
            .catch((error) => {
                console.log("error", error);
            });
    }

    static getUserCookie() {
        const arr = document.cookie.split(';');
        for (let i = 0; i < arr.length; i++) {
            let c = arr[i];
            while (c.charAt(0) === ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(COOKIE) === 0) {
                return JSON.parse(c.substring(COOKIE.length + 1, c.length));
            }
        }
        return null;
    }

    _handleDataReceived(data) {
        if (!this.chartUpdated) {
            this.populateTimeline(data.data);
            this.chartUpdated = true;
        }
        window.dispatchEvent(new Event('resize'));
    }

    setReceivedMsg(receivedMsg) {
        this.chartUpdated = false;
        super.getWidgetChannelManager().unsubscribeWidget(this.props.widgetID);
        if (receivedMsg.clearData && receivedMsg.clearData.indexOf('timeline') > -1) {
            this.populateTimeline([]);
        }
        if (receivedMsg.row) {
            let providerConfig = _.cloneDeep(this.state.dataProviderConf);
            providerConfig.configs.config.queryData.query =
                providerConfig.configs.config.queryData.query.replace("${receivedMsg.row.TRACEID}",
                    receivedMsg.row.TRACEID);
            super.getWidgetChannelManager().subscribeWidget(
                this.props.widgetID, this._handleDataReceived, providerConfig);
        }

    }

    populateTimeline(data) {
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
                    id: i + 1,
                    start: startTime.getTime(),
                    end: endTime.getTime()
                };
                itemList.push(item);
                this.tempItems.push(tempItem);
                let group = {
                    content: "  ",
                    value: i + 1,
                    id: i + 1,
                    title: i + 1,
                    start: startTime.getTime(),
                    end: endTime.getTime()
                };
                groupList.push(group);
            }
            for (let i = 0; i < this.tempItems.length; i++) {
                for (let j = 0; j < this.tempItems.length; j++) {
                    if (this.tempItems[i]["parent"] === this.tempItems[j]["span"]) {
                        groupList = this.addToTheGrandParentGroup(
                            groupList, this.tempItems, this.tempItems[j], this.tempItems[i]["id"]);
                    }
                }
            }
            let swap_item_1;
            let swap_item_value1;
            let swap_item_2;
            let swap_item_value2;
            for (let i = 0; i < this.tempItems.length; i++) {
                for (let j = i+1; i+1 < this.tempItems.length && j < this.tempItems.length; j++) {
                    if ((this.tempItems[i].start < this.tempItems[j].start) ||
                        (this.tempItems[i].start === this.tempItems[j].start &&
                            this.tempItems[i].end > this.tempItems[j].end)) {
                        for (let k = 0; k < groupList.length; k++) {
                            if (this.tempItems[i].id === groupList[k].id) {
                                swap_item_1 = k;
                                swap_item_value1 = groupList[k].value;
                            }
                        }
                        for (let k = 0; k < groupList.length; k++) {
                            if (this.tempItems[j].id === groupList[k].id) {
                                swap_item_2 = k;
                                swap_item_value2 = groupList[k].value;
                            }
                        }
                        groupList[swap_item_1].value = swap_item_value2;
                        groupList[swap_item_2].value = swap_item_value1;
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
                    return  b.value - a.value;
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
