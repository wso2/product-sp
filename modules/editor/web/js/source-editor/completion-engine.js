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

define(["ace/ace", "jquery", "./constants", "./utils", "ace/snippets", "ace/range", "ace/lib/lang"],
    function (ace, $, constants, utils, aceSnippetManager, aceRange, aceLang) {

        "use strict";   // JS strict mode

        /*
         * Loading ace modules
         */
        var aceModules = {
            snippetManager: aceSnippetManager.snippetManager,    // Required for changing the snippets used
            range: aceRange.Range,                            // Required for extracting part of the query
            lang: aceLang
        };

        /*
         * Suggestion lists used by the engine
         */
        var suggestions = {
            logicalOperatorList: ["and", "or", "not", "in", "is null"]
                .map(function (operator) {
                    return {
                        value: operator,
                        type: constants.typeToDisplayNameMap[constants.LOGICAL_OPERATORS]
                    };
                }),
            dataTypes: ["int", "long", "double", "float", "string", "bool", "object"]
                .map(function (dataType) {
                    return {
                        value: dataType,
                        type: constants.typeToDisplayNameMap[constants.DATA_TYPES]
                    };
                }),
            outputEventTypes: ["current", "all", "expired"]
                .map(function (eventType) {
                    return {value: eventType};
                }),
            timeValueTypes: ["years", "months", "weeks", "days", "hours", "minutes", "seconds", "milliseconds"]
                .map(function (timeValueType) {
                    return {value: timeValueType};
                })
        };

        /*
         * Regex strings used by the engine starts here
         */
        var regex = {};
        regex.comment = "(?:\\/\\*[^\\*]*\\*\\/)|(?:--.*\n)\\s*";
        regex.identifier = "[a-zA-Z_][a-zA-Z_0-9]*";
        regex.namespace = "(" + regex.identifier + ")\\s*:\\s*";
        regex.hash = "#\\s*";
        regex.comma = ",\\s*";
        regex.functionOperation = regex.identifier + "\\s*\\((?:(?:.(?!\\)))*.)?\\)";
        regex.dataTypes = suggestions.dataTypes.map(function (dataType) {
            return dataType.value;
        }).join("|");

        regex.query = {};

        regex.query.input = {};
        regex.query.input.windowKeywordAndDot = "window\\s*\\.\\s*";
        regex.query.input.sourceRegex = "((?:" + regex.hash + ")?" + regex.identifier + ")\\s*";
        regex.query.input.filterRegex = "\\[(?:(?:.(?!\\]))*.\\]|\\])\\s*";
        regex.query.input.streamProcessorRegex = regex.hash + "(?:" + regex.namespace + ")?" +
            regex.functionOperation + "\\s*";
        regex.query.input.windowRegex = regex.hash + regex.query.input.windowKeywordAndDot +
            "(?:" + regex.namespace + ")?" + regex.functionOperation + "\\s*";
        regex.query.input.sourceHandlersRegex = regex.query.input.filterRegex + "|" +
            regex.query.input.streamProcessorRegex;
        regex.query.input.standardStreamRegex = regex.query.input.sourceRegex +
            "(?:" + regex.query.input.sourceHandlersRegex + ")*" +
            "(?:" + regex.query.input.windowRegex + ")?(?:" + regex.query.input.sourceHandlersRegex + ")*";
        regex.query.input.streamReference = regex.query.input.standardStreamRegex +
            "\\s+as\\s+(" + regex.identifier + ")";
        regex.query.input.patternStreamRegex =
            "(" + regex.identifier + ")\\s*=\\s*(" + regex.identifier + ")\\s*";

        regex.query.selection = {};
        regex.query.selection.outputAttribute = "(?:(?:" + regex.identifier + "\\s*\\.\\s*)?" +
            regex.identifier + "|" + regex.functionOperation + ")" +
            "(?:\\s+as\\s+" + regex.identifier + "\\s*|\\s*)?";
        regex.query.selection.outputAttributesList = regex.query.selection.outputAttribute +
            "(?:" + regex.comma + regex.query.selection.outputAttribute + ")*";

        regex.query.outputRate = {};
        regex.query.outputRate.types = "(?:all|first|last)\\s+";

        regex.query.output = {};
        regex.query.output.eventTypes = "(?:current|all|expired)\\s+";
        /*
         * Regex strings used by the engine ends here
         */

        /*
         * Snippets to be used in the ace editor at the start of a statement
         */
        var generalInitialSnippets = aceModules.snippetManager.parseSnippetFile("#Define Statements\n" +
            "snippet define-Stream\n" +
            "\tdefine stream ${1:stream_name} (${2:attr1} ${3:Type1}, ${4:attN} ${5:TypeN});\n" +
            "snippet define-Table\n" +
            "\tdefine table ${1:table_name} (${2:attr1} ${3:Type1}, ${4:attN} ${5:TypeN});\n" +
            "snippet define-Window\n" +
            "\tdefine window ${1:window_name} (${2:attr1} ${3:Type1}, ${4:attN} ${5:TypeN}) ${6:window_type} ${7:output ${8:event_type} events};\n" +
            "snippet define-Trigger\n" +
            "\tdefine trigger ${1:trigger_name} at ${2:time};\n" +
            "snippet define-Function\n" +
            "\tdefine function ${1:function_name}[${2:lang_name}] return ${3:return_type} { \n" +
            "\t\t${4:function_body} \n" +
            "\t};\n" +
            "snippet annotation-IndexedBy\n" +
            "\t@IndexedBy('${1:attribute_name}')\n" +
            "snippet annotation-PlanName\n" +
            "\t@Plan:name(\"${1:Plan_Name}\")\n" +
            "snippet annotation-PlanDesc\n" +
            "\t@Plan:Description(\"${1:Plan_Description}\")\n" +
            "snippet annotation-PlanStatistics\n" +
            "\t@Plan:Statistics(\"${1:Plan_Statistics}\")\n" +
            "snippet annotation-PlanTrace\n" +
            "\t@Plan:Trace(\"${1:Plan_Trace}\")\n" +
            "snippet annotation-ImportStream\n" +
            "\t@Import(\"${1:Stream_ID}\")\n" +
            "snippet annotation-ExportStream\n" +
            "\t@Export(\"${1:Stream_ID}\")\n" +
            "snippet annotation-Info\n" +
            "\t@info(name = \"${1:Stream_ID}\")\n" +
            "snippet annotation-Config\n" +
            "\t@config(async = \'true\')\n" +
            "snippet partition\n" +
            "\tpartition with (${1:attribute_name} of ${2:stream_name}, ${3:attribute2_name} of ${4:stream2_name})\n" +
            "\tbegin\n" +
            "\t\t${5:queries}\n" +
            "\tend;\n" +
            "snippet annotation-Source\n" +
            "\t@source(type='${1:source_type}', ${2:static_option_key}='${3:static_option_value}', ${4:dynamic_option_key}='{{${5:dynamic_option_value}}}',\n" +
            "\t\t@map(type='${6:map_type}', ${7:static_option_key}='${8:static_option_value}', ${9:dynamic_option_key}='{{${10:dynamic_option_value}}}',\n" +
            "\t\t\t@attributes( '${11:attribute_mapping_1}', '${12:attribute_mapping_N}')\n" +
            "\t\t)\n" +
            "\t)\n" +
            "\tdefine stream ${13:stream_name} (${14:attribute1} ${15:Type1}, ${16:attributeN} ${17:TypeN});\n" +
            "snippet annotation-Sink\n" +
            "\t@sink(type='${1:sink_type}', ${2:static_option_key}='${3:static_option_value}', ${4:dynamic_option_key}='{{${5:dynamic_option_value}}}',\n" +
            "\t\t@map(type='${6:map_type}', ${7:static_option_key}='${8:static_option_value}', ${9:dynamic_option_key}='{{${10:dynamic_option_value}}}',\n" +
            "\t\t\t@payload( '${11:payload_mapping}')\n" +
            "\t\t)\n" +
            "\t)\n" +
            "\tdefine stream ${12:stream_name} (${13:attribute1} ${14:Type1}, ${15:attributeN} ${16:TypeN});\n"
        );

        /*
         * Snippets to be used in the ace editor at the start of a statement and at the start of a query inside partitions
         */
        var queryInitialSnippets = aceModules.snippetManager.parseSnippetFile(
            "snippet query-Filter\n" +
            "\tfrom ${1:stream_name}[${2:filter_condition}]\n" +
            "\tselect ${3:attribute1}, ${4:attribute2}\n" +
            "\tinsert into ${5:output_stream}\n" +
            "snippet query-Window\n" +
            "\tfrom ${1:stream_name}#window.${2:namespace}:${3:window_name}(${4:args})\n" +
            "\tselect ${5:attribute1}, ${6:attribute2}\n" +
            "\tinsert into ${7:output_stream}\n" +
            "snippet query-WindowFilter\n" +
            "\tfrom ${1:stream_name}[${2:filter_condition}]#window.${3:namespace}:${4:window_name}(${5:args})\n" +
            "\tselect ${6:attribute1} , ${7:attribute2}\n" +
            "\tinsert into ${8:output_stream}\n" +
            "snippet query-Join\n" +
            "\tfrom ${1:stream_name}[${2:filter_condition}]#window.${3:window_name}(${4:args}) as ${5:reference}\n" +
            "\t\tjoin ${6:stream_name}[${7:filter_condition}]#window.${8:window_name}(${9:args}) as ${10:reference}\n" +
            "\t\ton ${11:join_condition}\n" +
            "\t\twithin ${12: time_gap}\n" +
            "\tselect ${13:attribute1}, ${14:attribute2}\n" +
            "\tinsert into ${15:output_stream}\n" +
            "snippet query-Pattern\n" +
            "\tfrom every ${1:stream_reference}=${2:stream_name}[${3:filter_condition}] -> \n" +
            "\t\tevery ${4:stream_reference2}=${5:stream_name2}[${6:filter_condition2}]\n" +
            "\t\twithin ${7: time_gap}\n" +
            "\tselect ${8:stream_reference}.${9:attribute1}, ${10:stream_reference}.${11:attribute1}\n" +
            "\tinsert into ${12:output_stream}\n" +
            "snippet query\n" +
            "\tfrom ${1:stream_name}\n" +
            "\tselect ${2:attribute1} , ${3:attribute2}\n" +
            "\tinsert into ${4:output_stream}\n"
        );

        /*
         *   mainRuleBase has a list of regular expressions to identify the different contexts and appropriate handlers to generate context aware suggestions.
         *
         *   RULES HAVE DIFFERENT FORMAT
         *   ---------------------------
         *
         *          if the suggestions list is a simple keyword list (ex : suggestions list after 'define' keyword)
         *          ------------------------------------------------
         *                 {
         *                    regex : "regularExpression",
         *                    handler : [
         *                      {value: "list", caption: "caption", description:"description", type: "type"},
         *                      {value: "of", caption: "caption", description:"of", type: "type"},
         *                      {value: "keywords", caption: "caption", description:"keywords", type: "type"}
         *                    ]
         *                 }
         *          "description", "caption", "type" attributes are optional.
         *
         *          if the suggestions list dynamically calculated (Ex : suggestions list after the 'from' keyword)
         *          ----------------------------------------------
         *                 {
         *                    regex : "regularExpression",
         *                    handler : "$FunctionHandler"     // CONVENTION : function name is started with $ mark
         *                 }
         *
         */
        var mainRuleBase = [
            // Annotation rule
            {
                regex: "@[^\\(]*$",
                handler: [
                    'Plan:name(\'Name of the plan\')',
                    'Plan:description(\'Description of the plan\')',
                    'Plan:trace(\'true|false\')',
                    'Plan:statistics(\'true|false\')',
                    'Import(\'StreamName\')',
                    'Export(\'StreamName\')',
                    'Config(async=true)',
                    'Config(async=true)',
                    'source(type=\'source_type\', option_key=\'option_value\', ...)',
                    'sink(type=\'sink_type\', option_key=\'option_value\', ...)',
                    'map(type=\'map_type\', option_key=\'option_value\', ...)',
                    'attributes(\'attribute_mapping_a\', \'attribute_mapping_b\')',
                    'payload(type=\'payload_string\')',
                    'info(name=\'stream_id\')'
                ]
            },

            /*
             * Define statement rules starts here
             */
            {
                regex: "define\\s+[^\\s]*$",
                handler: ["stream", "table", "trigger", "function", "window"]
            },
            {
                regex: "define\\s+(stream|table|window)\\s+" + regex.identifier + "\\s*\\((\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*,)*\\s*" +
                regex.identifier + "\\s+[^\\s" +
                "\\),]*$",
                handler: suggestions.dataTypes
            },
            {
                regex: "define\\s+trigger\\s+" + regex.identifier + "\\s+[^\\s]*$",
                handler: ["at"]
            },
            {
                regex: "define\\s+trigger\\s+" + regex.identifier + "\\s+at\\s+[^\\s]*$",
                handler: ["every"]
            },
            {
                regex: "define\\s+function\\s+" + regex.identifier + "\\s*\\[[^\\s]*(?!\\])$",
                handler: ["JavaScript", "R", "Scala"]
            },
            {
                regex: "define\\s+function\\s+" + regex.identifier + "\\s*\\[\\s*[^\\s]*\\s*\\]\\s+[^\\s]*$",
                handler: ["return"]
            },
            {
                regex: "define\\s+function\\s+" + regex.identifier + "\\s*\\[\\s*[^\\s]*\\s*\\]\\s+return\\s+[^\\s]*$",
                handler: suggestions.dataTypes
            },
            {
                regex: "define\\s+window\\s+" + regex.identifier + "\\s*\\((\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*,)*\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*" +
                "\\)\\s+[a-zA-Z_0-9]*$",
                handler: "$defineWindowStatementWindowType"
            },
            {
                regex: "define\\s+window\\s+(" + regex.identifier + ")\\s*\\((\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*,)*\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*" +
                "\\)\\s+(" + regex.identifier + ":)?" + regex.identifier + "\\s*" +
                "\\((\\s*" + regex.identifier + "\\s*,)*\\s*[^\\s\\)]*$",
                handler: "$defineWindowStatementWindowParameters"
            },
            {
                regex: "define\\s+window\\s+" + regex.identifier + "\\s*\\((\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*,)*\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*" +
                "\\)\\s+(" + regex.identifier + ":)?" + regex.identifier + "\\s*\\(.*\\)\\s+[^\\s]*$",
                handler: ["output"]
            },
            {
                regex: "define\\s+window\\s+" + regex.identifier + "\\s*\\((\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*,)*\\s*" +
                regex.identifier + "\\s+(" + regex.dataTypes + ")\\s*" +
                "\\)\\s+(" + regex.identifier + ":)?" + regex.identifier + "\\s*\\(.*\\)\\s+output\\s+[^\\s]*$",
                handler: suggestions.outputEventTypes.map(function (completion) {
                    return Object.assign({}, completion, {
                        value: completion.value + " events;"
                    });
                })
            },
            /*
             * Define statement rules ends here
             */

            // Query rule
            {
                regex: "(from)\\s+((?:.(?!select|group\\s+by|having|output|insert|delete|update))*)" +
                "(?:\\s+(select)\\s+((?:.(?!group\\s+by|having|output|insert|delete|update))*)" +
                "(?:\\s+(group\\s+by)\\s+((?:.(?!having|output|insert|delete|update))*))?" +
                "(?:\\s+(having)\\s+((?:.(?!output|insert|delete|update))*))?" +
                ")?" +
                "(?:\\s+(output)\\s+((?:.(?!insert|delete|update))*))?" +
                "(?:\\s+((?:insert\\s+overwrite|delete|update|insert))\\s+((?:.(?!;))*.?))?$",
                handler: "$query"
            },

            /*
             * Partition rules starts here
             */
            {
                regex: "partition\\s+[a-zA-Z_0-9]*$",
                handler: ["with"]
            },
            {
                regex: "partition\\s+with\\s*((?:.(?!\\s+begin))*.)\\s*(?:(begin))?(?:\\s+((?:.(?!\\s+end))*))?$",
                handler: "$partition"
            }
            /*
             * Partition rules ends here
             */
        ];

        /**
         * Siddhi Editor Completion Engine prototype constructor
         *
         * @constructor
         */
        function CompletionEngine() {
            var self = this;

            /*
             * List of streams defined
             *
             * self.streamList = {
             *      streamName: {
             *          attributes: {
             *              attribute1: "dataType1",
             *              attribute2: "dataType2"
             *          },
             *          description: "description to be shown in the tooltip"
             *      },
             *      ...
             * }
             */
            self.streamsList = {};

            /*
             * Partition list with each partition containing the inner streams
             * This is updated after the stream definitions are received from the server since inner streams are not defined
             *
             * self.partitionsList = [
             *      {
             *          partition1InnerStream1: {
             *              attributes: {
             *                  attribute1: "dataType1",
             *                  attribute2: "dataType2",
             *              },
             *              description: "description to be shown in the tooltip"
             *          },
             *          partition1InnerStream2: {
             *              attributes: {
             *                  attribute3: "dataType1",
             *                  attribute4: "dataType2"
             *              },
             *              description: "description to be shown in the tooltip"
             *          },
             *          ...
             *      },
             *      {
             *          partition2InnerStream1: {
             *              attributes: {
             *                  attribute5: "dataType1",
             *                  attribute6: "dataType2"
             *              },
             *              description: "description to be shown in the tooltip"
             *          },
             *          ...
             *      },
             *      ...
             * ]
             */
            self.partitionsList = [];

            /*
             * List of tables defined
             *
             * self.eventTablesList = {
             *      eventTableName: {
             *          attributes: {
             *              attribute1: "dataType1",
             *              attribute2: "dataType2"
             *          },
             *          description: "description to be shown in the tooltip"
             *      },
             *      ...
             * }
             */
            self.eventTablesList = {};

            /*
             * List of triggers defined
             *
             * self.eventTriggersList = {
             *      eventTriggerName: {
             *          type: "Time Value | Cron Expression",
             *          time: *The cron expression or the time value,
             *          description: "description to be shown in the tooltip"
             *      },
             *      ...
             * }
             */
            self.eventTriggersList = {};

            /*
             * List of functions defined
             *
             * self.evalScriptsList = {
             *      evalScriptName: {
             *          language: "Language Type",
             *          returnType: “dataType”
             *          functionBody: "Function body inside the braces",
             *          description: "description to be shown in the tooltip"
             *      },
             *      ...
             * }
             */
            self.evalScriptsList = {};

            /*
             * List of windows defined
             *
             * self.eventWindowsList = {
             *      eventWindowName: {
             *          attributes: {
             *              attribute1: dataType1,
             *              attribute2: dataType2
             *          },
             *          functionOperation: "windowName",
             *          description: "description to be shown in the tooltip"
             *      },
             *      ...
             * }
             */
            self.eventWindowsList = {};

            /*
             * Incomplete data which will be retrieved from the server along with the validation
             * Information about these data items will be fetched from the server upon validation
             * siddhi ExecutionPlanRuntime generates the data
             */
            self.incompleteData = {
                streams: [],    // Array of stream names of which definitions are missing
                partitions: []  // 2D array of inner stream names with the partition number as the index in the outer array
            };

            /**
             * Clear the completion engine data
             * Includes clearing the incomplete data lists
             * This does not include clearing partitions list since it is completely updated using the server
             */
            self.clearData = function () {
                self.streamsList = {};
                self.partitionsList = [];
                self.eventTablesList = {};
                self.eventTriggersList = {};
                self.evalScriptsList = {};
                self.eventWindowsList = {};
                self.clearIncompleteDataLists();
            };

            /**
             * Clear the incomplete data lists
             */
            self.clearIncompleteDataLists = function () {
                for (var incompleteDataSet in self.incompleteData) {
                    if (self.incompleteData.hasOwnProperty(incompleteDataSet)) {
                        self.incompleteData[incompleteDataSet] = [];
                    }
                }
            };

            /**
             * Update the descriptions in the completion data lists
             */
            self.updateDescriptions = function () {
                // Updating stream descriptions
                for (var stream in self.streamsList) {
                    if (self.streamsList.hasOwnProperty(stream)) {
                        self.streamsList[stream].description = utils.generateDescriptionForStreamOrTable(
                            "Stream", stream, self.streamsList[stream].attributes
                        );
                    }
                }

                // Updating inner stream descriptions
                for (var i = 0; i < self.partitionsList.length; i++) {
                    var partition = self.partitionsList[i];
                    for (var innerStream in partition) {
                        if (partition.hasOwnProperty(innerStream)) {
                            partition[innerStream].description = utils.generateDescriptionForStreamOrTable(
                                "Inner Stream", innerStream, partition[innerStream].attributes
                            );
                        }
                    }
                }

                // Updating event table descriptions
                for (var eventTable in self.eventTablesList) {
                    if (self.eventTablesList.hasOwnProperty(eventTable)) {
                        self.eventTablesList[eventTable].description = utils.generateDescriptionForStreamOrTable(
                            "Event Table", eventTable, self.eventTablesList[eventTable].attributes
                        );
                    }
                }

                // Updating event trigger descriptions
                for (var eventTrigger in self.eventTriggersList) {
                    if (self.eventTriggersList.hasOwnProperty(eventTrigger)) {
                        self.eventTriggersList[eventTrigger].description = utils.generateDescriptionForTrigger(
                            eventTrigger, self.eventTriggersList[eventTrigger]
                        );
                    }
                }

                // Updating event eval script descriptions
                for (var evalScript in self.evalScriptsList) {
                    if (self.evalScriptsList.hasOwnProperty(evalScript)) {
                        self.evalScriptsList[evalScript].description = utils.generateDescriptionForEvalScript(
                            evalScript, self.evalScriptsList[evalScript]
                        );
                    }
                }

                // Updating event eval script descriptions
                for (var eventWindow in self.eventWindowsList) {
                    if (self.eventWindowsList.hasOwnProperty(eventWindow)) {
                        self.eventWindowsList[eventWindow].description = utils.generateDescriptionForWindow(
                            eventWindow, self.eventWindowsList[eventWindow]
                        );
                    }
                }
            };

            /*
             * CompletionEngine.completionsList is the current suggestions list
             * This should not be directly updated. Use addCompletions() function instead
             * This is an array of objects with following format
             *
             * completionsList = {
             *       caption: "suggestion name",        // caption to be show in the tooltip
             *       value: "suggestion value",         // text added to the editor when the completion is selected
             *       score: 2,                          // priority
             *       description: "description"         // description shown in the tooltip
             *       meta: "suggestion type"            // type shown in the popup
             * }
             */
            self.completionsList = [];

            /*
             * Snippets that had been added to the SnippetManager
             * This is stored so that they can be unregistered when the next suggestion need to be calculated
             */
            self.suggestedSnippets = [];

            /*
             * List of statements in the execution plan
             * Created by the data population listener while walking the parse tree
             */
            self.statementsList = [];

            /*
             * SiddhiCompleter provides language specific suggestions
             * use addSnippets() function to add snippets to the SnippetCompleter
             */
            self.SiddhiCompleter = {
                getCompletions: function (editor, session, pos, prefix, callback) {
                    // Calculate the suggestions list for current context
                    // context-handler functions will be updated the the worldList based on the context around the cursor position
                    self.calculateCompletions(editor);

                    // This completer will be using the completionsList array
                    callback(null, self.completionsList);
                }
            };

            /*
             * SnippetCompleter provides language specific snippets
             */
            self.SnippetCompleter = {
                getCompletions: function (editor, session, pos, prefix, callback) {
                    var snippetMap = aceModules.snippetManager.snippetMap;
                    var completions = [];
                    aceModules.snippetManager.getActiveScopes(editor).forEach(function (scope) {
                        var snippets = snippetMap[scope] || [];
                        for (var i = snippets.length; i--;) {
                            var s = snippets[i];
                            var caption = s.name || s.tabTrigger;
                            if (!caption) {
                                continue;
                            }
                            completions.push({
                                caption: caption,
                                snippet: s.content,
                                meta: s.tabTrigger && !s.name ? s.tabTrigger + "\u21E5 " : (
                                    s.type != undefined ?
                                        s.type :
                                        constants.typeToDisplayNameMap[constants.SNIPPETS]
                                ),
                                docHTML: s.description,
                                type: (
                                    s.type != undefined ?
                                        s.type :
                                        constants.typeToDisplayNameMap[constants.SNIPPETS]
                                )
                            });
                        }
                    }, this);
                    callback(null, completions);
                },
                getDocTooltip: function (item) {
                    if (item.type == constants.typeToDisplayNameMap[constants.SNIPPETS] && !item.docHTML) {
                        item.docHTML =
                            "<div><strong>" + aceModules.lang.escapeHTML(item.caption) + "</strong>" +
                            "<p>" + aceModules.lang.escapeHTML(item.snippet) + "</p></div>";
                    }
                }
            };

            /**
             * Calculate the list of suggestions based on the context around the cursor position
             *
             * @param {Object} editor ace editor instance
             */
            self.calculateCompletions = function (editor) {
                var cursorPosition = editor.getCursorPosition();

                // Getting the last statement from the statements list
                var lastStatement = self.statementsList[0];
                for (var i = 0; i < self.statementsList.length; i++) {
                    if (self.statementsList[i].line > cursorPosition.row) {
                        break;
                    } else {
                        lastStatement = self.statementsList[i];
                    }
                }

                // Getting the editor text from the start of the last statement before the cursor to the cursor position
                var editorText = editor.session.doc.getTextRange(aceModules.range.fromPoints({
                    row: (lastStatement ? lastStatement.line : 0),
                    column: 0
                }, cursorPosition));

                // Removing content not relevant to the completion engine
                editorText = editorText.replace(new RegExp(regex.comment, "ig"), "");       // Removing comments
                editorText = editorText.replace(/\s+/g, " ");           // Replacing all spaces with single white spaces

                // Clear the suggestion lists
                // Clear the previous snippet suggestions
                aceModules.snippetManager.unregister(self.suggestedSnippets, constants.SNIPPET_SIDDHI_CONTEXT);
                self.suggestedSnippets = [];

                self.completionsList = [];

                var editorTextStatements = editorText.split(";"); // If the last statement is a complete statement this step is important
                if (/^\s*(?:@(?:.(?!\)))*.\)\s*)?[a-zA-Z_0-9]*$/i.test(editorTextStatements[editorTextStatements.length - 1])) {
                    self.$startOfStatement();
                    aceModules.snippetManager.register(
                        generalInitialSnippets.concat(queryInitialSnippets),
                        constants.SNIPPET_SIDDHI_CONTEXT
                    );
                } else {
                    aceModules.snippetManager.unregister(
                        generalInitialSnippets.concat(queryInitialSnippets),
                        constants.SNIPPET_SIDDHI_CONTEXT
                    );
                }

                // Finding the relevant rule from the main rule base
                for (i = 0; i < mainRuleBase.length; i++) {
                    var ruleRegex = new RegExp(mainRuleBase[i].regex, "i");
                    if (ruleRegex.test(editorText)) {
                        if (mainRuleBase[i].handler.__proto__.constructor === Array) {
                            addCompletions(mainRuleBase[i].handler.map(function (completion) {
                                if (typeof completion == "string") {
                                    completion = {value: completion + " "};
                                }
                                return completion;
                            }));
                        } else {
                            self[mainRuleBase[i].handler].call(this,
                                // Regex results from the main rule base regexp matching
                                ruleRegex.exec(editorText),

                                // Full editor text before cursor
                                editor.session.doc.getTextRange(aceModules.range.fromPoints({
                                    row: 0,
                                    column: 0
                                }, cursorPosition))
                            );
                        }
                        return;
                    }
                }
            };

            /*
             * Suggestion Handler functions starts here
             */

            /**
             * Load the initial suggestions list
             */
            self.$startOfStatement = function () {
                addCompletions(["define", "from", "partition", "@"].map(function (completion) {
                    return {value: completion + " "};
                }));
            };

            /**
             * Load in-built window names for the define window statement
             */
            self.$defineWindowStatementWindowType = function () {
                addSnippets(getInBuiltWindowProcessors());
            };

            /**
             * Load attribute names as completions for the define window statement window's parameters
             *
             * @param {string[]} regexResults Regex results from the regex test in the main rule base matching
             */
            self.$defineWindowStatementWindowParameters = function (regexResults) {
                var window = regexResults[1];
                if (self.eventWindowsList[window].attributes) {
                    addCompletions(Object.keys(self.eventWindowsList[window].attributes).map(function (attribute) {
                        return {
                            value: attribute,
                            type: constants.typeToDisplayNameMap[constants.ATTRIBUTES]
                        }
                    }));
                }
            };

            /**
             * Load completions for queries
             * Regex results contains regex result groups for different parts of the query; input, select, group by, having, output rate, output
             * The relevant part of the query the user is in will be tested again using regexps
             *
             * @param {string[]} regexResults Regex results from the regex test in the main rule base matching
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            self.$query = function (regexResults, fullEditorText) {
                // Find the part of the query in which the cursor is at
                for (var i = regexResults.length - 1; i > 0; i--) {
                    if (regexResults[i] != undefined) {
                        break;
                    }
                }
                switch (regexResults[i - 1]) {
                    case "from":
                        handleQueryInputSuggestions(regexResults, fullEditorText);
                        break;
                    case "select":
                        handleQuerySelectionSuggestions(regexResults, fullEditorText);
                        break;
                    case "group by":
                        handleGroupBySuggestions(regexResults, fullEditorText);
                        break;
                    case "having":
                        handleHavingSuggestions(regexResults, fullEditorText);
                        break;
                    case "output":
                        handleQueryOutputRateSuggestions(regexResults);
                        break;
                    case "insert":
                        handleQueryInsertIntoSuggestions(regexResults, fullEditorText);
                        break;
                    case "insert overwrite":
                    case "delete":
                    case "update":
                        handleQueryInsertOverwriteDeleteUpdateSuggestions(regexResults);
                        break;
                    default:
                }
            };

            /**
             * Handle the query input suggestions for the query
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            function handleQueryInputSuggestions(regexResults, fullEditorText) {
                var queryInput = regexResults[2];

                // Regexps used for identifying the suggestions
                var sourceSuggestionsRegex = new RegExp("(?:" +
                    "^(#\\s*)?[a-zA-Z_0-9]*|" +                             // Source name at the start of query input
                    "\\s+join\\s+(#\\s*)?[a-zA-Z_0-9]*|" +                  // Source name after "join" keyword
                    regex.identifier + "\\s*=\\s*(#)?\\s*[a-zA-Z_0-9]*" +   // Source name after "=" in patterns
                    ")$", "i");
                var afterHashSuggestionsRegex = new RegExp(regex.query.input.standardStreamRegex + regex.hash +
                    "[a-zA-Z_0-9]*$", "i");
                var streamProcessorExtensionSuggestionsRegex =
                    new RegExp(regex.query.input.standardStreamRegex + regex.hash +
                        regex.namespace + "[a-zA-Z_0-9]*$", "i");
                var windowSuggestionsRegex = new RegExp(regex.query.input.standardStreamRegex + regex.hash +
                    regex.query.input.windowKeywordAndDot + "[a-zA-Z_0-9]*$", "i");
                var windowExtensionSuggestionsRegex = new RegExp(regex.query.input.standardStreamRegex +
                    regex.hash + regex.query.input.windowKeywordAndDot + regex.namespace +
                    "[a-zA-Z_0-9]*$", "i");
                var windowAndStreamProcessorParameterSuggestionsRegex = new RegExp(
                    regex.query.input.standardStreamRegex + regex.hash +
                    "(?:" + regex.query.input.windowKeywordAndDot + ")?" +
                    "(?:" + regex.namespace + ")?" +
                    regex.identifier + "\\s*\\([^\\)]*$", "i");
                var patternQueryFilterSuggestionsRegex = new RegExp(regex.query.input.patternStreamRegex +
                    "\\[(?:.(?!\\]))*$", "i");
                var nonPatternQueryFilterSuggestionsRegex = new RegExp(regex.query.input.standardStreamRegex +
                    "\\[(?:.(?!\\]))*$", "i");
                var afterStreamSuggestionsRegex =
                    new RegExp(regex.query.input.standardStreamRegex + "\\s+[^\\[#]*$", "i");
                var afterUnidirectionalKeywordSuggestionsRegex = new RegExp(
                    regex.query.input.standardStreamRegex + "\\s+unidirectional\\s+[a-zA-Z_0-9]*$", "i"
                );
                var afterOnKeywordSuggestionsRegex = new RegExp("\\s+on\\s+(?:.(?!\\s+within))*$", "i");
                var afterWithinKeywordSuggestionsRegex = new RegExp("\\s+within\\s+" +
                    "(?:.(?!select|group\\s+by|having|output|insert|delete|update))*$", "i");
                var everyKeywordSuggestionsRegex = new RegExp("->\\s*[a-zA-Z_0-9]*$", "i");

                // Testing to find the relevant suggestion
                if (sourceSuggestionsRegex.test(queryInput)) {
                    // Adding streams, inner streams ,event tables, event triggers, event windows
                    var isInner = sourceSuggestionsRegex.exec(queryInput)[1] == "#";
                    // Adding stream names if hash is not present: Inner stream and normal streams
                    if (!isInner) {
                        addCompletions(Object.keys(self.streamsList).map(function (stream) {
                            return {
                                value: stream,
                                type: constants.typeToDisplayNameMap[constants.STREAMS],
                                description: self.streamsList[stream].description,
                                priority: 6
                            }
                        }));
                    }
                    // Adding inner stream if # is present in the source name
                    if (isInsidePartition(regexResults)) {
                        var partitionNumber = getTheCurrentPartitionIndex(fullEditorText);
                        if (self.partitionsList[partitionNumber]) {
                            // Adding inner stream completions relevant to the correct partition
                            addCompletions(Object.keys(self.partitionsList[partitionNumber])
                                .map(function (innerStream) {
                                        return {
                                            value: (isInner ? innerStream.substring(1) : innerStream),
                                            type: constants.typeToDisplayNameMap[constants.INNER_STREAMS],
                                            description: self.partitionsList[partitionNumber][innerStream].description,
                                            priority: 6
                                        }
                                    }
                                ));
                        }
                    }
                    addCompletions(Object.keys(self.eventTablesList).map(function (table) {
                        return {
                            value: table,
                            type: constants.typeToDisplayNameMap[constants.EVENT_TABLES],
                            description: self.eventTablesList[table].description,
                            priority: 5
                        }
                    }));
                    addCompletions(Object.keys(self.eventWindowsList).map(function (window) {
                        return {
                            value: window,
                            type: constants.typeToDisplayNameMap[constants.WINDOWS],
                            description: self.eventWindowsList[window].description,
                            priority: 4
                        }
                    }));
                    addCompletions(Object.keys(self.eventTriggersList).map(function (trigger) {
                        return {
                            value: trigger,
                            type: constants.typeToDisplayNameMap[constants.TRIGGERS],
                            description: self.eventTriggersList[trigger].description,
                            priority: 3
                        }
                    }));
                    addCompletions({value: "every ", priority: 2});     // every keyword for patterns
                } else if (streamProcessorExtensionSuggestionsRegex.test(queryInput)) {
                    // stream processor extension suggestions after a namespace and colon
                    var namespace = streamProcessorExtensionSuggestionsRegex.exec(queryInput)[1].trim();
                    addSnippets(getExtensionStreamProcessors(namespace));
                } else if (windowSuggestionsRegex.test(queryInput)) {
                    // Add inbuilt windows, extension namespaces after hash + window + dot
                    addSnippets(getInBuiltWindowProcessors());
                    addSnippets(getExtensionNamesSpaces([constants.WINDOW_PROCESSORS])
                        .map(function (windowProcessor) {
                                return Object.assign({}, windowProcessor, {
                                    caption: windowProcessor.value,
                                    value: windowProcessor.value + ":"
                                });
                            }
                        ));
                } else if (windowExtensionSuggestionsRegex.test(queryInput)) {
                    // Add extension namespace names after hash + window + dot + namespace + colon
                    addSnippets(getExtensionWindowProcessors(
                        windowExtensionSuggestionsRegex.exec(queryInput)[1].trim()
                    ));
                } else if (windowAndStreamProcessorParameterSuggestionsRegex.test(queryInput)) {
                    // Add source attributes for parameters for stream processors and windows
                    addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(regexResults, fullEditorText, {
                        name: windowAndStreamProcessorParameterSuggestionsRegex.exec(queryInput)[1].trim()
                    }, [constants.STREAMS, constants.WINDOWS]));
                } else if (afterUnidirectionalKeywordSuggestionsRegex.test(queryInput)) {
                    // Add keywords after the unidirectional keyword
                    addCompletions(["join", "on", "within"].map(function (suggestion) {
                        return {
                            caption: suggestion, value: suggestion + " "
                        };
                    }));
                } else if (afterOnKeywordSuggestionsRegex.test(queryInput)) {
                    // Add suggestions after the on keyword in a join query
                    addAttributesOfSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 4, 3,
                        [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.TRIGGERS]
                    );
                    addCompletions(suggestions.logicalOperatorList.map(function (operator) {
                        return Object.assign({}, operator, {
                            value: operator.value + " ", priority: 2
                        });
                    }));
                    addCompletions({value: "within ", priority: 3});
                } else if (patternQueryFilterSuggestionsRegex.test(queryInput)) {
                    // Add suggestions inside filters in pattern queries
                    var patternMatch = patternQueryFilterSuggestionsRegex.exec(queryInput);
                    addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(
                        regexResults, fullEditorText, {name: patternMatch[2]},
                        [constants.STREAMS, constants.WINDOWS]
                    ));
                    addAttributesOfStandardStatefulSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2
                    );
                } else if (nonPatternQueryFilterSuggestionsRegex.test(queryInput)) {
                    // Add suggestions inside filters in queries without patterns
                    addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(
                        regexResults, fullEditorText,
                        {name: nonPatternQueryFilterSuggestionsRegex.exec(queryInput)[1].trim()},
                        [constants.STREAMS, constants.WINDOWS]
                    ).map(function (suggestion) {
                        return Object.assign({}, suggestion, {
                            value: suggestion.value + " ", priority: 3
                        });
                    }));
                    addCompletions(suggestions.logicalOperatorList.map(function (operator) {
                        return Object.assign({}, operator, {
                            value: operator.value + " ", priority: 2
                        });
                    }));
                } else if (afterWithinKeywordSuggestionsRegex.test(queryInput)) {
                    // Add suggestions after the within keyword in patterns
                    addCompletions(suggestions.timeValueTypes.map(function (type) {
                        return Object.assign({}, type, {
                            value: type.value + " ", priority: 2
                        });
                    }));
                    addCompletions(["select", "output", "insert", "delete", "update"].map(function (completion) {
                        return {value: completion + " ", priority: 2};
                    }));
                } else if (everyKeywordSuggestionsRegex.test(queryInput)) {
                    // Add every keyword suggestion after "->" in patterns
                    addCompletions({value: "every ", priority: 2});
                } else if (afterStreamSuggestionsRegex.test(queryInput)) {
                    // Add suggestions after typing a source name
                    var completions = [{value: "#"}];
                    if (/\s+[^\[#]*$/i.test(queryInput)) {
                        completions = completions.concat(
                            [
                                "join", "left outer join", "right outer join", "full outer join", "on",
                                "unidirectional", "within", "select", "output", "insert", "delete", "update"
                            ].map(function (completion) {
                                return {value: completion + " "};
                            })
                        );
                    }
                    addCompletions(completions);
                } else if (afterHashSuggestionsRegex.test(queryInput)) {
                    // Add stream processors, stream processor extension namespaces as suggestions after source + hash
                    addSnippets(getInBuiltStreamProcessors().map(function (suggestion) {
                        return Object.assign({}, suggestion, {
                            priority: 3
                        });
                    }));
                    addSnippets(getExtensionNamesSpaces([constants.STREAM_PROCESSORS]).map(function (suggestion) {
                        return Object.assign({}, suggestion, {
                            value: suggestion.value + ":",
                            priority: 3
                        });
                    }));
                    if (new RegExp(regex.query.input.sourceRegex +
                            "(?:" + regex.query.input.sourceHandlersRegex + ")*" +
                            regex.hash + "[^\\(\\.:]*$", "i").test(queryInput)) {
                        // Add window keyword suggestion
                        // Only one window can be applied for a stream
                        addCompletions({caption: "window", value: "window.", priority: 2});
                    }
                }
            }

            /**
             * Handle the query selection suggestions for the query
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            function handleQuerySelectionSuggestions(regexResults, fullEditorText) {
                var querySelectionClause = regexResults[4];

                // Regexps used for identifying the suggestions
                var extensionFunctionSuggestionsRegex = new RegExp(regex.query.selection.outputAttributesList +
                    regex.comma + regex.namespace + "[a-zA-Z_0-9]*$", "i");
                var afterQuerySelectionClauseSuggestionsRegex = new RegExp(
                    regex.query.selection.outputAttributesList + "\\s+[a-zA-Z_0-9]*$", "i");
                var attributeAndInBuiltFunctionSuggestionsRegex = new RegExp(
                    "(?:" + regex.query.selection.outputAttribute + regex.comma + ")*" +
                    "[a-zA-Z_0-9]*(?:\\s*\\((?:(?:.(?!\\)))*.)?\\s*)?$", "i");

                // Testing to find the relevant suggestion
                if (extensionFunctionSuggestionsRegex.test(querySelectionClause)) {
                    // Add function extension suggestions after namespace + colon
                    var namespace = extensionFunctionSuggestionsRegex.exec(querySelectionClause)[0];
                    addSnippets(getExtensionFunctionNames(namespace));
                } else if (afterQuerySelectionClauseSuggestionsRegex.test(querySelectionClause)) {
                    // Add keyword suggestions after a list attributes without a comma at the end
                    addCompletions(["as", "group by", "having", "output", "insert", "delete", "update"]
                        .map(function (completion) {
                                return {value: completion + " "};
                            }
                        ));
                } else if (attributeAndInBuiltFunctionSuggestionsRegex.test(querySelectionClause)) {
                    // Add attributes list suggestions : attributes, eval scripts, inbuilt and extension functions
                    addAttributesOfSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2,
                        [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS]
                    );
                    addAttributesOfStandardStatefulSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2
                    );
                    addAttributesOfStreamOrTableReferencesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2
                    );
                    addCompletions(Object.keys(self.evalScriptsList).map(function (evalScript) {
                        return {
                            value: evalScript,
                            description: self.evalScriptsList[evalScript].description,
                            priority: 2
                        }
                    }));
                    addSnippets(getExtensionNamesSpaces([constants.FUNCTIONS]).map(function (suggestion) {
                        return Object.assign({}, suggestion, {
                            value: suggestion.value + ":",
                            priority: 2
                        });
                    }));
                    addSnippets(getInBuiltFunctionNames().map(function (completion) {
                        return Object.assign({}, completion, {
                            priority: 2
                        });
                    }));
                }
            }

            /**
             * Handle the query section group by suggestions for the query
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            function handleGroupBySuggestions(regexResults, fullEditorText) {
                var groupByClause = regexResults[6];

                // Regexps used for identifying the suggestions
                var afterGroupByClauseRegex = new RegExp(regex.identifier + "\\s*" +
                    "(?:" + regex.comma + regex.identifier + "\\s*)*" + "\\s+[a-zA-Z_0-9]*$", "i");
                var generalSuggestionsRegex = new RegExp("(?:" + regex.identifier + "\\s*" +
                    regex.comma + ")*", "i");

                // Testing to find the relevant suggestion
                if (afterGroupByClauseRegex.test(groupByClause)) {
                    // Add keyword suggestions after the group by attribute list without a comma at the end
                    addCompletions(["having", "output", "insert", "delete", "update"]
                        .map(function (completion) {
                                return {value: completion + " ", priority: 2};
                            }
                        ));
                } else if (generalSuggestionsRegex.test(groupByClause)) {
                    // Add attributes of the sources for the group by clause
                    addAttributesOfSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2,
                        [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS]
                    );
                    addAttributesOfStandardStatefulSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2
                    );
                    addAttributesOfStreamOrTableReferencesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2
                    );
                }
            }

            /**
             * Handle the having suggestions for the query
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            function handleHavingSuggestions(regexResults, fullEditorText) {
                var havingClause = regexResults[8];

                // Regexps used for identifying the suggestions
                var afterHavingClauseRegex = new RegExp("\\s+[a-zA-Z_0-9]*$");

                // Testing to find the relevant suggestion
                if (afterHavingClauseRegex.test(havingClause)) {
                    addCompletions(["output", "insert", "delete", "update"].map(function (completion) {
                        return {value: completion + " ", priority: 2};
                    }));
                }
                addAttributesOfSourcesAsCompletionsFromQueryIn(
                    regexResults, fullEditorText, 3, 2,
                    [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS]
                );
                addAttributesOfStandardStatefulSourcesAsCompletionsFromQueryIn(
                    regexResults, fullEditorText, 3, 2
                );
                addAttributesOfStreamOrTableReferencesAsCompletionsFromQueryIn(
                    regexResults, fullEditorText, 3, 2
                );
                addCompletions(suggestions.logicalOperatorList.map(function (suggestion) {
                    return Object.assign({}, suggestion, {
                        priority: 2
                    });
                }));
            }

            /**
             * Handle the query output rate suggestions for the query
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             */
            function handleQueryOutputRateSuggestions(regexResults) {
                var outputRateClause = regexResults[10];

                // Regexps used for identifying the suggestions
                var afterHalfTypedKeywordSuggestionsRegex = new RegExp("^[a-zA-Z]*$", "i");
                var everyKeywordSuggestionsRegex = new RegExp(
                    "^(?:" + regex.query.outputRate.types + "|snapshot)\\s+" +
                    "[a-zA-Z]*$", "i");
                var afterOutputRateClauseSuggestionsRegex = new RegExp("^(?:" +
                    "(?:" + regex.query.outputRate.types + ")?|" +
                    "(?:(?:" + regex.query.outputRate.types + ")?|snapshot)" +
                    ")\\s+every\\s+[0-9]*\\s+" + regex.identifier + "\\s+[a-zA-Z]*$", "i");
                var timeValueSuggestionsRegex = new RegExp(
                    "^(?:(?:" + regex.query.outputRate.types + ")?|snapshot)\\s+" +
                    "every\\s+[0-9]*\\s+[a-zA-Z]*$", "i");
                var eventKeywordSuggestionRegex = new RegExp(
                    "^(?:" + regex.query.outputRate.types + ")?\\s+" + "every\\s+[0-9]*\\s+[a-zA-Z]*$", "i");

                // Testing to find the relevant suggestion
                if (outputRateClause == "" || afterHalfTypedKeywordSuggestionsRegex.test(outputRateClause)) {
                    // Output rate type suggestions
                    addCompletions(["snapshot every", "all every", "last every", "first every", "every"]
                        .map(function (completion) {
                            return {value: completion + " "};
                        })
                    );
                } else if (everyKeywordSuggestionsRegex.test(outputRateClause)) {
                    // Add every keyword suggestion after snapshot, all, last, first keywords
                    addCompletions({value: "every "});
                } else if (afterOutputRateClauseSuggestionsRegex.test(outputRateClause)) {
                    // Add keywords after the output rate clause
                    addCompletions(["insert", "delete", "update"].map(function (completion) {
                        return {value: completion + " "};
                    }));
                } else {
                    if (timeValueSuggestionsRegex.test(outputRateClause)) {
                        // Add time value data type suggestions
                        addCompletions(suggestions.timeValueTypes);
                    }
                    if (eventKeywordSuggestionRegex.test(outputRateClause)) {
                        // Add events keyword after the event count
                        addCompletions({value: "events"});
                    }
                }
            }

            /**
             * Handle the query insert into suggestions for the query
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            function handleQueryInsertIntoSuggestions(regexResults, fullEditorText) {
                var streamOutputClause = regexResults[12];

                // Regexps used for identifying the suggestions
                var afterHalfTypedKeywordSuggestionsRegex = new RegExp("^[a-zA-Z]*$", "i");
                var afterOutputEventTypesSuggestionRegex = new RegExp(
                    "^(?:" + regex.query.output.eventTypes + ")?" + "events\\s+[a-zA-Z]*$", "i");
                var afterIntoKeywordSuggestionsRegex = new RegExp(
                    "^(?:(?:" + regex.query.output.eventTypes + ")?" + "events\\s+)?into\\s+[a-zA-Z]*$", "i");
                var afterQuerySuggestionsRegex = new RegExp(
                    "^(?:(?:" + regex.query.output.eventTypes + ")?events\\s+)?" +
                    "into\\s+" + regex.identifier + "\\s*(?:;)?\\s+[a-zA-Z]*$", "i");
                var streamFinderRegex = new RegExp(regex.query.input.standardStreamRegex, "ig");

                // Testing to find the relevant suggestion
                if (streamOutputClause == "" || afterHalfTypedKeywordSuggestionsRegex.test(streamOutputClause)) {
                    // Add output event types, into and overwrite keywords
                    addCompletions(suggestions.outputEventTypes.map(function (completion) {
                        return Object.assign({}, completion, {
                            value: completion.value + " events into "
                        });
                    }));
                    addCompletions(["into", "overwrite"].map(function (completion) {
                        return {value: completion + " "};
                    }));
                } else if (afterOutputEventTypesSuggestionRegex.test(streamOutputClause)) {
                    // Add into keyword after output event types
                    addCompletions({value: "into "});
                } else if (afterIntoKeywordSuggestionsRegex.test(streamOutputClause)) {
                    // Add source suggestions to insert into
                    var isInner = streamFinderRegex.exec(regexResults.input)[1] == "#";
                    // Add normal streams and inner streams as suggestions
                    if (!isInner) {
                        addCompletions(Object.keys(self.streamsList).map(function (stream) {
                            return {
                                caption: stream,
                                value: stream + ";",
                                type: constants.typeToDisplayNameMap[constants.STREAMS],
                                description: self.streamsList[stream].description,
                                priority: 6
                            }
                        }));
                    }
                    // Adding inner streams if hash is present
                    if (isInsidePartition(regexResults)) {
                        var partitionNumber = getTheCurrentPartitionIndex(fullEditorText);
                        if (self.partitionsList[partitionNumber]) {
                            // Adding inner stream completions relevant to the correct partition
                            addCompletions(Object.keys(self.partitionsList[partitionNumber])
                                .map(function (innerStream) {
                                        innerStream = (isInner ? innerStream.substring(1) : innerStream);
                                        return {
                                            caption: innerStream,
                                            value: innerStream + ";",
                                            type: constants.typeToDisplayNameMap[constants.INNER_STREAMS],
                                            description: self.partitionsList[partitionNumber][innerStream].description,
                                            priority: 6
                                        };
                                    }
                                ));
                        }
                    }
                    addCompletions(Object.keys(self.eventTablesList).map(function (table) {
                        return {
                            caption: table,
                            value: table + ";",
                            type: constants.typeToDisplayNameMap[constants.EVENT_TABLES],
                            description: self.eventTablesList[table].description
                        }
                    }));
                    addCompletions(Object.keys(self.eventWindowsList).map(function (window) {
                        return {
                            caption: window,
                            value: window + ";",
                            type: constants.typeToDisplayNameMap[constants.WINDOWS],
                            description: self.eventWindowsList[window].description
                        }
                    }));
                } else if (afterQuerySuggestionsRegex.test(streamOutputClause)) {
                    // Check if this can be the end of the partition and add "end" keyword suggestion
                    handleEndOfPartitionCheck(regexResults);
                }
            }

            /**
             * Handle the query output to table suggestions for the query
             * Handles insert overwrite, delete and update
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             */
            function handleQueryInsertOverwriteDeleteUpdateSuggestions(regexResults, fullEditorText) {
                var tableOutputClause = regexResults[12];

                // Regexps used for identifying the suggestions
                var afterHalfTypedKeywordSuggestionsRegex = new RegExp("^[a-zA-Z]*$", "i");
                var eventTypeSuggestionsRegex = new RegExp("^" + regex.identifier + "\\s+[a-zA-Z]*$", "i");
                var afterForKeywordSuggestionsRegex = new RegExp("^" + regex.identifier + "\\s+" +
                    "for\\s+[a-zA-Z]*$", "i");
                var eventsKeywordSuggestionsRegex = new RegExp("^" + regex.identifier + "\\s+" +
                    "for\\s+" + regex.query.output.eventTypes + "[a-zA-Z]*$", "i");
                var onKeywordSuggestionsRegex = new RegExp("^" + regex.identifier + "\\s+" +
                    "(?:for\\s+(?:" + regex.query.output.eventTypes + ")?events\\s+)?[a-zA-Z]*$", "i");
                var afterOnKeywordSuggestionsRegex = new RegExp("^" + regex.identifier + "\\s+" +
                    "(?:for\\s+(?:" + regex.query.output.eventTypes + ")?events\\s+)?" +
                    "on\\s+(?!;)(?:.(?!;))*$", "i");

                // Testing to find the relevant suggestion
                if (tableOutputClause == "" || afterHalfTypedKeywordSuggestionsRegex.test(tableOutputClause)) {
                    // Add table names as suggestions
                    addCompletions(Object.keys(self.eventTablesList).map(function (table) {
                        return {
                            value: table + " ",
                            type: constants.typeToDisplayNameMap[constants.EVENT_TABLES],
                            description: self.eventTablesList[table].description
                        }
                    }));
                } else if (eventTypeSuggestionsRegex.test(tableOutputClause)) {
                    // Add output event type suggestions after the table name
                    addCompletions(suggestions.outputEventTypes.map(function (completion) {
                        return Object.assign({}, completion, {
                            value: "for " + completion.value + " events on "
                        });
                    }));
                } else if (afterForKeywordSuggestionsRegex.test(tableOutputClause)) {
                    // Add output event type suggestions after the for keyword
                    addCompletions(suggestions.outputEventTypes.map(function (completion) {
                        return Object.assign({}, completion, {
                            value: completion.value + " events on "
                        });
                    }));
                } else if (eventsKeywordSuggestionsRegex.test(tableOutputClause)) {
                    // Add the events keyword suggestion after the event type
                    addCompletions({value: "events "});
                } else if (afterOnKeywordSuggestionsRegex.test(tableOutputClause)) {
                    // Add suggestions after the on keyword for specifying the rows to update in tables
                    addAttributesOfSourcesAsCompletionsFromQueryIn(
                        regexResults, fullEditorText, 3, 2, [constants.EVENT_TABLES]
                    );
                    addCompletions(suggestions.logicalOperatorList.map(function (suggestion) {
                        return Object.assign({}, suggestion, {
                            priority: 2
                        });
                    }));

                    // Check if this can be the end of the partition and add "end" keyword suggestion
                    handleEndOfPartitionCheck(regexResults);
                }
                if (onKeywordSuggestionsRegex.test(tableOutputClause)) {
                    // Add on keyword suggestion
                    addCompletions({value: "on "});
                }
            }

            /**
             * Add "end" keyword after checking for end of partition
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             */
            function handleEndOfPartitionCheck(regexResults) {
                if (isInsidePartition(regexResults.input)) {
                    addCompletions({caption: "end", value: "\nend;"});
                }
            }

            /**
             * Check if the editorText provided indicates that the cursor is inside a partition
             *
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @return {boolean} Boolean indicating whether the cursor is inside a partition
             */
            function isInsidePartition(regexResults) {
                // Regexps used for identifying the suggestions
                var endOfPartitionRegex = new RegExp("partition\\s+with\\s+(?:.(?!\\s+begin))*.\\s+begin\\s+" +
                    "(?:.(?!\\s+end))*.$", "i");

                // Testing to find the relevant suggestion
                return endOfPartitionRegex.test(regexResults.input);
            }

            /**
             * Get the current partition index
             * Partition index indicates the index in the order they are found in the execution plan
             *
             * @param {string} fullEditorText The full editor text before the cursor
             * @return {number}
             */
            function getTheCurrentPartitionIndex(fullEditorText) {
                return (fullEditorText.match(/\s+partition\s+/g) || []).length - 1;
            }

            self.$partition = function (regexResults) {
                // Regexps used for identifying the suggestions
                var partitionConditionStatement = regexResults[1];
                var partitionBody = regexResults[3];

                // Getting te unclosed bracket count for finding the context in the partition with clause
                var unclosedBracketsCount = 0;
                for (var i = 0; i < partitionConditionStatement.length; i++) {
                    if (partitionConditionStatement.charAt(i) == "(") {
                        unclosedBracketsCount++;
                    } else if (partitionConditionStatement.charAt(i) == ")") {
                        unclosedBracketsCount--;
                    }
                }

                // Testing to find the relevant suggestion
                if (partitionBody != undefined) {
                    // Add suggestions inside partitions that did not match query regexp
                    var isCursorAfterSemicolon = false;
                    if (/;\s*$/.test(partitionBody)) {
                        // Add end of partition keyword: "end;"
                        addCompletions({caption: "end", value: "\nend;"});
                        isCursorAfterSemicolon = true;
                    }
                    if (isCursorAfterSemicolon || /^[a-zA-Z_0-9]*$/i.test(partitionBody)) {
                        // Add from keyword and query snippets
                        addCompletions({value: "from "});
                        aceModules.snippetManager.register(
                            queryInitialSnippets, constants.SNIPPET_SIDDHI_CONTEXT
                        );
                    }
                } else if (unclosedBracketsCount == 0 && /\)\s*[a-zA-Z_0-9]*/.test(partitionConditionStatement)) {
                    // Ass suggestions after the partition with clause: "begin" keyword
                    var completionPrefix = "";
                    if (partitionConditionStatement.charAt(partitionConditionStatement.length - 1) == ")") {
                        completionPrefix = "\n";
                    }
                    addCompletions({value: completionPrefix + "begin\n\t", caption: "begin"});
                } else if (unclosedBracketsCount == 1) {
                    // Add completions inside partition with statement

                    // Regexps used for identifying the suggestions
                    var beforeOfKeywordSuggestionRegex = new RegExp(
                        "(?:^\\s*\\(|,)(?:(?:.(?!\\s+of))+.)?(?:\\s+[a-zA-Z_0-9]*)?$", "i");
                    var afterOfKeywordSuggestionRegex = new RegExp(
                        "(?:^\\s*\\(|,)(?:.(?!\\s+of))*.\\s+of\\s+[a-zA-Z_0-9]*$", "i");

                    // Testing to find the relevant suggestion
                    if (beforeOfKeywordSuggestionRegex.test(partitionConditionStatement)) {
                        // Get the sources which has the same attributes in the partition with condition
                        var streams = getStreamsForAttributesInPartitionCondition();
                        if (streams.length == 0) {
                            // Adding all streams if no streams has attributes in the partition condition
                            streams = Object.keys(self.streamsList);
                        }

                        // Getting the attributes of the streams which has attributes in the partition condition already
                        // This is done so that attributes suggested inside the partition condition will all be from one source only
                        var attributes = [];
                        for (i = 0; i < streams.length; i++) {
                            var newAttributes = Object.keys(self.streamsList[streams[i]].attributes);
                            for (var j = 0; j < newAttributes.length; j++) {
                                if (attributes.indexOf(newAttributes[j]) == -1) {
                                    attributes.push(newAttributes[j]);
                                }
                            }
                        }

                        // Adding the extracted attributes
                        addCompletions(attributes.map(function (attribute) {
                            return {
                                value: attribute,
                                priority: 2,
                                type: constants.typeToDisplayNameMap[constants.ATTRIBUTES]
                            };
                        }));

                        if (new RegExp("\s+$", "i")) {
                            // Add keywords inside the partition with condition
                            addCompletions([{value: "of "}, {value: "as "}, {
                                value: "or ",
                                type: constants.typeToDisplayNameMap[constants.LOGICAL_OPERATORS]
                            }]);
                        }
                    } else if (afterOfKeywordSuggestionRegex.test(partitionConditionStatement)) {
                        // Add the source name suggestions after the of keyword
                        // Only sources which has all the attributes in the partition with condition will be suggested
                        addCompletions(getStreamsForAttributesInPartitionCondition().map(function (stream) {
                            return {
                                value: stream,
                                type: constants.typeToDisplayNameMap[constants.STREAMS],
                                description: self.streamsList[stream].description
                            };
                        }));
                    }
                }

                /**
                 * Get the streams of the attributes mentioned in the partition condition statement
                 * Partition condition cannot contain attributes from more than one stream
                 *
                 * @private
                 * @return {string[]} streams of the attributes in the partition condition
                 */
                function getStreamsForAttributesInPartitionCondition() {
                    var streamAttributeSearchRegex = new RegExp("(?:(?:[0-9]+|(" + regex.identifier + "))\\s*" +
                        "(?:<|>|=|!){1,2}\\s*" +
                        "(?:[0-9]+|(" + regex.identifier + "))\\s+as|" +
                        "(?:^\\s*\\(|,)\\s*(" + regex.identifier + ")\\s+(?:of\\s+)?[a-zA-Z_0-9]*$)", "ig");

                    // Getting the attributes mentioned in the partition condition
                    var attributeList = [];
                    var attribute;
                    while (attribute = streamAttributeSearchRegex.exec(partitionConditionStatement)) {
                        if (attribute && attributeList.indexOf(attribute) == -1) {
                            for (i = 1; i < attribute.length; i++) {
                                if (attribute[i]) {
                                    attributeList.push(attribute[i]);
                                }
                            }
                        }
                    }

                    // Getting the streams with all the attributes in them
                    var streamList = [];
                    if (attributeList.length > 0) {
                        streamListLoop: for (var streamName in self.streamsList) {
                            if (self.streamsList.hasOwnProperty(streamName)) {
                                for (i = 0; i < attributeList.length; i++) {
                                    if (!self.streamsList[streamName].attributes[attributeList[i]]) {
                                        continue streamListLoop;
                                    }
                                }
                                streamList.push(streamName);
                            }
                        }
                    }
                    return streamList;
                }
            };

            /*
             * Suggestion Handler functions ends here
             */

            /**
             * add attributes of stream references or table references in query in section of the query
             * (stream as reference)
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {int} attributePriority priority to be set as attribute priority
             * @param {int} streamPriority priority to be set as stream priority
             */
            function addAttributesOfStreamOrTableReferencesAsCompletionsFromQueryIn(regexResults, fullEditorText,
                                                                                    attributePriority, streamPriority) {
                var queryInput = regexResults[2];
                var sourceReferenceSearchRegex = new RegExp(regex.query.input.streamReference, "ig");
                var referenceToSourceMap = [];
                var sourceReferenceMatch;

                // Getting the reference to source map
                while (sourceReferenceMatch = sourceReferenceSearchRegex.exec(queryInput)) {
                    if (getSource(regexResults, fullEditorText, sourceReferenceMatch[1],
                            [constants.STREAMS, constants.EVENT_TABLES])) {
                        referenceToSourceMap[sourceReferenceMatch[2]] = sourceReferenceMatch[1];
                    }
                }

                addAttributesOfSourceReferencesAsCompletions(
                    regexResults, fullEditorText, referenceToSourceMap, attributePriority,
                    streamPriority, [constants.STREAMS, constants.EVENT_TABLES]
                );
            }

            /**
             * add attributes of standard stateful sources in patterns in query
             * (event = stream)
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {int} attributePriority priority to be set as attribute priority
             * @param {int} streamPriority priority to be set as stream priority
             */
            function addAttributesOfStandardStatefulSourcesAsCompletionsFromQueryIn(regexResults, fullEditorText,
                                                                                    attributePriority, streamPriority) {
                var queryInput = regexResults[2];
                var standardStatefulSourceSearchRegex = new RegExp(regex.query.input.patternStreamRegex, "ig");
                var eventToStreamMap = [];
                var standardStatefulSourceMatch;

                // Getting the standard stateful source to stream map
                while (standardStatefulSourceMatch = standardStatefulSourceSearchRegex.exec(queryInput)) {
                    if (self.streamsList[standardStatefulSourceMatch[2]]) {
                        eventToStreamMap[standardStatefulSourceMatch[1]] = standardStatefulSourceMatch[2];
                    }
                }

                addAttributesOfSourceReferencesAsCompletions(
                    regexResults, fullEditorText, eventToStreamMap,
                    attributePriority, streamPriority, [constants.STREAMS]
                );
            }

            /**
             * add attributes in the streams or tables in the query by searching the query input section
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {int} attributePriority priority to be set as attribute priority
             * @param {int} streamPriority priority to be set as stream priority
             * @param {string[]} sourceTypes Source types to search for. Should be a subset of [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.EVAL_SCRIPTS, constants.TRIGGERS]
             */
            function addAttributesOfSourcesAsCompletionsFromQueryIn(regexResults, fullEditorText, attributePriority,
                                                                    streamPriority, sourceTypes) {
                var queryInput = regexResults[2];
                var queryInSources = [];
                var streamFinderRegex = new RegExp(regex.query.input.standardStreamRegex, "ig");
                var streamMatch;

                // Getting the sources list in the query input clause
                while (streamMatch = streamFinderRegex.exec(queryInput)) {
                    if (["join", "every"].indexOf(streamMatch[1]) == -1 &&
                        getSource(regexResults, fullEditorText, streamMatch[1], sourceTypes)) {
                        queryInSources.push(streamMatch[1]);
                    }
                }

                addAttributesOfSourcesAsCompletions(
                    regexResults, fullEditorText, queryInSources, attributePriority, streamPriority, sourceTypes
                );
            }

            /**
             * add attributes in the streams or tables provided
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {string[]} sources Array of streams of which attributes will be added
             * @param {int} attributePriority priority to be set as attribute priority
             * @param {int} sourcePriority priority to be set as source priority
             * @param {string[]} sourceTypes Source types to search for. Should be a subset of [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.EVAL_SCRIPTS, constants.TRIGGERS]
             */
            function addAttributesOfSourcesAsCompletions(regexResults, fullEditorText, sources,
                                                         attributePriority, sourcePriority, sourceTypes) {
                var afterSourceAndDotSuggestionsRegex =
                    new RegExp("((?:#)?" + regex.identifier + ")\\s*\\.\\s*[a-zA-Z_0-9]*$", "i");

                var sourceBeforeDotMatch;
                if (sourceBeforeDotMatch = afterSourceAndDotSuggestionsRegex.exec(regexResults.input)) {
                    // Add suggestions after the source name + dot
                    if (sources.indexOf(sourceBeforeDotMatch[1]) != -1) {
                        addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(
                            regexResults, fullEditorText, {name: sourceBeforeDotMatch[1]}, sourceTypes
                        ).map(function (attribute) {
                            return Object.assign({}, attribute, {
                                priority: attributePriority
                            });
                        }));
                    }
                } else {
                    // Add the attributes names (duplicate attributes are prefixed with the stream + dot)
                    addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(
                        regexResults, fullEditorText,
                        sources.map(function (source) {
                            return {name: source};
                        }),
                        sourceTypes
                    ).map(function (attribute) {
                        return Object.assign({}, attribute, {
                            priority: attributePriority
                        });
                    }));
                    // Add the stream names + dot
                    addCompletions(sources.map(function (sourceName) {
                        var source =
                            getSource(regexResults, fullEditorText, sourceName, sourceTypes).description;
                        return {
                            value: sourceName + ".",
                            description: source.description,
                            type: source.type,
                            priority: sourcePriority
                        };
                    }));
                }
            }

            /**
             * add attributes in the source in the reference to stream map
             * References will be used rather than stream names to refer to attributes (reference.attribute)
             * A reference can be an event in a pattern (event=pattern) or a stream reference in query in (stream as reference)
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {string[]} sourceToStreamMap Array of streams of which attributes will be added
             * @param {int} attributePriority priority to be set as attribute priority
             * @param {int} sourcePriority priority to be set as source priority
             * @param {string[]} sourceTypes Source types to search for. Should be a subset of [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.EVAL_SCRIPTS, constants.TRIGGERS]
             */
            function addAttributesOfSourceReferencesAsCompletions(regexResults, fullEditorText,
                                                                  sourceToStreamMap, attributePriority,
                                                                  sourcePriority, sourceTypes) {
                var afterSourceAndDotSuggestionsRegex = new RegExp("(" + regex.identifier + ")\\s*" +
                    "(?:\\[\\s*[0-9]*\\s*\\])?\\s*\\.\\s*[a-zA-Z_0-9]*$", "i");

                var sourceBeforeDotMatch;
                if (sourceBeforeDotMatch = afterSourceAndDotSuggestionsRegex.exec(regexResults.input)) {
                    if (sourceToStreamMap[sourceBeforeDotMatch[1]]) {
                        // Add suggestions after the source name + dot
                        addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(
                            regexResults, fullEditorText,
                            {
                                name: sourceToStreamMap[sourceBeforeDotMatch[1]],
                                reference: sourceBeforeDotMatch[1]
                            },
                            sourceTypes
                        ).map(function (attribute) {
                            return Object.assign({}, attribute, {
                                priority: attributePriority
                            });
                        }));
                    }
                } else {
                    // Add the attributes names (duplicate attributes are prefixed with the stream + dot)
                    for (var reference in sourceToStreamMap) {
                        if (sourceToStreamMap.hasOwnProperty(reference)) {
                            addCompletions(getAttributesFromSourcesWithPrefixedDuplicates(
                                regexResults, fullEditorText,
                                {name: sourceToStreamMap[reference], reference: reference},
                                sourceTypes
                            ).map(function (attribute) {
                                return Object.assign({}, attribute, {
                                    value: reference + "." + attribute.value,
                                    priority: attributePriority
                                });
                            }));
                        }
                    }
                    // Add the stream names + dot
                    addCompletions(Object.keys(sourceToStreamMap).map(function (reference) {
                        var source = getSource(
                            regexResults, fullEditorText, sourceToStreamMap[reference],
                            [constants.STREAMS, constants.EVENT_TABLES]
                        );
                        return {
                            value: reference + ".",
                            description: source.description,
                            type: source.type,
                            priority: sourcePriority
                        };
                    }));
                }
            }

            /**
             * Get the list of namespaces which has artifacts in  objType1 or objType2 categories
             *
             * @private
             * @param {string[]} types types of processors of which namespaces are returned. Should be one of ["windowProcessors", "functions", "streamProcessors"]
             * @returns {Array} list of namespaces.
             */
            function getExtensionNamesSpaces(types) {
                var namespaces = [];
                for (var namespace in CompletionEngine.functionOperationSnippets.extensions) {
                    if (CompletionEngine.functionOperationSnippets.extensions.hasOwnProperty(namespace)) {
                        var processorsPresentInNamespace = false;
                        for (var i = 0; i < types.length; i++) {
                            if (CompletionEngine.functionOperationSnippets.extensions[namespace][types[i]]) {
                                processorsPresentInNamespace = true;
                                break;
                            }
                        }
                        if (processorsPresentInNamespace) {
                            namespaces.push(namespace);
                        }
                    }
                }
                return namespaces;
            }

            /**
             * Get the list of  extension function snippets of given namespace
             *
             * @private
             * @param {string} namespace namespace of the functions
             * @returns {Array} : list of function snippets
             */
            function getExtensionFunctionNames(namespace) {
                if (CompletionEngine.functionOperationSnippets.extensions[namespace]) {
                    return Object.values(CompletionEngine.functionOperationSnippets.extensions[namespace].functions)
                        .map(function (processor) {
                            processor.type = constants.typeToDisplayNameMap[constants.FUNCTIONS];
                            return processor;
                        });
                } else {
                    return [];
                }
            }

            /**
             * Get the list of  extension window processor snippets of given namespace
             *
             * @private
             * @param {string} namespace namespace of the window processors
             * @returns {Array} list of window processor snippets
             */
            function getExtensionWindowProcessors(namespace) {
                if (CompletionEngine.functionOperationSnippets.extensions[namespace]) {
                    return Object.values(CompletionEngine.functionOperationSnippets.extensions[namespace].windowProcessors)
                        .map(function (processor) {
                            processor.type = constants.typeToDisplayNameMap[constants.WINDOW_PROCESSORS];
                            return processor;
                        });
                } else {
                    return [];
                }
            }

            /**
             * Get the list of  extension stream processor snippets of given namespace
             *
             * @private
             * @param {string} namespace namespace of the stream processors
             * @returns {Array} list of stream processor snippets
             */
            function getExtensionStreamProcessors(namespace) {
                if (CompletionEngine.functionOperationSnippets.extensions[namespace]) {
                    return Object.values(CompletionEngine.functionOperationSnippets.extensions[namespace].streamProcessors)
                        .map(function (processor) {
                            processor.type = constants.typeToDisplayNameMap[constants.STREAM_PROCESSORS];
                            return processor;
                        });
                } else {
                    return [];
                }
            }

            /**
             * Get the list of inbuilt function snippets
             *
             * @private
             * @returns {Array} list of function snippets
             */
            function getInBuiltFunctionNames() {
                if (CompletionEngine.functionOperationSnippets.inBuilt.functions) {
                    return Object.values(CompletionEngine.functionOperationSnippets.inBuilt.functions)
                        .map(function (processor) {
                            processor.type = constants.typeToDisplayNameMap[constants.FUNCTIONS];
                            return processor;
                        });
                } else {
                    return [];
                }
            }

            /**
             * Get the list of inbuilt window processor snippets
             *
             * @private
             * @returns {Array} list of window processor snippets
             */
            function getInBuiltWindowProcessors() {
                if (CompletionEngine.functionOperationSnippets.inBuilt.windowProcessors) {
                    return Object.values(CompletionEngine.functionOperationSnippets.inBuilt.windowProcessors)
                        .map(function (processor) {
                            processor.type = constants.typeToDisplayNameMap[constants.WINDOW_PROCESSORS];
                            return processor;
                        });
                } else {
                    return [];
                }
            }

            /**
             * Get the list of inbuilt stream processor snippets
             *
             * @private
             * @returns {Array} list of stream processor snippets
             */
            function getInBuiltStreamProcessors() {
                if (CompletionEngine.functionOperationSnippets.inBuilt.streamProcessors) {
                    return Object.values(CompletionEngine.functionOperationSnippets.inBuilt.streamProcessors)
                        .map(function (processor) {
                            processor.type = constants.typeToDisplayNameMap[constants.STREAM_PROCESSORS];
                            return processor;
                        });
                } else {
                    return [];
                }
            }

            /**
             * Get the attributes of the streams or tables specified
             * Duplicate attribute names will be prefixed with the stream or table names
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {Object|Object[]} sourceName name of the source of which attributes are returned
             * @param {string[]} sourceTypes Source types to search for. Should be a subset of [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.EVAL_SCRIPTS, constants.TRIGGERS]
             * @return {Object[]} arrays of attribute names of the stream or table
             */
            function getAttributesFromSourcesWithPrefixedDuplicates(regexResults, fullEditorText,
                                                                    sourceName, sourceTypes) {
                var attributes = [];
                if (sourceName.constructor === Array) {
                    // Get the attributes list with the relevant source
                    var newAttributes = [];
                    for (var i = 0; i < sourceName.length; i++) {
                        newAttributes = newAttributes.concat(getAttributesOfSource(
                            regexResults, fullEditorText, sourceName[i].name,
                            sourceTypes, sourceName[i].reference
                        ));
                    }

                    // Prefixing duplicates attribute names with stream
                    var prefixedAttributes = [];
                    for (var j = 0; j < newAttributes.length; j++) {
                        if (prefixedAttributes.indexOf(newAttributes[j].value) == -1) {
                            // Check for duplicates after the current index
                            for (var k = j + 1; k < newAttributes.length; k++) {
                                if (newAttributes[j].value == newAttributes[k].value) {
                                    attributes.push({
                                        value: newAttributes[k].source + "." + newAttributes[k].value
                                    });

                                    // If this is the first time this duplicate is detected prefix the first attribute as well
                                    if (prefixedAttributes.indexOf(newAttributes[j].value) == -1) {
                                        attributes.push({
                                            value: newAttributes[j].source + "." + newAttributes[j].value
                                        });
                                        prefixedAttributes.push(newAttributes[j].value);
                                    }
                                }
                            }

                            // If no duplicates are found add without prefix
                            if (prefixedAttributes.indexOf(newAttributes[j].value) == -1) {
                                attributes.push({
                                    value: newAttributes[j].value
                                });
                            }
                        }
                    }
                } else {
                    attributes = getAttributesOfSource(
                        regexResults, fullEditorText, sourceName.name, sourceTypes, sourceName.reference
                    );
                }

                return attributes.map(function (attribute) {
                    return Object.assign({}, attribute, {
                        type: constants.typeToDisplayNameMap[constants.ATTRIBUTES]
                    });
                });
            }

            /**
             * Get the attributes of a single stream or table
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {string} sourceName name of the source of which attributes are returned
             * @param {string[]} sourceTypes Source types to search for. Should be a subset of [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.EVAL_SCRIPTS, constants.TRIGGERS]
             * @param {string} [reference] reference name used to refer to the stream or table
             * @return {Object[]} arrays of attribute names of the stream or table
             */
            function getAttributesOfSource(regexResults, fullEditorText, sourceName, sourceTypes, reference) {
                var attributes = [];
                var source = getSource(regexResults, fullEditorText, sourceName, sourceTypes);
                if (source && source.attributes) {
                    attributes = Object.keys(source.attributes);
                }
                return attributes.map(function (attribute) {
                    return {value: attribute, source: (reference ? reference : sourceName)};
                });
            }

            /**
             * Get a source: stream, inner stream, table, window, trigger
             * Eval scripts can be retrieved in the same way as well
             *
             * @private
             * @param {string[]} regexResults Array of groups from the regex execution of the query
             * @param {string} fullEditorText Complete editor text before the cursor
             * @param {string} sourceName Name of the source to fetch
             * @param {string[]} sourceTypes Source types to search for. Should be a subset of [constants.STREAMS, constants.EVENT_TABLES, constants.WINDOWS, constants.EVAL_SCRIPTS, constants.TRIGGERS]
             * @return {Object} The object representing the requested source belonging to the requested source type
             */
            function getSource(regexResults, fullEditorText, sourceName, sourceTypes) {
                var source;
                for (var i = 0; i < sourceTypes.length; i++) {
                    if (sourceName.charAt(0) == "#" &&
                        constants.STREAMS == sourceTypes[i] &&
                        isInsidePartition(regexResults)) {
                        // Inner streams
                        var partitionCount = getTheCurrentPartitionIndex(fullEditorText);
                        if (self.partitionsList[partitionCount] &&
                            self.partitionsList[partitionCount][sourceName]) {
                            source = self.partitionsList[partitionCount][sourceName];
                            source.type = constants.typeToDisplayNameMap[constants.INNER_STREAMS];
                        }
                        break;
                    } else if (self[sourceTypes[i] + "List"][sourceName] &&
                        self[sourceTypes[i] + "List"][sourceName].attributes) {
                        // Sources except inner streams
                        source = self[sourceTypes[i] + "List"][sourceName];
                        source.type = constants.typeToDisplayNameMap[sourceTypes[i]];
                        break;
                    }
                }
                return source;
            }

            /**
             * Add a new completions to the words list
             *
             * Can be object or array of objects with the object format as shown below
             *
             * {
         *      caption:        "caption to be shown in the tooltip"        // optional | default: value
         *      value:          "value to append after the cursor"          // required
         *      priority:       2                                           // optional | default: 1
         *      description:    "description about the completion"          // optional | default: no description
         *      type:           "type of completion"                        // optional | default: no type
         * }
             *
             * @private
             * @param {Object[]|Object} suggestions list of  suggestions
             */
            function addCompletions(suggestions) {
                if (suggestions.constructor === Array) {
                    for (var i = 0; i < suggestions.length; i++) {
                        addCompletion(suggestions[i]);
                    }
                } else {
                    addCompletion(suggestions);
                }

                /**
                 * Add a single completion to the completions list
                 *
                 * @private
                 * @param {Object} completion Completion to add to the completions list
                 */
                function addCompletion(completion) {
                    self.completionsList.push({
                        caption: (completion.caption == undefined ? completion.value : completion.caption),
                        value: completion.value,
                        score: (completion.priority == undefined ? 1 : completion.priority),
                        docHTML: completion.description,
                        meta: completion.type
                    });
                }
            }

            /**
             * Add a new completions to the words list
             * Snippets needs to created using aceModules.snippetManager.parseSnippetFile(string) method
             *
             * @private
             * @param {Object[]|Object} suggestions list of  suggestions
             */
            function addSnippets(suggestions) {
                if (suggestions.constructor === Array) {
                    for (var i = 0; i < suggestions.length; i++) {
                        aceModules.snippetManager.register(suggestions[i], constants.SNIPPET_SIDDHI_CONTEXT);
                        self.suggestedSnippets.push(suggestions[i]);
                    }
                } else {
                    aceModules.snippetManager.register(suggestions, constants.SNIPPET_SIDDHI_CONTEXT);
                    self.suggestedSnippets.push(suggestions);
                }
            }
        }

        /*
         * Data stored common for all editors
         *
         * Stream processors and stream function are stored as streamProcessors
         * Window processors are stored as windowProcessors
         * Function executors and attribute aggregators are stored as functions
         */
        CompletionEngine.functionOperationSnippets = {
            /*
             * extensions object contains the custom function, streamProcessor and windowProcessor extensions available for
             * the current Siddhi session. This data structure is dynamically pulled down from the backend services.
             *
             *      extensions = {
             *        namespace1: {
             *          functions: {
             *              function1: {function1 snippet object},
             *              function2: {function2 snippet object},
             *          },
             *          streamProcessors: {
             *              // Same as in function section
             *          },
             *          windowProcessors: {
             *              // same as in function section
             *          }
             *       },
             *       namespace2: {
             *          functions: {
             *              // Same as in function section
             *          },
             *          streamProcessors: {
             *              // Same as in function section
             *          },
             *          windowProcessors: {
             *              // same as in function section
             *          }
             *       }
             *    }
             */
            extensions: {},

            /*
             * inBuilt object contains the custom function, streamProcessor and windowProcessor extensions available for
             * the current Siddhi session. This data structure is dynamically pulled down from the backend services.
             *
             *    inBuilt = {
             *          functions: {
             *              function1: {function1 snippet object},
             *              function2: {function2 snippet object},
             *          },
             *          streamProcessors: {
             *              // Same as in function section
             *              // Same as in function section
             *          }  ,
             *          windowProcessors: {
             *              // same as in function section
             *          }
             *    }
             */
            inBuilt: {}
        };

        /*
         * Meta data JSON object structure (for extensions and inbuilt) :
         * These are either inside the inBuilt JSON object and extensions.namespace JSON object
         *
         *  {
         *      processorType: [
         *          {
         *              "name": "name of the processor",
         *              "description": "description about the processor",
         *              "parameters": [
         *                  {
         *                      "name": "name of the first parameter",
         *                      "type": ["possible", "types", "of", "arguments", "that", "can", "be", "passed", "for", "this", "parameter"],
         *                      "optional": "boolean"
         *                  },
         *                  {
         *                      "name": "name of the second parameter",
         *                      "type": ["possible", "types", "of", "arguments", "that", "can", "be", "passed", "for", "this", "parameter"],
         *                      "optional": "boolean"
         *                  }
         *              ],
         *              "return": ["possible", "types", "returned", "by", "the", "processor"]
         *          }
         *      ]
         *  }
         */
        /**
         * Load meta data from a json file
         *
         * @param [onSuccessCallback] Callback function to be called on successful reception of meta data
         * @param [onErrorCallback] Callback function to be called on error
         */
        CompletionEngine.loadMetaData = function (onSuccessCallback, onErrorCallback) {
            $.ajax({
                type: "GET",
                url: constants.SERVER_URL + "siddhi-editor/metadata",
                success: function (response, textStatus, jqXHR) {
                    if (response.status == "SUCCESS") {
                        (function () {
                            var snippets = {};
                            for (var processorType in response.inBuilt) {
                                if (response.inBuilt.hasOwnProperty(processorType)) {
                                    var snippet = {};
                                    for (var i = 0; i < response.inBuilt[processorType].length; i++) {
                                        snippet[response.inBuilt[processorType][i].name] =
                                            generateSnippetFromProcessorMetaData(response.inBuilt[processorType][i]);
                                    }
                                    snippets[processorType] = snippet;
                                }
                            }
                            CompletionEngine.functionOperationSnippets.inBuilt = snippets;
                        })();
                        (function () {
                            var snippets = {};
                            for (var namespace in response.extensions) {
                                if (response.extensions.hasOwnProperty(namespace)) {
                                    snippets[namespace] = {};
                                    for (var processorType in response.extensions[namespace]) {
                                        if (response.extensions[namespace].hasOwnProperty(processorType)) {
                                            var snippet = {};
                                            for (var i = 0; i < response.extensions[namespace][processorType].length; i++) {
                                                snippets[response.extensions[namespace][processorType][i].name] =
                                                    generateSnippetFromProcessorMetaData(
                                                        response.extensions[namespace][processorType][i]
                                                    );
                                            }
                                            snippets[namespace][processorType] = snippet;
                                        }
                                    }
                                }
                            }
                            CompletionEngine.functionOperationSnippets.extensions = snippets;
                        })();
                        if (onSuccessCallback) {
                            onSuccessCallback(response, textStatus, jqXHR);
                        }
                    } else if (onErrorCallback) {
                        onErrorCallback(response.message, (response.status ? response.status : textStatus), jqXHR);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    if (onErrorCallback) {
                        onErrorCallback(errorThrown, textStatus, jqXHR);
                    }
                }
            });
        };

        /**
         * Callback function for load meta data success
         *
         * @callback LoadMetaDataOnSuccessCallback
         * @param {string} response The response returned from the server
         * @param {string} status The http status returned by the server
         * @param {object} jqXHR jQuery XMLHTTPRequest object
         */

        /**
         * Callback function for load meta data error
         *
         * @callback LoadMetaDataOnErrorCallback
         * @param {string} message The error thrown or the error message
         * @param {string} status The http status or the status returned by the server
         * @param {object} jqXHR jQuery XMLHTTPRequest object
         */

        /**
         * Prepare a snippet from the processor
         * Snippets are objects that can be passed into the ace editor to add snippets to the completions provided
         *
         * @private
         * @param {Object} processorMetaData The processor object with relevant parameters
         * @return {Object} snippet
         */
        function generateSnippetFromProcessorMetaData(processorMetaData) {
            var snippetVariableCount = 0;
            var snippetText = "snippet " + processorMetaData.name + "\n\t" + processorMetaData.name;
            if (processorMetaData.parameters) {
                snippetText += "(";
                for (var i = 0; i < processorMetaData.parameters.length; i++) {
                    var parameter = processorMetaData.parameters[i];
                    if (i != 0) {
                        snippetText += ", ";
                    }
                    snippetText += "${" + (snippetVariableCount + 1) + ":" + parameter.name + "}";
                    snippetVariableCount++;
                }
                snippetText += ")\n";
            }
            var snippet = aceModules.snippetManager.parseSnippetFile(snippetText)[0];

            if (processorMetaData.description || processorMetaData.returnType || processorMetaData.parameters) {
                snippet.description = utils.generateDescriptionForProcessor(processorMetaData);
            }
            return snippet;
        }

        return CompletionEngine;
    });