/**
 * wso2 namespace
 */
var wso2 = wso2 || {};

(function() {
    var uns = wso2.utils = wso2.utils || {};

    var version = {
        major : 1,
        minor : 0
    };

    var getFirstObj = function(json) {
        var first = null;
        $.each(json, function(key, value) {
            first = { key : key, value : value };
            return false;
        });
        return first;
    };

    var getLastObj = function(json) {
        var lkey;
        $.each(json, function(key, value) {
            lkey = key;
        });
        return { key : lkey, value : json[lkey] };
    };

    var getArray = function(json, callback, that) {
        that = that ? that : this;
        var array = new Array();
        $.each(json, function(key, value) {
            array.push(callback ? callback.call(that, key, value) : value);
        });
        return array;
    };

    var base64 = function (input) {
        // Not strictly base64 returns - nulls represented as "~"
        input = typeof input === "string" ? input : JSON.stringify(input);
        if (input == null) return "~";
        var base64Map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        var length = input.length;
        var output = "";
        var p = [];
        var charCode;
        var i = 0;
        var padding = 0;
        while (charCode = input.charCodeAt(i++)) {
            // convert to utf-8 as we fill the buffer
            if (charCode < 0x80) {
                p[p.length] = charCode;
            } else if (charCode < 0x800) {
                p[p.length] = 0xc0 | (charCode >> 6);
                p[p.length] = 0x80 | (charCode & 0x3f);
            } else if (charCode < 0x10000) {
                p[p.length] = 0xe0 | (charCode >> 12);
                p[p.length] = 0x80 | ((charCode >> 6) & 0x3f);
                p[p.length] = 0x80 | (charCode & 0x3f);
            } else {
                p[p.length] = 0xf0 | (charCode >> 18);
                p[p.length] = 0x80 | ((charCode >> 12) & 0x3f);
                p[p.length] = 0x80 | ((charCode >> 6) & 0x3f);
                p[p.length] = 0x80 | (charCode & 0x3f);
            }

            if (i == length) {
                while (p.length % 3) {
                    p[p.length] = 0;
                    padding++;
                }
            }

            if (p.length > 2) {
                output += base64Map.charAt(p[0] >> 2);
                output += base64Map.charAt(((p.shift() & 3) << 4) | (p[0] >> 4));
                output += (padding > 1) ? "=" : base64Map.charAt(((p.shift() & 0xf) << 2) | (p[0] >> 6));
                output += (padding > 0) ? "=" : base64Map.charAt(p.shift() & 0x3f);
            }

        }

        return output;
    };

    wso2.version = version;
    uns.getFirstObj = getFirstObj;
    uns.getLastObj = getLastObj;
    uns.getArray = getArray;
    uns.base64 = base64;
})();
