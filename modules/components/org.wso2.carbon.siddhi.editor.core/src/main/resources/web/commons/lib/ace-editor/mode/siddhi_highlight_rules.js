/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define("ace/mode/siddhi_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (require, exports, module) {

    "use strict";   // JS strict mode

    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var SiddhiHighlightRules = function () {
        var keywords = "STREAM|DEFINE|FUNCTION|TRIGGER|TABLE|PLAN|FROM|PARTITION|WINDOW|SELECT|GROUP|BY|" +
            "HAVING|INSERT|OVERWRITE|DELETE|UPDATE|RETURN|EVENTS|INTO|OUTPUT|EXPIRED|CURRENT|SNAPSHOT|" +
            "FOR|RAW|OF|AS|AT|OR|AND|IN|ON|IS|NOT|WITHIN|WITH|BEGIN|END|EVERY|LAST|ALL|FIRST|JOIN|" +
            "INNER|OUTER|RIGHT|LEFT|FULL|UNIDIRECTIONAL|YEARS|MONTHS|WEEKS|DAYS|HOURS|MINUTES|SECONDS|" +
            "MILLISECONDS|STRING|INT|LONG|FLOAT|DOUBLE|BOOL|OBJECT";

        var builtInConstants = "TRUE|FALSE|NULL";

        var builtInBooleanConstants = "TRUE|FALSE";

        var builtInTypes = "STRING|INT|LONG|FLOAT|DOUBLE|BOOL|OBJECT";

        var keywordMapper = this.createKeywordMapper({
            "keyword": keywords,
            "constant.language": builtInConstants,
            "constant.language.boolean": builtInBooleanConstants,
            "support.type": builtInTypes
        }, "identifier", true);

        this.$rules = {
            "start": [
                {
                    token: "annotation.plan",
                    regex: "@Plan\\s*\\:\\s*\\w+"
                },
                {
                    token: "annotation.common",
                    regex: "@\\w+"
                },
                {
                    token: "comment.line",
                    regex: "--.*$"
                },
                {
                    token: "comment.block",
                    start: "/\\*",
                    end: "\\*/"
                },
                {
                    token: "string.quoted.double",
                    regex: '".*?"'
                },
                {
                    token: "string.quoted.single",
                    regex: "'.*?'"
                },
                {
                    token: "constant.numeric",
                    regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
                },
                {
                    token: keywordMapper,
                    regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
                },
                {
                    token: "keyword.operator",
                    regex: "\\+|\\-|\\/|\\/\\/|%|<@>|@>|<@|&|\\^|~|<|>|<=|=>|==|!=|<>|=|->|@|#|\\?|\\[|\\]|\\(|\\)|\\?|:|;|,|\\."
                },
                {
                    token: "paren.lparen",
                    regex: "\\("
                },
                {
                    token: "paren.rparen",
                    regex: "\\)"
                },
                {
                    token: "text",
                    regex: "\\s+"
                }
            ]
        };

        this.normalizeRules();
    };

    oop.inherits(SiddhiHighlightRules, TextHighlightRules);
    exports.SiddhiHighlightRules = SiddhiHighlightRules;
});