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

define(['require', 'jquery', 'backbone', 'lodash','log','./design', "./source"],

    function (require, $, Backbone,  _, log, DesignView, SourceView) {

    var ServicePreview = Backbone.View.extend(
        /** @lends ServicePreview.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ServicePreview Represents the view for siddhi samples
             * @param {Object} options Rendering options for the view
             */
        initialize: function (options) {
            if(!_.has(options, 'container')) {
                throw "container is not defined."
            }
            var container = $(_.get(options, 'container'));
            var toolPallete = $(_.get(options, 'toolPalette'));
            if(!container.length > 0) {
                throw "container not found."
            }
            this._$parent_el = container;
            this.options = options;

         },

        render: function () {
            var canvasContainer = this._$parent_el.find(_.get(this.options, 'canvas.container'));
            var previewContainer = this._$parent_el.find(_.get(this.options, 'preview.container'));
            var sourceContainer = this._$parent_el.find(_.get(this.options, 'source.container'));
            var tabContentContainer = $(_.get(this.options, 'tabs_container'));
            var toolPallette = _.get(this.options, 'toolPalette._$parent_el');

            if(!canvasContainer.length > 0){
                var errMsg = 'cannot find container to render svg';
                log.error(errMsg);
                throw errMsg;
            }
            var designViewOpts = {};
            _.set(designViewOpts, 'container', canvasContainer.get(0));

            //use this line to assign dynamic id for canvas and pass the canvas id to initialize jsplumb
            canvasContainer.attr('id', 'canvasId1');

            var sourceViewOptions = {
                sourceContainer: sourceContainer.attr('id')
            };

            var sourceView = new SourceView(sourceViewOptions);
            canvasContainer.removeClass('show-div').addClass('hide-div');
            previewContainer.removeClass('show-div').addClass('hide-div');
            sourceContainer.removeClass('source-view-disabled').addClass('source-view-enabled');
            toolPallette.addClass('hide-div');
            tabContentContainer.removeClass('tab-content-default');
            sourceView.render(sourceViewOptions);
        }
    });
    return ServicePreview;
});
