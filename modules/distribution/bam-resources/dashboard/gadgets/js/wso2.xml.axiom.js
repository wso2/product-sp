/**
 * wso2.xml.axiom namespace
 */
(function() {
    var xns = wso2.xml;
    var ns = xns.axiom = xns.axiom || {};
    var uns = ns.utils = ns.utils || {};

    var getQName = function(name, om) {
        var index = name.indexOf(":");
        var lname = name.substr(index + 1);
        var prefix = index != -1 ? name.substr(0, index) : null;
        var ns = prefix != null ?
                om.namespaces[prefix] : om.namespaces["$"];
        return new QName(ns, lname, prefix);
    };

    var getFirstEl = function(json) {
        var el = null;
        $.each(json, function(key, value) {
            if (key != "$" && key.indexOf("@") != 0) {
                el = { key : key, value : value };
                return false;
            }
        });
        return el;
    };

    var OMElement = function(json) {
        var firstObj = wso2.utils.getFirstObj(json);
        this.preprocessed = false;
        this.processed = false;
        this.attributes = [];
        this.children = [];
        if (firstObj != null) {
            this.json = firstObj.value;
            this.namespaces = firstObj.value["@xmlns"];
            this.qname = getQName(firstObj.key, this);
        } else {
            this.qname = null;
            this.json = null;
            this.namespaces = null;
        }
    };

    var OMAttribute = function(lname, ns, value) {
        this.qname = new QName(ns.getNamespaceURI(), lname, ns.getPrefix());
        this.value = value;
    };

    var OMNamespace = function(uri, prefix) {
        this.uri = uri;
        this.prefix = prefix;
    };

    var QName = function(arg1, arg2, arg3) {
        this.namespaceURI = null;
        this.localPart = null;
        this.prefix = null;
        if (arguments.length == 3) {
            this.namespaceURI = arg1;
            this.localPart = arg2;
            this.prefix = arg3;
        } else if (arguments.length == 2) {
            this.namespaceURI = arg1;
            this.localPart = arg2;
        } else {
            this.localPart = arg1;
        }
    };

    OMElement.prototype = {

        init : function(partial) {
            //we load attributes and children only when init() method is called
            if (this.processed || (partial && this.preprocessed)) return;
            //resetting the array to wipe-out any partial init data
            this.children = [];
            var that = this;
            $.each(this.json, function(key, value) {
                if (key.match(/^[@]/)) {
                    //this is an attribute
                    if (partial) return true;
                    if (key != "@xmlns") {
                        var qname = getQName(key.substr(1), that);
                        that.attributes.push(new OMAttribute(qname.getLocalPart(),
                                new OMNamespace(qname.getNamespaceURI(), qname.getPrefix()), value));
                    }
                } else if (key != "$") {
                    var json;
                    if (value instanceof Array) {
                        $.each(value, function(index, val) {
                            json = {};
                            json[key] = val;
                            that.children.push(new OMElement(json));
                            that.preprocessed = true;
                            if (partial) return false;
                        });
                    } else {
                        json = {};
                        json[key] = value;
                        that.children.push(new OMElement(json));
                        that.preprocessed = true;
                        return !partial;
                    }
                }
            });
            this.processed = partial ? this.processed : true;
        },

        addAttribute : function(lname, value, ns) {
            //TODO
        },

        addChild : function(om) {
            //TODO
        },

        findNamespace : function(uri, prefix) {
            return this.namespaces[prefix] == uri ? new OMNamespace(uri, prefix) : null;
        },

        findNamespaceURI : function(prefix) {
            var uri = this.namespaces[prefix];
            return uri ? new OMNamespace(uri, prefix) : null;
        },

        /**
         * return all attributes of the current Element
         */
        getAllAttributes : function() {
            this.init();
            return this.attributes;
        },

        /**
         * return an array of OMNamespace objects. Default namespace will have the prefix "$".
         */
        getAllDeclaredNamespaces : function() {
            return wso2.utils.getArray(this.namespaces, function(key, value) {
                return new OMNamespace(value, key);
            }, this);
        },

        getAttribute : function(qname) {
            this.init();
            var attr = null;
            $.each(this.attributes, function(index, value) {
                if (qname.equals(value.getQName())) {
                    attr = value;
                    return false;
                }
            });
            return attr;
        },

        getLocalName : function() {
            return this.qname.getLocalPart();
        },

        getNamespace : function() {
            return new OMNamespace(this.qname.getNamespaceURI(), this.qname.getPrefix());
        },

        getQName : function() {
            return this.qname;
        },
        /**
         * returns OMElement object
         */
        getFirstElement : function() {
            this.init(true);
            return this.children[0];
        },

        /**
         * returns an array of OMElement objects
         */
        getChildren : function() {
            this.init();
            return this.children;
        },

        getChildrenWithLocalName : function(lname) {
            this.init();
            var array = new Array();
            $.each(this.children, function(index, value) {
                if (value.getLocalName() == lname) array.push(value);
            });
            return array;
        },

        getChildrenWithName : function(qname) {
            this.init();
            var array = new Array();
            $.each(this.children, function(index, value) {
                if (qname.equals(value.getQName())) array.push(value);
            });
            return array;
        },

        getChildrenWithNamespaceURI : function(uri) {
            this.init();
            var array = new Array();
            $.each(this.children, function(index, value) {
                if (value.getNamespace().getNamespaceURI() == uri) array.push(value);
            });
            return array;
        },

        getDefaultNamespace : function() {
            return this.namespaces["$"] ? this.namespaces["$"] : null;
        },

        getFirstChildWithName : function(qname) {
            this.init();
            $.each(this.children, function(index, value) {
                if (value.getQName().equals(qname)) return value;
            });
            return null;
        },

        getText : function() {
            return this.json["$"];
        },

        removeAttribute : function(attr) {
            //TODO
        },

        resolveQName : function(qname) {
            return getQName(qname, this);
        },

        setLocalName : function(lname) {
            this.qname.localPart = lname;
        },

        setNamespace : function(ns) {
            var cp = this.namespaces[ns.getPrefix()];
            if (!cp) {
                this.qname.namespaceURI = ns.getNamespaceURI();
                this.qname.prefix = ns.getPrefix();
                this.namespaces[ns.getPrefix()] = ns.getNamespaceURI();
            } else if (cp != ns.getNamespaceURI()) {
                //TODO : this prefix already exists, we need to create a new prefix and add it
            }
        },

        setText : function(text) {
            this.json["$"] = text;
        }
    };

    OMAttribute.prototype = {
        getAttributeValue : function() {
            return this.value;
        },
        getLocalName : function() {
            return this.qname.getLocalPart();
        },
        getNamespace : function() {
            return new OMNamespace(this.qname.getNamespaceURI(), this.qname.getPrefix());
        },
        getQName : function() {
            return this.qname;
        },
        setAttributeValue : function(value) {
            this.value = value;
        },
        setLocalName : function(lname) {
            this.qname.localPart = lname;
        },
        setOMNamespace : function(ns) {
            this.qname.namespaceURI = ns.getNamespaceURI();
            this.qname.prefix = ns.getPrefix();
        }
    };

    OMNamespace.prototype = {
        getNamespaceURI : function() {
            return this.uri;
        },
        getPrefix : function() {
            return this.prefix;
        },
        equals : function(obj) {
            return (this.uri == obj.getNamespaceURI() && this.prefix == obj.getPrefix());
        }
    };

    QName.prototype = {
        getLocalPart : function() {
            return this.localPart;
        },
        getNamespaceURI : function() {
            return this.namespaceURI;
        },
        getPrefix : function() {
            return this.prefix;
        },
        equals : function(obj) {
            return (this.localPart == obj.getLocalPart() && this.namespaceURI == obj.getNamespaceURI());
        }
    };

    ns.OMElement = OMElement;
    ns.OMAttribute = OMAttribute;
    ns.OMNamespace = OMNamespace;
    ns.QName = QName;
})();
