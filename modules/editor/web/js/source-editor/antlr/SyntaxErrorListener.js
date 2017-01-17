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

var ErrorListener = require("../antlr4-js-runtime/error/ErrorListener").ErrorListener;

/**
 * The Syntax Error Listener prototype constructor
 * Inherits from ErrorListener in the antlr4 JS runtime
 *
 * Only recognizing syntax errors is done by this listener
 * Errors are added to the ace editor gutter in the main js
 *
 * Semantic errors such as invalid extension names is not recognized by this listener
 * These errors are recognized using a server side ajax call to validate using the Siddhi engine
 *
 * @constructor
 * @param walker The walker for which this listener is listening for errors
 */
function SyntaxErrorListener(walker) {
    ErrorListener.call(this);
    this.walker = walker;
    return this;
}
SyntaxErrorListener.prototype = Object.create(ErrorListener.prototype);
SyntaxErrorListener.prototype.constructor = SyntaxErrorListener;

SyntaxErrorListener.prototype.syntaxError = function (recognizer, offendingSymbol, line, column, msg, e) {
    this.walker.syntaxErrorList.push({
        row: line - 1,
        column: column,
        text: msg,
        type: "error"
    });
};

exports.SyntaxErrorListener = SyntaxErrorListener;