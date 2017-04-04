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

"use strict";   // JS strict mode

var SiddhiQLListener = require('./gen/SiddhiQLListener').SiddhiQLListener;

/**
 * The Data Population Listener prototype constructor
 * Inherits from SiddhiQLListener generated from SiddhiQL grammar using ANTLR4
 *
 * Only data extraction is done by this listener
 * THe data is stored in the main js
 *
 * @constructor
 * @param walker The editor for which this listener is populating data
 */
function DataPopulationListener(walker) {
    SiddhiQLListener.call(this);     // inherit default listener
    this.walker = walker;
    this.partitionCount = 0;
    return this;
}
DataPopulationListener.prototype = Object.create(SiddhiQLListener.prototype);
DataPopulationListener.prototype.constructor = DataPopulationListener;

/*
 * Define statement listeners starts here
 */

DataPopulationListener.prototype.exitDefinition_stream = function (ctx) {
    // Extracting the stream data from the define stream statement
    var streamName = this.walker.utils.getTextFromANTLRCtx(ctx.source());
    var attributes = {};
    var i = 0;
    while (ctx.attribute_name(i)) {
        attributes[this.walker.utils.getTextFromANTLRCtx(ctx.attribute_name(i))] =
            this.walker.utils.getTextFromANTLRCtx(ctx.attribute_type(i));
        i++;
    }
    this.walker.completionData.streamsList[streamName] = {
        attributes: attributes
    };

    // Adding define stream statements to the statements list
    addStatement(this.walker, ctx, ";");
};

DataPopulationListener.prototype.exitDefinition_table = function (ctx) {
    // Extracting the event table data from the define table statement
    var tableName = this.walker.utils.getTextFromANTLRCtx(ctx.source());
    var attributes = {};
    var i = 0;
    while (ctx.attribute_name(i)) {
        attributes[this.walker.utils.getTextFromANTLRCtx(ctx.attribute_name(i))] =
            this.walker.utils.getTextFromANTLRCtx(ctx.attribute_type(i));
        i++;
    }
    this.walker.completionData.eventTablesList[tableName] = {
        attributes: attributes
    };

    // Adding define table statements to the statements list
    addStatement(this.walker, ctx, ";");
};

DataPopulationListener.prototype.exitDefinition_trigger = function (ctx) {
    // Extracting the event trigger data from the define trigger statement
    var triggerName = this.walker.utils.getTextFromANTLRCtx(ctx.trigger_name());
    var metaData;
    if (ctx.time_value()) {
        metaData = {type: "Time Value", time: this.walker.utils.getTextFromANTLRCtx(ctx.time_value())};
    } else if (ctx.string_value()) {
        metaData = {type: "Cron Expression", time: this.walker.utils.getTextFromANTLRCtx(ctx.string_value())};
    }
    if (metaData) {
        this.walker.completionData.eventTriggersList[triggerName] = metaData;
    }

    // Adding define trigger statements to the statements list
    addStatement(this.walker, ctx, ";");
};

DataPopulationListener.prototype.exitDefinition_function = function (ctx) {
    // Extracting the eval script data from the define function statement
    var evalScriptName = this.walker.utils.getTextFromANTLRCtx(ctx.function_name());
    this.walker.completionData.evalScriptsList[evalScriptName] = {
        language: this.walker.utils.getTextFromANTLRCtx(ctx.language_name()),
        returnType: this.walker.utils.getTextFromANTLRCtx(ctx.attribute_type()),
        functionBody: this.walker.utils.getTextFromANTLRCtx(ctx.function_body())
    };

    // Adding define function statements to the statements list
    addStatement(this.walker, ctx, ";");
};

DataPopulationListener.prototype.exitDefinition_window = function (ctx) {
    // Extracting the event window data from the define window statement
    var windowName = this.walker.utils.getTextFromANTLRCtx(ctx.source());
    var attributes = {};
    var i = 0;
    while (ctx.attribute_name(i)) {
        attributes[this.walker.utils.getTextFromANTLRCtx(ctx.attribute_name(i))] =
            this.walker.utils.getTextFromANTLRCtx(ctx.attribute_type(i));
        i++;
    }
    var metaData = {
        attributes: attributes,
        functionOperation: this.walker.utils.getTextFromANTLRCtx(ctx.function_operation())
    };
    if (ctx.output_event_type()) {
        metaData.output = this.walker.utils.getTextFromANTLRCtx(ctx.output_event_type());
    }
    this.walker.completionData.eventWindowsList[windowName] = metaData;

    // Adding define window statements to the statements list
    addStatement(this.walker, ctx, ";");
};

/*
 * Define statement listeners ends here
 */

DataPopulationListener.prototype.exitQuery = function (ctx) {
    // Extracting the stream data from the queries which insert into without defining them
    // Inner streams are also extracted
    if (ctx.query_output() && ctx.query_output().children && ctx.query_output().target()) {
        var outputTarget = this.walker.utils.getTextFromANTLRCtx(ctx.query_output().target());
        // Updating the data for streams inserted into without defining if select section is available
        if (!this.walker.completionData.eventTablesList[outputTarget] &&
            !this.walker.completionData.streamsList[outputTarget] &&
            !this.walker.completionData.eventWindowsList[outputTarget]) {
            var isInner = !!ctx.query_output().target().source().inner;
            if (ctx.query_section()) {
                // Creating the attributes to reference map
                var querySelectionCtx = ctx.query_section();
                var attributes = {};
                var i = 0;
                var outputAttributeCtx;
                while (outputAttributeCtx = querySelectionCtx.output_attribute(i)) {
                    if (outputAttributeCtx.attribute_name()) {
                        attributes[this.walker.utils.getTextFromANTLRCtx(outputAttributeCtx.attribute_name())] =
                            SiddhiEditor.constants.dataPopulation.UNDEFINED_DATA_TYPE;
                    } else if (outputAttributeCtx.attribute_reference() &&
                        outputAttributeCtx.attribute_reference().attribute_name()) {
                        attributes[this.walker.utils.getTextFromANTLRCtx(outputAttributeCtx.attribute_reference().attribute_name())] =
                            SiddhiEditor.constants.dataPopulation.UNDEFINED_DATA_TYPE;
                    }
                    i++;
                }

                // Adding the stream name and the attributes names since they are required in completions
                if (isInner) {
                    this.walker.completionData.partitionsList[this.partitionCount][outputTarget] = {
                        attributes: attributes
                    };
                } else {
                    this.walker.completionData.streamsList[outputTarget] = {
                        attributes: attributes
                    };
                }
            }
            // Marking the streams as incomplete since attribute types are not fetched
            // Data types are required in tooltips
            if (isInner) {
                this.walker.incompleteData.partitions[this.partitionCount].push(outputTarget);
            } else {
                this.walker.incompleteData.streams.push(outputTarget);
            }
        }
    }
};

DataPopulationListener.prototype.enterPartition = function (ctx) {
    // Adding object/array to the partition arrays
    // The this.partitionCount variable indicates the index in this array
    this.walker.completionData.partitionsList.push({});
    this.walker.incompleteData.partitions.push([]);
};

DataPopulationListener.prototype.exitPartition = function () {
    // Incrementing the partition count so that the correct index in used in the next partition
    this.partitionCount++;
};

DataPopulationListener.prototype.exitPlan_annotation = function (ctx) {
    // Adding plan annotation to the statements list
    addStatement(this.walker, ctx);
};

DataPopulationListener.prototype.exitExecution_element = function (ctx) {
    // Adding queries and partitions to the statements list
    addStatement(this.walker, ctx, ";");
};

/**
 * Add a statement to the editor.completionEngine.statementsList array
 * endOfStatementToken is added at the end of the statement if provided
 * Statements list is used in sending statement by statement for validation
 * Also the statements list is used for getting the last statement for completions
 *
 * @param {object} walker The editor which holds the statements list to which the statement is added
 * @param {object} ctx The ANTLR context which will be used in getting the statement
 * @param [endOfStatementToken] The token to be appended at the end of the statement
 */
function addStatement(walker, ctx, endOfStatementToken) {
    walker.statementsList.push({
        statement: walker.utils.getTextFromANTLRCtx(ctx)  + (endOfStatementToken ? endOfStatementToken : ""),
        line:ctx.start.line - 1
    });
}

/*
 * Token Tooltip update listeners ends here
 */

exports.DataPopulationListener = DataPopulationListener;
