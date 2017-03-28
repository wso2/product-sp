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
define(['require', 'log', 'jquery', 'jsplumb','backbone', 'lodash','jquery_ui'],

function (require, log, $, _jsPlumb ,Backbone, _

) {

    var DesignView = Backbone.View.extend(
        /** @lends DesignView.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class DesignView Represents the view for the siddhi
             * @param {Object} options Rendering options for the view
             */
            initialize: function (opts) {
                var i =0;
                this.options = opts;

                _jsPlumb.ready(function() {

                    _jsPlumb.importDefaults({
                        PaintStyle : {
                            strokeWidth:2,
                            stroke: 'darkblue',
                            outlineStroke:"transparent",
                            outlineWidth:"5"
                            // lineWidth: 2
                        },
                        HoverPaintStyle :{
                            strokeStyle: 'darkblue',
                            strokeWidth : 3
                        },
                        Overlays : [["Arrow",  {location:1.0, id:"arrow" }]],
                        DragOptions : { cursor: "crosshair" },
                        Endpoints : [ [ "Dot", { radius:7 } ], [ "Dot", { radius:11 } ] ],
                        EndpointStyle : {
                            radius: 3
                        },
                        ConnectionsDetachable:false,
                        Connector: ["Bezier", {curviness: 50}]
                    });
                    _jsPlumb.setContainer($(opts.container));

                    $(opts.container).droppable
                    ({
                        accept: '#stream, #window-stream, #pass-through, #filter-query,  #join-query, #window-query, #pattern ',
                        drop: function (e, ui) {
                            console.log(i);
                            i++;
                        }
                    });
                });
            }

        });

    return DesignView;
});

