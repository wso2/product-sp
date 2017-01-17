/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

define(['require', 'backbone', 'app/source-editor/editor'],
function (require, Backbone, SiddhiEditor) {

    var SourceView = Backbone.View.extend({
        initialize: function (options) {
            this.options = options;
            this.mainEditor = new SiddhiEditor({
                divID: options.sourceContainer,
                realTimeValidation: true,
                autoCompletion: true
            });
        },
        render: function (options) {
            this.mainEditor.setContent(options.source);
            $(this.options.sourceContainer).show();
        }
    });

    return SourceView;
});