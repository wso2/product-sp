/**
 * wso2.ui.html namespace
 */
(function() {
    var ns = wso2.ui.html = wso2.ui.html || {};
    var uns = ns.utils = ns.utils || {};

    var getHTMLSelect = function(json, keyFn, valueFn, options) {
        var html = "";
        if (options) {
            if (options.wrap) {
                html = "<select class=\"select";
                if (options.classes) {
                    $.each(options.classes, function(index, value) {
                        html += " " + value;
                    });
                }
                html += "\"";
                html += options.id ? " id=\"" + options.id + "\">" : ">";
            }
            if (options.optional) {
                html += "<option value=\"" + options.optional[0] + "\">" + options.optional[1] + "</option>";
            }
        }
        if (json instanceof Array) {
            $.each(json, function(index, value) {
                var key = keyFn ? keyFn(index, value) : i;
                value = valueFn ? valueFn(index, value) : value;
                html += "<option value=\"" + key + "\">" + value + "</option>";
            });
        } else {
            $.each(json, function(key, value) {
                key = keyFn ? keyFn(key, value) : key;
                value = valueFn ? valueFn(key, value) : value;
                html += "<option value=\"" + key + "\">" + value + "</option>";
            });
        }
        html += (options && options.wrap) ? "</select>" : "";
        return html;

    };

    uns.getHTMLSelect = getHTMLSelect;
}());
