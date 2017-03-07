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

define("ace/mode/siddhi", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/siddhi_highlight_rules", "ace/range", "ace/mode/folding/siddhi"],
    function (require, exports, module) {

    "use strict";   // JS strict mode

    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var SiddhiHighlightRules = require("./siddhi_highlight_rules").SiddhiHighlightRules;
    var SiddhiFoldMode = require("./folding/siddhi").SiddhiFoldMode;

    var Mode = function () {
        this.HighlightRules = SiddhiHighlightRules;
        this.foldingRules = new SiddhiFoldMode();
    };
    oop.inherits(Mode, TextMode);

    (function () {
        this.lineCommentStart = "--";
        this.$id = "ace/mode/siddhi";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});
