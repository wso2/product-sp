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
define(['jquery', 'lodash', 'backbone', 'log'], function ($, _, Backbone, log) {

    var File = Backbone.Model.extend(
        {
            defaults: {
                path: 'unsaved/',
                isTemp: true,
                isPersisted: false
            },

            initialize: function (attrs, options) {
                var errMsg;
                if (_.isEqual(this.get('isPersisted'), false)){
                    if(!_.has(options, 'storage')){
                        errMsg = 'unable to find storage' + _.toString(attrs);
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var storage = _.get(options, 'storage');
                    if(!_.isUndefined(storage.create(this))){
                        this.set('isPersisted', true);
                    }
                }
            }
        });

    return File;
});