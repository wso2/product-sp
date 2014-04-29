/**
 * wso2.xml namespace
 */
(function() {
    var ns = wso2.xml = wso2.xml || {};
    var uns = ns.utils = ns.utils || {};

    var xml2bf = function(node, reduce) {
        if (typeof node !== "object") return null;
        var json = {};
        (function process(node, obj, ns) {
            var i;
            if (node.nodeType === 3) {
                if (!node.nodeValue.match(/[\S]+/)) return;
                if (obj["$"] instanceof Array) {
                    obj["$"].push(node.nodeValue);
                } else if (obj["$"] instanceof Object) {
                    obj["$"] = [obj["$"], node.nodeValue];
                } else {
                    obj["$"] = node.nodeValue;
                }
            } else if (node.nodeType === 1) {
                var p = {};
                var activeNS = ns["$"] ? { "$" : true } : {};
                var nodeName = node.nodeName;
                if (nodeName.indexOf(":") != -1)
                    activeNS[nodeName.substr(0, nodeName.indexOf(":"))] = true;
                for (i = 0; node.attributes && i < node.attributes.length; i++) {
                    var attr = node.attributes[i];
                    var name = attr.nodeName;
                    var value = attr.nodeValue;
                    if (name === "xmlns") {
                        ns["$"] = value;
                        activeNS["$"] = true;
                    } else if (name.indexOf("xmlns:") === 0) {
                        ns[name.substr(name.indexOf(":") + 1)] = value;
                    } else if (name.indexOf(":") != -1) {
                        p["@" + name] = value;
                        activeNS[name.substr(0, name.indexOf(":"))] = true;
                    } else {
                        p["@" + name] = value;
                    }
                }
                var namespace = reduce ? activeNS : ns;
                for (var prefix in namespace) {
                    p["@xmlns"] = p["@xmlns"] || {};
                    p["@xmlns"][prefix] = ns[prefix];
                }
                if (obj[nodeName] instanceof Array) {
                    obj[nodeName].push(p);
                } else if (obj[nodeName] instanceof Object) {
                    obj[nodeName] = [obj[nodeName], p];
                } else {
                    obj[nodeName] = p;
                }
                for (i = 0; i < node.childNodes.length; i++) {
                    process(node.childNodes[i], p, ns);
                }
            } else if (node.nodeType === 9) {
                for (i = 0; i < node.childNodes.length; i++) {
                    process(node.childNodes[i], obj, ns);
                }
            }
        })(node, json, {});
        return json;
    };

    var bf2xml = function (json) {
        if (typeof json !== "object") return null;
        for (var lname in json) {
            if (json.hasOwnProperty(lname) && lname.indexOf("@") == -1) {
                return (function(lname, child, ns) {
                    var attributes = "";
                    var body = "";
                    if (child instanceof Array) {
                        for (var i = 0; i < child.length; i++) {
                            var nsc = {};
                            for (var p in ns) {
                                if (ns.hasOwnProperty(p)) nsc[p] = ns[p];
                            }
                            body += arguments.callee(lname, child[i], nsc);
                        }
                        return body;
                    } else if (typeof child === "object") {
                        var text = "";
                        var el = "<" + lname;
                        for (var key in child) {
                            if (child.hasOwnProperty(key)) {
                                var obj = child[key];
                                if (key === "$") {
                                    text += obj;
                                } else if (key === "@xmlns") {
                                    for (var prefix in obj) {
                                        if (obj.hasOwnProperty(prefix)) {
                                            if (prefix === "$") {
                                                if (ns[prefix] !== obj[prefix]) {
                                                    attributes += " " + "xmlns=\"" + obj[prefix] + "\"";
                                                    ns[prefix] = obj[prefix];
                                                }
                                            } else if (!ns[prefix] || (ns[prefix] !== obj[prefix])) {
                                                attributes += " xmlns:" + prefix + "=\"" + obj[prefix] + "\"";
                                                ns[prefix] = obj[prefix];
                                            }
                                        }
                                    }
                                } else if (key.indexOf("@") === 0) {
                                    attributes += " " + key.substring(1) + "=\"" + obj + "\"";
                                } else {
                                    body += arguments.callee(key, obj, ns);
                                }
                            }
                        }
                        body = text + body;
                        return (body !== "") ? el + attributes + ">" + body + "</" + lname + ">" : el + attributes + "/>";
                    }
                }(lname, json[lname], {}));
            }
        }
        return null;
    };

    var xml2DOM = function(content) {
        //create new document from string
        var xmlDoc;
        // Parser is browser specific.
        if ($.browser.msie) {
            //create a DOM from content string.
            xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
            if (content != null && content != "")
                xmlDoc.loadXML(content);
        } else {
            //create a DOMParser to get DOM from content string.
            var xmlParser = new DOMParser();
            if (content != null && content != "")
                xmlDoc = xmlParser.parseFromString(content, "text/xml");
        }
        return xmlDoc;
    };

    uns.xml2bf = xml2bf;
    uns.bf2xml = bf2xml;
    uns.xml2DOM = xml2DOM;
}());
