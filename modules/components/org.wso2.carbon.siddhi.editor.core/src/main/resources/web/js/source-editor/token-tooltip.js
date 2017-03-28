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
 * Taken from ace editor demos and updated to suit the siddhi editor
 * Generates the token tool tips on mouse over
 */

/*
 * Generating tooltips when user hovers over a token
 * The tooltip content is set in siddhi-editor.js
 */
define(["ace/lib/dom", "ace/lib/oop", "ace/lib/event", "ace/range", "ace/tooltip", "./constants", "exports"],
    function (dom, oop, event, range, tooltip, constants, exports) {

    "use strict";   // JS strict mode

    function TokenTooltip(editor) {
        if (editor.tokenTooltip)
            return;
        tooltip.Tooltip.call(this, editor.container);
        editor.tokenTooltip = this;
        this.editor = editor;

        this.update = this.update.bind(this);
        this.onMouseMove = this.onMouseMove.bind(this);
        this.onMouseOut = this.onMouseOut.bind(this);
        event.addListener(editor.renderer.scroller, "mousemove", this.onMouseMove);
        event.addListener(editor.renderer.content, "mouseout", this.onMouseOut);
    }

    oop.inherits(TokenTooltip, tooltip.Tooltip);

    (function () {
        this.token = {};
        this.range = new range.Range();

        this.update = function () {
            this.$updateTooltipTimer = null;
            this.$showTooltipTimer = null;

            var r = this.editor.renderer;
            if (this.lastT - (r.timeStamp || 0) > 1000) {
                r.rect = null;
                r.timeStamp = this.lastT;
                this.maxHeight = window.innerHeight;
                this.maxWidth = window.innerWidth;
            }

            var canvasPos = r.rect || (r.rect = r.scroller.getBoundingClientRect());
            var offset = (this.x + r.scrollLeft - canvasPos.left - r.$padding) / r.characterWidth;
            var row = Math.floor((this.y + r.scrollTop - canvasPos.top) / r.lineHeight);
            var col = Math.round(offset);

            var screenPos = {row: row, column: col, side: offset - col > 0 ? 1 : -1};
            var session = this.editor.session;
            var docPos = session.screenToDocumentPosition(screenPos.row, screenPos.column);
            var token = session.getTokenAt(docPos.row, docPos.column);

            if (!token && !session.getLine(docPos.row)) {
                token = {
                    type: "",
                    value: "",
                    state: session.bgTokenizer.getState(0)
                };
            }
            if (!token) {
                session.removeMarker(this.marker);
                this.hide();
                return;
            }

            /*
             * This had been added to suit the siddhi editor implementation of token tool tips
             */
            var tokenText = token.tooltip;
            if (tokenText) {
                // Showing the tooltip using the tooltip specified in the token

                // Setting the tooltip html to the tooltip popup
                if (this.tokenText != tokenText) {
                    this.setHtml(tokenText);
                    this.width = this.getWidth();
                    this.height = this.getHeight();
                    this.tokenText = tokenText;
                }

                // Showing the tooltip
                if (!this.$showTooltipTimer) {
                    if (!this.isOpen) {
                        // Starting the timer to show tooltip if it not already open
                        var self = this;
                        this.$showTooltipTimer = setTimeout(function() {
                            self.show(null, self.x, self.y);
                        }, constants.TOOLTIP_SHOW_DELAY - 100);
                    } else {
                        // Updating the position if it is already open
                        this.show(null, this.x, this.y);
                    }
                }

                this.token = token;
                session.removeMarker(this.marker);
                this.range =
                    new range.Range(docPos.row, token.start, docPos.row, token.start + token.value.length);
                this.marker = session.addMarker(this.range, "ace_bracket", "text", false);
            } else {
                // Hiding the tooltip if there is no tooltip specified for the token
                clearTimeout(this.$showTooltipTimer);
                this.$showTooltipTimer = undefined;
                this.hide();
            }
        };

        this.onMouseMove = function (e) {
            this.x = e.clientX;
            this.y = e.clientY;
            if (this.isOpen) {
                this.lastT = e.timeStamp;
                this.setPosition(this.x, this.y);
            }
            if (!this.$updateTooltipTimer) {
                this.$updateTooltipTimer = setTimeout(this.update, 100);
            }
        };

        this.onMouseOut = function (e) {
            if (e && e.currentTarget.contains(e.relatedTarget)) {
                return;
            }
            this.hide();
            this.editor.session.removeMarker(this.marker);
            this.$updateTooltipTimer = clearTimeout(this.$updateTooltipTimer);
        };

        this.setPosition = function (x, y) {
            if (x + 10 + this.width > this.maxWidth) {
                x = window.innerWidth - this.width - 10;
            }
            if (y > window.innerHeight * 0.75 || y + 20 + this.height > this.maxHeight) {
                y = y - this.height - 30;
            }

            tooltip.Tooltip.prototype.setPosition.call(this, x + 10, y + 20);
        };

        this.destroy = function () {
            this.onMouseOut();
            event.removeListener(this.editor.renderer.scroller, "mousemove", this.onMouseMove);
            event.removeListener(this.editor.renderer.content, "mouseout", this.onMouseOut);
            delete this.editor.tokenTooltip;
        };

    }).call(TokenTooltip.prototype);

    exports.TokenTooltip = TokenTooltip;
});