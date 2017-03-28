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
 * This is intended to be used as a web worker for running ANTLR related tasks
 * This is the starting point of the web worker and this loads all the relevant modules and runs the tasks
 */

"use strict";   // JS strict mode

/*
 * Importing the scripts required by the web worker
 * This is required since web worker runs isolated from everything else
 * constants.js is imported by the main html file
 */
importScripts("../../commons/lib/smoothie-require/require.js");

/*
 * This holds all SiddhiEditor related data that is shared among antlr web worker related js scripts
 * This is only added to the web worker's global space and not the main js global space
 */
var SiddhiEditor = {};

(function () {
    var antlrWalker;

    /**
     * Message handler object for handling the messages from the main js
     */
    var messageHandler = (function () {
        var handler = {};
        var messageHandlerMap = {
            INIT: initializeWorker
        };

        /**
         * Handle an incoming message from the main js
         *
         * @param {object} message Message received from the main js
         */
        handler.handle = function (message) {
            messageHandlerMap[message.type](message.data);
        };

        /**
         * Initialize the web worker
         *
         * @param {object} data Initialize data received. Should contain the constants
         */
        function initializeWorker(data) {
            SiddhiEditor.constants = data;      // This loads the constants defined in the constants.js
            antlrWalker = new ANTLRWalker();

            messageHandlerMap[SiddhiEditor.constants.worker.EDITOR_CHANGE_EVENT] = onEditorChange;
            messageHandlerMap[SiddhiEditor.constants.worker.GENERATE_TOKEN_TOOLTIP] = recognizeTokenTooltipPoints;
        }

        /**
         * Run on editor change tasks
         *
         * @param {object} data Editor change data received
         */
        function onEditorChange(data) {
            antlrWalker.onEditorChange(data);
        }

        /**
         * Run recognize token tool tip points tasks
         */
        function recognizeTokenTooltipPoints() {
            antlrWalker.recognizeTokenTooltipPoints();
        }

        return handler;
    })();

    /**
     * Handler object for passing messages to the main js
     */
    var renderer = (function () {
        var renderer = {};

        /**
         * Notify the main js that the parse tree walking for recognizing errors is complete
         * Syntax errors list will be passed with this message
         *
         * @param {object[]} errors List of syntax errors in the parse tree
         */
        renderer.notifyParseTreeWalkingCompletion = function (errors) {
            postMessage(JSON.stringify({
                type: SiddhiEditor.constants.worker.PARSE_TREE_GENERATION_COMPLETION,
                data: errors
            }));
        };

        /**
         * Notify the main js that the data population is complete
         * Completion engine's data, incomplete data sets and the statements list is passed with this message
         *
         * @param {object} completionData Data required by the completion engine
         * @param {object} incompleteData Data that the web worker failed to generate
         * @param {object[]} statementsList List of statements with their respective line numbers
         */
        renderer.notifyDataPopulationCompletion = function (completionData, incompleteData, statementsList) {
            postMessage(JSON.stringify({
                type: SiddhiEditor.constants.worker.DATA_POPULATION_COMPLETION,
                data: {
                    completionData: completionData,
                    incompleteData: incompleteData,
                    statementsList: statementsList
                }
            }));
        };

        /**
         * Notify the main js that the tooltip point recognition is complete
         * List of tool tip point data will be passed with the message
         *
         * @param {object[]} tooltipData Tooltip point data required for generating tooltips
         */
        renderer.notifyTokenTooltipPointRecognitionCompletion = function (tooltipData) {
            postMessage(JSON.stringify({
                type: SiddhiEditor.constants.worker.TOKEN_TOOLTIP_POINT_RECOGNITION_COMPLETION,
                data: tooltipData
            }));
        };

        return renderer;
    })();

    /*
     * Adding listener for listening to the messages sent by the main js
     */
    self.addEventListener('message', function (event) {
        messageHandler.handle(JSON.parse(event.data));
    });

    /**
     * ANTLR worker prototype
     * The instance created will handle all ANTLR related tasks
     *
     * @constructor
     */
    function ANTLRWalker() {
        var walker = this;
        var lastParseTree;

        /*
         * Variables used for storing temporary data
         */
        walker.syntaxErrorList = [];
        walker.completionData = {
            streamsList: {},
            partitionsList: [],
            eventTablesList: {},
            eventTriggersList: {},
            evalScriptsList: {},
            eventWindowsList: {}
        };
        walker.incompleteData = {
            streams: [],
            partitions: []
        };
        walker.statementsList = [];
        walker.tokenToolTipData = [];

        /**
         * Clear all the temporary data held after data population by the ANTLR walker
         */
        function clearCompletionEngineData() {
            walker.syntaxErrorList = [];
            walker.completionData = {
                streamsList: {},
                partitionsList: [],
                eventTablesList: {},
                eventTriggersList: {},
                evalScriptsList: {},
                eventWindowsList: {}
            };
            walker.incompleteData = {
                streams: [],
                partitions: []
            };
            walker.statementsList = [];
        }

        /**
         * Clear all the tooltip point data held by the ANTLR worker
         */
        function clearTokenTooltipData() {
            walker.tokenToolTipData = [];
        }

        /*
         * Loading ANTLR related modules
         */
        var antlr4 = require(SiddhiEditor.constants.antlr.INDEX);                                                                          // ANTLR4 JS runtime
        var SiddhiQLLexer = require(SiddhiEditor.constants.antlr.ROOT + SiddhiEditor.constants.antlr.SIDDHI_LEXER).SiddhiQLLexer;
        var SiddhiQLParser = require(SiddhiEditor.constants.antlr.ROOT + SiddhiEditor.constants.antlr.SIDDHI_PARSER).SiddhiQLParser;
        var DataPopulationListener = require(SiddhiEditor.constants.antlr.ROOT + SiddhiEditor.constants.antlr.SIDDHI_DATA_POPULATION_LISTENER).DataPopulationListener;
        var SyntaxErrorListener = require(SiddhiEditor.constants.antlr.ROOT + SiddhiEditor.constants.antlr.SYNTAX_ERROR_LISTENER).SyntaxErrorListener;
        var TokenTooltipPointRecognitionListener = require(SiddhiEditor.constants.antlr.ROOT + SiddhiEditor.constants.antlr.SIDDHI_TOKEN_TOOL_TIP_UPDATE_LISTENER).TokenTooltipPointRecognitionListener;

        /**
         * Run on editor change tasks
         * Creates the parse tree and walks it for recognizing syntax errors and completion engine's data
         * Syntax errors list will be passed to the main js after finding them
         * Completion Engine's data will be passed to the main js after finding them
         *
         * @param {string} editorText Text in the editor for which the parse tree will be generated
         */
        walker.onEditorChange = function (editorText) {
            // Following code segment parse the input query using antlr4's parser and lexer
            var errorListener = new SyntaxErrorListener(walker);
            var txt = new antlr4.InputStream(editorText);       // Input stream
            var lexer = new SiddhiQLLexer(txt);                 // Generating lexer
            lexer._listeners = [];
            lexer._listeners.push(errorListener);
            var tokens = new antlr4.CommonTokenStream(lexer);   // Generated a token stream
            var parser = new SiddhiQLParser(tokens);            // Using the token stream , generate the parser
            parser._listeners = [];
            parser._listeners.push(errorListener);
            parser.buildParseTrees = true;

            // Syntax errors in parsing are stored in  editor.state.syntaxErrorList
            lastParseTree = parser.parse();

            // Adding the syntax errors identified into the editor gutter
            renderer.notifyParseTreeWalkingCompletion(walker.syntaxErrorList);

            // Walking the parse tree to generate completion data
            var dataPopulationListener = new DataPopulationListener(walker);
            antlr4.tree.ParseTreeWalker.DEFAULT.walk(dataPopulationListener, lastParseTree);

            // Notify the main js and clear completion data
            renderer.notifyDataPopulationCompletion(walker.completionData, walker.incompleteData, walker.statementsList);
            clearCompletionEngineData();
        };

        /**
         * Recognize all the points at which the tooltips should be added
         * Walks the parse tree to recognize the points
         * Position of the tooltip, type of the tooltip and other data required by the tooltip will be passed back to the main js
         */
        walker.recognizeTokenTooltipPoints = function () {
            // Walking the parse tree to identify the token tooltip points
            // Actually adding the tooltips in done in the main.js
            var tokenTooltipPointRecognitionListener = new TokenTooltipPointRecognitionListener(walker);
            antlr4.tree.ParseTreeWalker.DEFAULT.walk(tokenTooltipPointRecognitionListener, lastParseTree);

            // Notify the main js and clear data
            renderer.notifyTokenTooltipPointRecognitionCompletion(walker.tokenToolTipData);
            clearTokenTooltipData();
        };

        walker.utils = (function () {
            var utils = {};

            /**
             * Get the text in the parse tree relevant for the ANTLR context provided
             *
             * @param ctx The context for which the text is returned
             * @return {string} The text relevant to the context provided
             */
            utils.getTextFromANTLRCtx = function (ctx) {
                return ctx.start.getInputStream().getText(ctx.start.start, ctx.stop.stop);
            };

            return utils;
        })();
    }
})();