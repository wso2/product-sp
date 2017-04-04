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
define(['ace/ace', 'jquery', 'lodash', 'backbone', 'log', 'bootstrap', 'file_saver'], function (ace, $, _, Backbone, log) {

    // workspace manager constructor
    /**
     * Arg: application instance
     */
    return function (app) {
        const plan_regex = /@Plan:name\(['|"](.*?)['|"]\)/g;

        if (_.isUndefined(app.commandManager)) {
            var error = "CommandManager is not initialized.";
            log.error(error);
            throw error;
        }


        this.createNewTab = function createNewTab() {
            //var welcomeContainerId = app.config.welcome.container;
            //$(welcomeContainerId).css("display", "none");
            var editorId = app.config.container;
            $(editorId).css("display", "block");
            //Showing menu bar
            // app.menuBar.show();
            app.tabController.newTab();

            var editor = ace.edit('siddhi-editor');
            editor.setValue(
                "@Plan:name(\"ExecutionPlan\")\n\n" +
                "-- Please refer to https://docs.wso2.com/display/DAS400/Quick+Start+Guide " +
                "on getting started with DAS editor. \n\n"
            );
        };

        this.saveFileBrowserBased = function saveFile() {
            var editor = ace.edit('siddhi-editor');
            var code = editor.getValue();
            var filename = "untitled";
            var match = plan_regex.exec(code);
            if (match && match[1]) {
                filename = match[1].replace(/ /g, "_");
            }
            var blob = new Blob([code], {type: "text/plain;charset=utf-8"});
            saveAs(blob, filename + ".siddhi");
        };

        this.saveFile = function saveFile() {
            var editor = ace.edit('siddhi-editor');
            var code = editor.getValue();
            var filename = "untitled";
            var match = plan_regex.exec(code);
            if (match && match[1]) {
                filename = match[1].replace(/ /g, "_") + ".siddhi";
            }
            var filePath = prompt("Enter a directory (absolute path) to save the execution plan : ");
            filePath = (filePath.slice(-1) === '/') ? filePath + filename : filePath + '/' + filename;
            $.ajax({
                type: "POST",
                url: "http://localhost:9090/editor/save",
                data: JSON.stringify({
                    executionPlan: code,
                    filePath: filePath
                }),
                success: function (e) {
                    alert("file successfully saved.");
                },
                error: function (e) {
                    alert("failed to save file.");
                }
            });
        };

        this.popupRegularWelcomeScreen = function () {
            // hide the page content and only the regular welcome screen will be shown
            $(app.config.container).hide();
        };

        app.commandManager.registerCommand("create-new-tab", {key: ""});
        app.commandManager.registerHandler('create-new-tab', this.createNewTab);

        app.commandManager.registerCommand("save-to-file", {key: ""});
        app.commandManager.registerHandler('save-to-file', this.saveFile);

    }


});

