import React, {Component} from "react";
import Widget from "@wso2-dashboards/widget";
import Timeline from 'vis/lib/timeline/Timeline';
import DataSet from 'vis/lib/DataSet';
import 'vis/dist/vis.min.css';
import {Scrollbars} from 'react-custom-scrollbars';
import './OpenTracingVisTimeline.css';

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
        this.handleResize = this.handleResize.bind(this);
        this.addToTheGrandParentGroup = this.addToTheGrandParentGroup.bind(this);
        this.populateTimeline = this.populateTimeline.bind(this);
        this.clickHandler = this.clickHandler.bind(this);
        this.props.glContainer.on('resize', this.handleResize);
        this.timeline = null;
        this.tempItems = [];
        this.itemList = [];
        this.descriptionItemList = [];
        this.clickedItemGroupId = -1;
    }

    handleResize() {
        this.setState({width: this.props.glContainer.width, height: this.props.glContainer.height});
    }

    componentDidMount() {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                var urlParams = new URLSearchParams(decodeURI(window.location.search));
                message.data.configs.providerConfig.configs.config.queryData.query
                    = message.data.configs.providerConfig.configs.config.queryData.query
                    .replace("${traceId}", urlParams.get('traceid'));
                super.getWidgetChannelManager().subscribeWidget(
                    this.props.widgetID, this._handleDataReceived, message.data.configs.providerConfig);
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

    populateTimeline(data) {
        let groupList = [];
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
                if (lowestDate > data[i][5] || lowestDate === -1) {
                    lowestDate = data[i][5];
                }
                if (highestDate < data[i][6] || highestDate === -1) {
                    highestDate = data[i][6];
                }
            }
            let latestId = -1;
            for (let i = 0; i < data.length; i++) {
                latestId = i;
                let startTime = new Date(data[i][5]);
                let endTime;
                if (-1 !== data[i][6]) {
                    endTime = new Date(data[i][6]);
                } else {
                    endTime = new Date(data[i][5] + 1000);
                }
                let item = {
                    type2: "span",
                    start: startTime,
                    end: endTime,
                    content: data[i][4],
                    title: data[i][4],
                    id: i + 1 + 0.1,
                    group: i + 1,
                };
                let descriptionItem = {
                    type2: "description",
                    start: new Date(lowestDate),
                    end: new Date(highestDate),
                    tags: data[i][8],
                    baggageItems: data[i][9],
                    id: i + 1,
                    group: i + 1,
                    className: "constant_value"
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
                this.descriptionItemList.push(descriptionItem);
                this.itemList.push(item);
                this.tempItems.push(tempItem);
                let group = {
                    content: "",
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
                //from 1 millisecond to second
                scale = "millisecond";
                step = 100;
                addingLimits = 10;
            } else if ((highestDate - lowestDate) < 60000 && (highestDate - lowestDate) >= 1000) {
                //from 1 second to 1 minute
                scale = "second";
                step = 10;
                addingLimits = 2000;
            } else if ((highestDate - lowestDate) <= 3600000 && (highestDate - lowestDate) >= 60000) {
                //from 1 minute to 1 hour
                scale = "minute";
                step = 10;
                addingLimits = 60000;
            } else if ((highestDate - lowestDate) <= 86400000 && (highestDate - lowestDate) >= 3600000) {
                //from 1 hour to 24 hours
                scale = "hour";
                step = 1;
                addingLimits = 3600000;
            } else if ((highestDate - lowestDate) <= 604800000 && (highestDate - lowestDate) >= 86400000) {
                //from 24 hours to week
                scale = "day";
                step = 1;
                addingLimits = 86400000;
            } else if ((highestDate - lowestDate) <= 2592000000 && (highestDate - lowestDate) >= 604800000) {
                //from week to month
                scale = "week";
                step = 1;
                addingLimits = 604800000;
            } else if ((highestDate - lowestDate) <= 31104000000 && (highestDate - lowestDate) >= 2592000000) {
                //during a month
                scale = "month";
                step = 1;
                addingLimits = 2592000000;
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
                template: function (item, element, data) {
                    if (item.type2 === "span") {
                        return item.content;
                    } else {
                        let table = '<table class="description_table">';
                        table = table + '<tr><th>Tags</th></tr>';
                        try {
                            let dataArray = JSON.parse(item.tags);
                            if (0 < dataArray.length) {
                                for (let i = 0; i < dataArray.length; i++){
                                    table = table +
                                        '<tr>' +
                                        '<td style="width: 30%;">'+Object.keys(dataArray[i])[0]+'</td>' +
                                        '<td style="width: 70%;">'+dataArray[i][Object.keys(dataArray[i])[0]]+'</td>' +
                                        '</tr>'
                                }
                            } else {
                                table = table +
                                    '<tr>' +
                                    '<td>No available tags</td>' +
                                    '</tr>'
                            }
                        }
                        catch(err) {
                            table = table +
                                '<tr>' +
                                '<td>No available tags</td>' +
                                '</tr>'
                        }
                        table = table + '<tr><th>Baggage Items</th></tr>';
                        try {
                            let dataArray = JSON.parse(item.baggageItems);
                            if (0 < dataArray.length) {
                                for (let i = 0; i < dataArray.length; i++){
                                    table = table +
                                        '<tr>' +
                                        '<td style="width: 30%;">'+Object.keys(dataArray[i])[0]+'</td>' +
                                        '<td style="width: 70%;">'+dataArray[i][Object.keys(dataArray[i])[0]]+'</td>' +
                                        '</tr>'
                                }
                            } else {
                                table = table +
                                    '<tr>' +
                                    '<td>No available baggage items</td>' +
                                    '</tr>'
                            }
                        }
                        catch(err) {
                            table = table +
                                '<tr>' +
                                '<td>No available baggage items</td>' +
                                '</tr>'
                        }

                        return table;
                    }
                },
                showTooltips: true,
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
            this.timeline.setItems(new DataSet(this.itemList));

        }
    }

    clickHandler(properties) {
        for (let i = 0; i < this.tempItems.length; i++) {
            if (parseInt(properties.items) === this.tempItems[i]["id"]) {
                if (this.clickedItemGroupId === -1) {
                    this.clickedItemGroupId = parseInt(properties.items);
                    for (let i = 0; i < this.descriptionItemList.length; i++) {
                        if (this.descriptionItemList[i].group === this.clickedItemGroupId) {
                            this.itemList.push(this.descriptionItemList[i]);
                            this.timeline.setItems(new DataSet(this.itemList));
                            break;
                        }
                    }
                } else {
                    for (let i = 0; i < this.itemList.length; i++) {
                        if (this.itemList[i].id === this.clickedItemGroupId) {
                            this.clickedItemGroupId = -1;
                            this.itemList.splice(i, 1);
                            this.timeline.setItems(new DataSet(this.itemList));
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    render() {
        return (
            <Scrollbars style={{height: this.state.height}}>
                <div className="timeline-wrapper">
                    <div
                        ref={(ref) => {this.myRef.current = ref;}}
                        className="timeline-gadget-wrapper"
                    />
                </div>
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
