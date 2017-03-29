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

/*
 * This module contains the integration code segment of Siddhi editor.
 * This will set the options of ACE editor, attach client side parser and attach SiddhiCompletion Engine with the editor
 */
define(["ace/ace", "jquery", "./constants", "./utils", "./completion-engine", "./token-tooltip", "ace/ext/language_tools"],
    function (ace, $, constants, utils, CompletionEngine, aceTokenTooltip, aceExtLangTools) {

        "use strict";   // JS strict mode

        /*
         * Map for completion list styles
         * Update this map to update the styles applied to the completion list popup items
         */
        var completionTypeToStyleMap = {};
        completionTypeToStyleMap[constants.SNIPPETS] = "font-style: italic;";

        /*
         * Generating the displayNameToStyleMap from completionTypeToStyleMap
         * This is done to support defining completion popup styles using the completion type name rather than the display name
         */
        var displayNameToStyleMap = {};
        for (var completionType in completionTypeToStyleMap) {
            if (completionTypeToStyleMap.hasOwnProperty(completionType)) {
                displayNameToStyleMap[constants.typeToDisplayNameMap[completionType]] =
                    completionTypeToStyleMap[completionType];
            }
        }

        /*
         * Loading meta data for the completion engine from the server
         */
        CompletionEngine.loadMetaData();

        /**
         * Siddhi Editor prototype constructor
         *
         * @constructor
         * @param {Object} config The configuration object to be used in the initialization
         */
        function SiddhiEditor(config) {
            var self = this;
            var aceEditor = ace.edit(config.divID);                // Setting the DivID of the Editor .. Could be <pre> or <div> tags

            self.realTimeValidation = config.realTimeValidation;
            new aceTokenTooltip.TokenTooltip(aceEditor);

            /*
             * Setting the language mode to siddhi
             *
             * Language mode is located at ace-editor/mode/siddhi.js
             * Highlight style is located at ace-editor/mode/siddhi_highlight_rules.js.js
             * Folding is located at ace-editor/mode/folding/siddhi.js
             * Snippets are located at ace-editor/snippets/siddhi.js
             */
            aceEditor.session.setMode(constants.ace.SIDDHI_MODE);

            // Setting the editor options
            aceEditor.setReadOnly(config.readOnly);
            aceEditor.setTheme(config.theme ? "ace/theme/" + config.theme : constants.ace.DEFAULT_THEME);
            aceEditor.getSession().setUseWrapMode(true);
            aceEditor.getSession().setTabSize(4);
            aceEditor.getSession().setUseSoftTabs(true);
            aceEditor.setShowFoldWidgets(true);
            aceEditor.setBehavioursEnabled(true);
            aceEditor.setHighlightSelectedWord(true);
            aceEditor.setHighlightActiveLine(true);
            aceEditor.setDisplayIndentGuides(true);
            aceEditor.setShowPrintMargin(false);
            aceEditor.setShowFoldWidgets(true);
            aceEditor.session.setFoldStyle("markbeginend");
            aceEditor.setFontSize(14);
            aceEditor.setOptions({
                enableBasicAutocompletion: !config.readOnly && config.autoCompletion,
                enableSnippets: !config.readOnly && config.autoCompletion,
                enableLiveAutocompletion: config.autoCompletion,
                autoScrollEditorIntoView: true,
                enableMultiselect: false
            });

            /* Debug points */
            var $breakpoints = [];

            aceEditor.on("guttermousedown", function(e) {
                var target = e.domEvent.target;
                if (target.className.indexOf("ace_gutter-cell") == -1)
                    return;
                if (!aceEditor.isFocused())
                    return;
                if (e.clientX > 25 + target.getBoundingClientRect().left)
                    return;

                var breakpoints = e.editor.session.getBreakpoints(row, 0);
                var row = e.getDocumentPosition().row;
                if(typeof breakpoints[row] === typeof undefined) {
                    $breakpoints[row] = true;
                    e.editor.session.setBreakpoint(row);
                } else {
                    delete $breakpoints[row];
                    e.editor.session.clearBreakpoint(row);
                }
                console.log(JSON.stringify($breakpoints));
                e.stop();
            })

            aceEditor.on("change", function(e) {
                var len, firstRow;

                if (e.end.row == e.start.row) {
                    // editing in same line
                    return;
                } else {
                    // new line or remove line
                    if (e.action == "insert") {
                        len = e.end.row - e.start.row;
                        firstRow = e.start.column == 0 ? e.start.row: e.start.row + 1;
                    } else if (e.action == "remove") {
                        len = e.start.row - e.end.row
                        firstRow = e.start.row;
                    }

                    if (len > 0) {
                        var args = Array(len);
                        args.unshift(firstRow, 0)
                        $breakpoints.splice.apply($breakpoints, args);
                    } else if (len < 0) {
                        var rem = $breakpoints.splice(firstRow + 1, -len);
                        if(!$breakpoints[firstRow]){
                            for (var oldBP in rem) {
                                if (rem[oldBP]){
                                    $breakpoints[firstRow] = rem[oldBP]
                                    break
                                }
                            }
                        }
                    }

                    // Redraw the breakpoints
                    for (var r in $breakpoints) {
                        if ($breakpoints[r]) {
                            aceEditor.session.setBreakpoint(r);
                        } else {
                            aceEditor.session.clearBreakpoint(r);
                        }
                    }

                }

                console.log(JSON.stringify($breakpoints));
                console.log(JSON.stringify(e));
            })

            // State variables for error checking and highlighting
            self.state = {};
            self.state.syntaxErrorList = [];        // To save the syntax Errors with line numbers
            self.state.semanticErrorList = [];      // To save semanticErrors with line numbers
            self.state.lastEdit = 0;                // Last edit time

            self.completionEngine = new CompletionEngine();

            // Attaching editor's onChange event handler
            aceEditor.getSession().on('change', editorChangeHandler);

            // For adjusting the completer list as required
            adjustAutoCompletionHandlers();
            aceEditor.commands.on('afterExec', function () {
                adjustAutoCompletionHandlers();
            });

            // Adding events for adjusting the completions list styles
            // This is used for showing different styles for different types of completions
            aceEditor.renderer.on("afterRender", function () {
                // Checking if a popup is open when the editor is re-rendered
                if (aceEditor.completer && aceEditor.completer.popup) {
                    // Adding a on after render event for updating the popup styles
                    aceEditor.completer.popup.renderer.on("afterRender", function () {
                        var completionElements = document.querySelectorAll(
                            ".ace_autocomplete > .ace_scroller > .ace_content > .ace_text-layer > .ace_line"
                        );
                        for (var i = 0; i < completionElements.length; i++) {
                            var element =
                                completionElements[i].getElementsByClassName("ace_rightAlignedText")[0];
                            if (element && displayNameToStyleMap[element.innerHTML]) {
                                completionElements[i].setAttribute(
                                    "style",
                                    displayNameToStyleMap[element.innerHTML]
                                );
                            }
                        }
                    });
                }
            });

            /*
             * Starting a new siddhi worker for running antlr tasks
             * This is done to isolate the antlr tasks form the main js to solve RequireJS conflicts
             * Also this will enable the antlr tasks to run without blocking the UI thread
             */
            var siddhiWorker = new SiddhiWorker(new MessageHandler(self));

            /**
             * Returns the ace editor object
             * Can be used for getting the ace editor object and making custom changes
             */
            self.getAceEditorObject = function () {
                return aceEditor;
            };

            /**
             * Returns the content in the ace editor when the method is invoked
             *
             * @return {string} Content in the editor when the method is invoked
             */
            self.getContent = function () {
                return aceEditor.getValue();
            };

            /**
             * Sets the content in the ace editor
             *
             * @param {string} content Content to set into the ace editor
             */
            self.setContent = function (content) {
                aceEditor.setValue((content ? content : ""), 1);
            };

            /**
             * Dynamically select the completers suitable for current context
             *
             * @private
             */
            function adjustAutoCompletionHandlers() {
                // Selecting the completer for current context when auto complete event is fired
                // SiddhiCompleter needs to be the first completer in the list as it will update the snippets
                var completerList =
                    [self.completionEngine.SiddhiCompleter, self.completionEngine.SnippetCompleter];

                // Adding keyword completor if the cursor is not in front of dot or colon
                var objectNameRegex = new RegExp("[a-zA-Z_][a-zA-Z_0-9]*\\s*\\.\\s*$", "i");
                var namespaceRegex = new RegExp("[a-zA-Z_][a-zA-Z_0-9]*\\s*:\\s*$", "i");
                var singleLineCommentRegex = new RegExp("--(?:.(?!\n))*$");
                var blockCommentRegex = new RegExp("\\/\\*(?:(?:.|\n)(?!\\*\\/))*$");

                // Adding the keyword completor
                var editorText = aceEditor.getValue();
                if (!(objectNameRegex.test(editorText) || namespaceRegex.test(editorText) ||
                    singleLineCommentRegex.test(editorText) || blockCommentRegex.test(editorText))) {
                    // todo removing keyword suggestions
                    // completerList.push(aceExtLangTools.keyWordCompleter);
                }

                aceEditor.completers = completerList;
            }

            /**
             * Editor change handler
             *
             * @private
             */
            function editorChangeHandler() {
                // Clearing all errors before finding the errors again
                self.state.semanticErrorList = [];
                self.state.syntaxErrorList = [];

                siddhiWorker.onEditorChange(aceEditor.getValue().trim());
            }

            /**
             * Start the timer for checking the semantic errors
             * After the timer elapses if the user had not typed anything semantic errors will be checked using the server
             */
            self.startCheckForSemanticErrorsTimer = function () {
                if (config.realTimeValidation && self.state.syntaxErrorList.length == 0) {
                    // If there are no syntax errors and there is a change in parserTree
                    // check for semantic errors if there is no change in the query within 3sec period
                    // 3 seconds delay is added to avoid repeated server calls while user is typing the query.
                    setTimeout(function () {
                        if (Date.now() - self.state.lastEdit >= constants.SERVER_SIDE_VALIDATION_DELAY - 100) {
                            // Check for semantic errors by sending a validate request to the server
                            checkForSemanticErrors();
                        }
                    }, constants.SERVER_SIDE_VALIDATION_DELAY);
                }

                self.state.lastEdit = Date.now();         // Save user's last edit time
            };

            /**
             * This method send server calls to check the semantic errors
             * Also retrieves the missing completion engine data from the server if the execution plan is valid
             *
             * @private
             */
            function checkForSemanticErrors() {
                var lastFoundSemanticErrorLine = Number.MAX_SAFE_INTEGER;

                var editorText = aceEditor.getValue();
                // If the user has not typed anything after 3 seconds from his last change, then send the query for semantic check
                // check whether the query contains errors or not
                submitToServerForSemanticErrorCheck(
                    {
                        executionPlan: editorText,
                        missingStreams: self.completionEngine.incompleteData.streams,
                        missingInnerStreams: self.completionEngine.incompleteData.partitions
                    },
                    function (response) {
                        if (response.status == "SUCCESS") {
                            /*
                             * Execution plan is valid
                             */

                            // Populating the fetched data for incomplete data items into the completion engine's data
                            var streams = getStreamsFromStreamDefinitions(response.streams);
                            for (var streamName in streams) {
                                if (streams.hasOwnProperty(streamName)) {
                                    self.completionEngine.streamsList[streamName] = streams[streamName];
                                }
                            }

                            self.completionEngine.partitionsList = [];
                            for (var i = 0; i < response.innerStreams.length; i++) {
                                var innerStreams =
                                    getStreamsFromStreamDefinitions(response.innerStreams[i], true);
                                self.completionEngine.partitionsList.push(innerStreams);
                            }

                            // for (var i = 0; i < response.innerStreams.length; )

                            // Updating token tooltips
                            self.completionEngine.clearIncompleteDataLists();
                        } else {
                            /*
                             * Error found in execution plan
                             */

                            /*
                             * Send the query appending one statement after each request to identify the statement in which the error is at
                             * This is required since the siddhi engine desnt return the line number
                             */
                            var query = "";
                            for (var i = 0; i < self.completionEngine.statementsList.length; i++) {
                                if (self.completionEngine.statementsList[i].statement.substring(0, 2) != "\\*" &&
                                    self.completionEngine.statementsList[i].statement.substring(0, 2) != "--") {
                                    // Appending statements excepts comments
                                    query += self.completionEngine.statementsList[i].statement + "  \n";
                                    (function (line, query) {
                                        submitToServerForSemanticErrorCheck({
                                            executionPlan: query,
                                            missingStreams: []
                                        }, function (response) {
                                            if (line < lastFoundSemanticErrorLine &&
                                                response.status != "SUCCESS" &&
                                                Date.now() - self.state.lastEdit >=
                                                constants.SERVER_SIDE_VALIDATION_DELAY - 100) {
                                                // Update the semanticErrorList
                                                self.state.semanticErrorList = [({
                                                    row: line,
                                                    // Change attribute "text" to "html" if html is sent from server
                                                    text: utils.wordWrap(response.message, 100),
                                                    type: "error"
                                                })];

                                                // Update the state of the lastFoundSemanticErrorLine to stop sending another server call
                                                lastFoundSemanticErrorLine = line;

                                                // Show the errors in the ace editor gutter
                                                aceEditor.session.setAnnotations(
                                                    self.state.semanticErrorList
                                                        .concat(self.state.syntaxErrorList)
                                                );
                                            }
                                        });
                                    })(self.completionEngine.statementsList[i].line, query);

                                    if (self.completionEngine.statementsList[i].line > lastFoundSemanticErrorLine ||
                                        Date.now() - self.state.lastEdit <
                                        constants.SERVER_SIDE_VALIDATION_DELAY - 100) {
                                        break;
                                    }
                                }
                            }
                        }

                        siddhiWorker.generateTokenTooltips();
                    },
                    siddhiWorker.generateTokenTooltips
                );

                /**
                 * Get the streams list from the stream definitions list returned from the server
                 * This is used for transforming server's stream definitions to completion engine's stream data
                 *
                 * @param {object[]} streamDefinitionsList Stream definitions list returned from the server
                 * @param {boolean} [isInner] Boolean indicating whether the set of stream definitions are inner streams or not
                 * @return {object} Stream data extracted from the stream definitions
                 */
                function getStreamsFromStreamDefinitions(streamDefinitionsList, isInner) {
                    var streams = {};
                    for (var i = 0; i < streamDefinitionsList.length; i++) {
                        var streamDefinition = streamDefinitionsList[i];
                        var attributes = {};
                        for (var k = 0; k < streamDefinition.attributeList.length; k++) {
                            attributes[streamDefinition.attributeList[k].name] =
                                streamDefinition.attributeList[k].type;
                        }
                        streams[streamDefinitionsList[i].id] = {
                            attributes: attributes,
                            description: utils.generateDescriptionForStreamOrTable(
                                (isInner ? "Inner " : "") + "Stream",
                                streamDefinitionsList[i].id, attributes
                            )
                        };
                    }
                    return streams;
                }
            }

            /**
             * Submit the execution plan to server for semantic error checking
             * Also fetched the incomplete data from the server for the completion engine
             *
             * @private
             * @param {Object} data The execution plan and the missing data in a java script object
             * @param {function} callback Callback to be called after successful semantic error check
             * @param {function} [errorCallback] Callback to be called after errors in semantic error check
             */
            function submitToServerForSemanticErrorCheck(data, callback, errorCallback) {
                if (data.executionPlan == "") {
                    return;
                }
                $.ajax({
                    type: "POST",
                    url: constants.SERVER_URL + "validator",
                    data: JSON.stringify(data),
                    success: callback,
                    error: errorCallback
                });
            }

            return self;
        }

        /**
         * Siddhi Web Worker wrapper prototype
         * Handles all ANTLR related processing
         * Automatically starts up the web worker as well
         *
         * @param {MessageHandler} messageHandler Message handler object which will handle all incoming messages from the worker
         * @return {SiddhiWorker} Siddhi worker instance
         * @constructor
         */
        function SiddhiWorker(messageHandler) {
            var self = this;
            var worker;

            /**
             * Restart the web worker
             */
            self.restart = function () {
                if (worker) {
                    worker.terminate();
                }
                worker = new Worker("/editor/js/source-editor/antlr-worker.js");
                self.init();
            };

            /**
             * Initialize the web worker
             * Constants are passed into the web worker
             * Constants are passed in this way because some of the constants are generated values and the generation of which requires the window object
             */
            self.init = function () {
                worker.postMessage(JSON.stringify({
                    type: constants.worker.INIT,
                    data: constants
                }));

                // Add event receiver to listen to incoming messages from the web worker
                worker.addEventListener('message', function (event) {
                    messageHandler.handle(JSON.parse(event.data));
                });
            };

            /**
             * Run on editor's change
             * Send message to the worker to create the parse tree and generate completion engine data
             *
             * @param {string} editorText Text in the editor after the change
             */
            self.onEditorChange = function (editorText) {
                worker.postMessage(JSON.stringify({
                    type: constants.worker.EDITOR_CHANGE_EVENT,
                    data: editorText
                }));
            };

            /**
             * Send message to the worker to start generating token tool tips
             * The worker will recognize the token tooltip generation points and pass the the relevant data back so that they can be added
             */
            self.generateTokenTooltips = function () {
                worker.postMessage(JSON.stringify({
                    type: constants.worker.GENERATE_TOKEN_TOOLTIP
                }));
            };

            self.restart();     // Starts up the web worker
            return self;
        }

        /**
         * Message handler prototype
         * Message handler is used by the siddhi worker
         *
         * @param {object} editor The editor object
         * @return {MessageHandler} Message handler instance
         * @constructor
         */
        function MessageHandler(editor) {
            var handler = this;
            var messageHandlerMap = {};
            var tokenTooltipUpdater = new TokenTooltipUpdater(editor);

            // Generating the map from message types to handler functions
            messageHandlerMap[constants.worker.PARSE_TREE_GENERATION_COMPLETION] = updateSyntaxErrorList;
            messageHandlerMap[constants.worker.DATA_POPULATION_COMPLETION] = updateCompletionEngineData;
            messageHandlerMap[constants.worker.TOKEN_TOOLTIP_POINT_RECOGNITION_COMPLETION] = updateTokenTooltips;

            /**
             * Handle an incoming message from the web worker
             * @param {object} message
             */
            handler.handle = function (message) {
                messageHandlerMap[message.type](message.data);
            };

            /**
             * Update the list of syntax errors and add annotations
             *
             * @param {object} data Syntax errors data list
             */
            function updateSyntaxErrorList(data) {
                editor.state.syntaxErrorList = data;
                editor.getAceEditorObject().session.setAnnotations(data);
            }

            /**
             * Update the completion engine's data using the data generated by the worker
             *
             * @param {object} data Completion engine data generated by the worker
             */
            function updateCompletionEngineData(data) {
                editor.completionEngine.clearData();            // Clear the exiting completion engine data
                editor.completionEngine.streamsList = data.completionData.streamsList;
                editor.completionEngine.partitionsList = data.completionData.partitionsList;
                editor.completionEngine.eventTablesList = data.completionData.eventTablesList;
                editor.completionEngine.eventTriggersList = data.completionData.eventTriggersList;
                editor.completionEngine.evalScriptsList = data.completionData.evalScriptsList;
                editor.completionEngine.eventWindowsList = data.completionData.eventWindowsList;
                editor.completionEngine.updateDescriptions();
                editor.completionEngine.incompleteData = data.incompleteData;
                editor.completionEngine.statementsList = data.statementsList;
                editor.startCheckForSemanticErrorsTimer();
            }

            /**
             * Update the token tool tips using the data generated by the worker
             *
             * @param {object} data Token tool tip points and information for generating token tool tips
             */
            function updateTokenTooltips(data) {
                for (var i = 0; i < data.length; i++) {
                    var tooltipType = data[i].type;
                    var tooltipData = data[i].tooltipData;
                    var row = data[i].row;
                    var column = data[i].column;

                    tokenTooltipUpdater.update(tooltipType, tooltipData, row, column);
                }
            }

            return handler;
        }

        /**
         * Token tooltips generator prototype
         *
         * @param {object} editor The editor object
         * @return {TokenTooltipUpdater} Token tooltip generator instance
         * @constructor
         */
        function TokenTooltipUpdater(editor) {
            var updater = this;

            /**
             * Update the tooltip for the given type using the tool tip data
             *
             * @param {string} tooltipType Type of the tool tip to be updated
             * @param {object} tooltipData Tool tip data from which the tool tip will be generated
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            updater.update = function(tooltipType, tooltipData, row, column) {
                switch (tooltipType) {
                    case constants.FUNCTION_OPERATION:
                        updateFunctionOperationTooltip(tooltipData, row, column);
                        break;
                    case constants.SOURCE:
                        updateSourceTooltip(tooltipData, row, column);
                        break;
                    case constants.INNER_STREAMS:
                        updateInnerStreamTooltip(tooltipData, row, column);
                        break;
                    case constants.TRIGGERS:
                        updateTriggerTooltip(tooltipData, row, column);
                }
            };

            /**
             * Update the tooltip for a function operation
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the function operation name and the namespace
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateFunctionOperationTooltip (tooltipData, row, column) {
                var processorName = tooltipData.processorName;
                var namespace = tooltipData.namespace;

                var snippets;
                if (namespace) {
                    snippets = CompletionEngine.functionOperationSnippets.extensions[namespace];
                } else {
                    snippets = CompletionEngine.functionOperationSnippets.inBuilt;
                }

                // Adding WindowProcessor/StreamProcessor/Function/additional tool tip
                var description;
                if (snippets) {
                    if (snippets.windowProcessors && snippets.windowProcessors[processorName]) {
                        description = snippets.windowProcessors[processorName].description;
                    } else if (snippets.streamProcessors && snippets.streamProcessors[processorName]) {
                        description = snippets.streamProcessors[processorName].description;
                    } else if (snippets.functions && snippets.functions[processorName]) {
                        description = snippets.functions[processorName].description;
                    } else if (editor.completionEngine.evalScriptsList[processorName]) {
                        description = editor.completionEngine.evalScriptsList[processorName].description;
                    }
                }
                if (description) {
                    updateTokenTooltip(row, column, description);
                }
            }

            /**
             * Update the tooltip for a stream/table/window
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the source name
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateSourceTooltip(tooltipData, row, column) {
                var sourceName = tooltipData.sourceName;
                var source;

                if (editor.completionEngine.streamsList[sourceName]) {
                    source = editor.completionEngine.streamsList[sourceName];
                } else if (editor.completionEngine.eventTablesList[sourceName]) {
                    source = editor.completionEngine.eventTablesList[sourceName];
                } else if (editor.completionEngine.eventWindowsList[sourceName]) {
                    source = editor.completionEngine.eventWindowsList[sourceName];
                }

                if (source && source.description) {
                    updateTokenTooltip(row, column, source.description);
                }
            }

            /**
             * Update the tooltip for a inner stream
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the inner stream name and the partition number
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateInnerStreamTooltip(tooltipData, row, column) {
                var innerStreamName = tooltipData.sourceName;
                var partitionNumber = tooltipData.partitionNumber;

                if (editor.completionEngine.partitionsList[partitionNumber]) {
                    var innerStream =
                        editor.completionEngine.partitionsList[partitionNumber][innerStreamName];
                    if (innerStream && innerStream.description) {
                        updateTokenTooltip(row, column, innerStream.description);
                    }
                }
            }

            /**
             * Update the tooltip for a trigger
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the trigger name
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateTriggerTooltip(tooltipData, row, column) {
                var triggerName = tooltipData.triggerName;

                var trigger = editor.completionEngine.eventTriggersList[triggerName];
                if (trigger && trigger.description) {
                    updateTokenTooltip(row, column, trigger.description);
                }
            }

            /**
             * Add a tooltip at the position specified
             *
             * @param {int} tokenRow The row at which the target token is at
             * @param {int} tokenColumn The column at which the target token is at
             * @param {string} tooltip Tooltip to be added
             */
            function updateTokenTooltip(tokenRow, tokenColumn, tooltip) {
                var token = editor.getAceEditorObject().session.getTokenAt(tokenRow, tokenColumn);
                if (token) {
                    token.tooltip = tooltip;
                }
            }

            return updater;
        }

        return SiddhiEditor;
    });
