/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['require', 'log', 'jquery', 'backbone', './tool-group-view', './tool-group'],
    function (require, log, $, Backbone, ToolGroupView, ToolGroup ) {

    var ToolPalette = Backbone.View.extend({
        initialize: function (options) {
            var errMsg;
            if (!_.has(options, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(options, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for tab list with selector: ' + _.get(options, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;
            this._options = options;
             this._initTools();
            //this.dragDropManager = new DragDropManager();
            this.commandManager = options.application.commandManager;
            this._renderCount = 0;
        },
         resetToolGroup: function(){
           this._toolGroups = [];
             $("#tool-group-main-tool-group").remove();
             $("#tool-group-query-tool-group").remove();
         },
        _initTools: function(){

            var _toolGroups = [];


            var definitionTools = [
                {
                    id : "stream",
                    title : "Stream",
                    icon : "/editor/images/stream.png"
                },
                {
                    id : "window-stream",
                    title : "window",
                    icon : "/editor/images/windowStream.png"
                }
            ];
            var mainToolGroup = new ToolGroup({
                toolGroupName: "Elements",
                toolGroupID: "main-tool-group",
                toolDefinitions: definitionTools
            });
            _toolGroups.push(mainToolGroup);

            var queryTools =[
                {
                    id : "pass-through",
                    title : "Pass Through",
                    icon : "/editor/images/passthrough.png"
                },
                {
                    id : "filter-query",
                    title : "Filter Query",
                    icon : "/editor/images/filter.png"
                },
                {
                    id : "window-query",
                    title : "Window Query",
                    icon : "/editor/images/windowQuery.png"
                },
                {
                    id : "join-query",
                    title : "Join Query",
                    icon : "/editor/images/join.png"
                },
                {
                    id : "pattern",
                    title : "Pattern",
                    icon : "/editor/images/pattern.png"
                }
                ];
            var queryToolGroup = new ToolGroup({
                    toolGroupName: "Queries",
                    toolGroupID: "query-tool-group",
                    toolDefinitions: queryTools
            });
            _toolGroups.push(queryToolGroup);
            this._toolGroups = _toolGroups;
            // // Create main tool group
            // var mainToolGroup = new ToolGroup({
            //     toolGroupName: "Elements",
            //     toolGroupID: "main-tool-group",
            //     toolDefinitions: MainElements.lifelines
            // });
            // _toolGroups.push(mainToolGroup);
            //
            // // Create mediators tool group
            // var mediatorsToolGroup = new ToolGroup({
            //     toolGroupName: "Mediators",
            //     toolGroupID: "mediators-tool-group",
            //     toolDefinitions: _.assign({},Processors.manipulators, Processors.flowControllers)
            // });
            // _toolGroups.push(mediatorsToolGroup);
            //
            // this._toolGroups = _toolGroups;

        },

        render: function () {
            var self = this;
                var toolPaletteDiv = $('<div></div>');
                //Adding search bar to tool-palette
            if(this._renderCount == 0) {
                var searchBarDiv = $('<div></div>');
                searchBarDiv.addClass(_.get(this._options, 'search_bar.cssClass.search_box'));
                var searchInput = $('<input>');
                searchInput.addClass(_.get(this._options, 'search_bar.cssClass.search_input'));
                searchInput.attr('id', 'search-field').attr('placeholder', 'Search').attr('type', 'text');
                var searchIcon = $('<i></i>');
                searchIcon.addClass(_.get(this._options, 'search_bar.cssClass.search_icon'));
                searchBarDiv.append(searchIcon);
                searchBarDiv.append(searchInput);
                toolPaletteDiv.append(searchBarDiv);
                this._renderCount++;
            }
                // End of adding search bar
                this._$parent_el.append(toolPaletteDiv);
                this.$el = toolPaletteDiv;

                this._toolGroups.forEach(function (group) {
                    var groupView = new ToolGroupView({model: group, toolPalette: self});
                    groupView.render(self.$el);
                    self.$el.addClass('non-user-selectable');
                });
            // For search
            var toolGroupList = this._toolGroups;
            var toolView = this;
            // var opts = this._options.application.config.alerts;

            //when search input field is empty render entire tool palette
            $("#search-field").unbind('keyup').bind('keyup', function (e) {
                e.preventDefault();
                if (!this.value) {
                    toolView.resetToolGroup();
                    toolView._initTools();
                    toolView.render();
                    toolGroupList = toolView._toolGroups;
                }
            });

            $("#search-field").keypress(function (e) {
                var key = e.which;
                //When user press 'enter'
                if (key == 13) {

                    var keyword = this.value;
                    // removing spaces from the keyword
                    keyword = keyword.replace(/\s+/g, '');
                    // For main elements
                    if (toolGroupList[0].tools.length > 0) {
                        var foundMain = _.find(toolGroupList[0].tools, function (tool) {
                            return tool.id.toLowerCase().includes(keyword.toLowerCase());
                        });
                    }
                    // For mediators
                    if (toolGroupList[1]) {
                        var foundMediator = _.find(toolGroupList[1].tools, function (tool) {
                            return tool.id.toLowerCase().includes(keyword.toLowerCase());
                        });
                    }
                    // If main element
                    if (!_.isUndefined(foundMain)) {
                        var sampleGroup = toolGroupList[0];
                        var toRemove = _.without(toolGroupList[0].tools, foundMain);
                        var newMainGroup = _.difference(toolGroupList[0].tools, toRemove);
                        toolView.resetToolGroup();
                        sampleGroup.tools = newMainGroup;
                        toolView._toolGroups.push(sampleGroup);
                        toolView.render();
                    }

                    // If mediator
                    else if (!_.isUndefined(foundMediator)) {
                        var sampleGroup1 = toolGroupList[1];
                        var toRemove1 = _.without(toolGroupList[1].tools, foundMediator);
                        var newMediatorGroup = _.difference(toolGroupList[1].tools, toRemove1);
                        toolView.resetToolGroup();
                        sampleGroup1.tools = newMediatorGroup;
                        toolView._toolGroups.push(sampleGroup1);
                        toolView.render();
                    }
                }

            });
            return this;
        },

        hideToolPalette: function () {
            this._$parent_el.hide();
        },

        showToolPalette: function () {
            this._$parent_el.show();
        }
    });

    return ToolPalette;
});
