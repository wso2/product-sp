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
 * The Token Tool Tip Poinr Recognition Listener prototype constructor
 * Inherits from SiddhiQLListener generated from SiddhiQL grammar using ANTLR4
 *
 * Only recognizng the tooltip points is done by this listener
 * The actual adding of the tooltips are done in the main js
 *
 * @constructor
 * @param walker The walker for which this listener is generating token tool tips
 */
function TokenTooltipPointRecognitionListener(walker) {
    SiddhiQLListener.call(this);     // inherit default listener
    this.walker = walker;
    this.partitionCount = 0;
    return this;
}
TokenTooltipPointRecognitionListener.prototype = Object.create(SiddhiQLListener.prototype);
TokenTooltipPointRecognitionListener.prototype.constructor = TokenTooltipPointRecognitionListener;

TokenTooltipPointRecognitionListener.prototype.exitFunction_operation = function (ctx) {
    // Identifying eval scripts, window processors, stream processors, stream function, function executors, attribute aggregators for tooltips
    var namespaceCtx = ctx.function_namespace(0);
    var functionCtx = ctx.function_id(0);

    if (functionCtx) {
        var processorName = this.walker.utils.getTextFromANTLRCtx(functionCtx);
        var namespace;
        if (namespaceCtx) {
            namespace = this.walker.utils.getTextFromANTLRCtx(namespaceCtx);
        }

        if (processorName) {
            updateTokenDescription(this.walker, SiddhiEditor.constants.FUNCTION_OPERATION, {
                processorName: processorName, namespace: namespace
            }, functionCtx);
        }
    }
};

TokenTooltipPointRecognitionListener.prototype.exitFunction_name = function (ctx) {
    updateTokenDescription(this.walker, SiddhiEditor.constants.FUNCTION_OPERATION, {
        processorName: this.walker.utils.getTextFromANTLRCtx(ctx), namespace: undefined
    }, ctx);
};

TokenTooltipPointRecognitionListener.prototype.exitStream_id = function (ctx) {
    // Identifying sources for tooltips
    var sourceName = this.walker.utils.getTextFromANTLRCtx(ctx);
    var isInnerStream;

    if (ctx.parentCtx.inner) {
        isInnerStream = true;
    }

    if (sourceName) {
        updateTokenDescription(this.walker, (isInnerStream ? SiddhiEditor.constants.INNER_STREAMS : SiddhiEditor.constants.SOURCE), {
            sourceName: (isInnerStream ? "#" : "") + sourceName,
            partitionNumber: (isInnerStream ? this.partitionCount : undefined)
        }, ctx);
    }
};

TokenTooltipPointRecognitionListener.prototype.exitTrigger_name = function (ctx) {
    // Identifying triggers for tooltips
    var triggerName = this.walker.utils.getTextFromANTLRCtx(ctx);

    if (triggerName) {
        updateTokenDescription(this.walker, SiddhiEditor.constants.TRIGGERS, {
            triggerName: triggerName
        }, ctx);
    }
};

TokenTooltipPointRecognitionListener.prototype.exitPartition = function () {
    // Incrementing the partition count so that the correct index in used in the next partition
    this.partitionCount++;
};

/**
 * Update the token tool tip point data in the ANTLR walker
 *
 * @private
 * @param walker The walker of which the token should be update
 * @param type The ty[e of tooltip
 * @param tooltipData Tooltip point data
 * @param ctx The ANTLR context. The row and column at which the tooltip should be added will be derived from this
 */
function updateTokenDescription(walker, type, tooltipData, ctx) {
    walker.tokenToolTipData.push({
        type: type,
        tooltipData: tooltipData,
        row: ctx.stop.line - 1,
        column: ctx.stop.column + 1
    });
}

exports.TokenTooltipPointRecognitionListener = TokenTooltipPointRecognitionListener;
