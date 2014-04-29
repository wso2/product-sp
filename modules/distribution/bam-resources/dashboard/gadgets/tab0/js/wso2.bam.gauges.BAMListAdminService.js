/**
 * wso2.bam.gauges.BAMConfigurationDS namespace
 */
(function() {
    var service = "BAMListAdminService";

    var ns = wso2.bam.gauges[service] = wso2.bam.gauges[service] || {};
    var uns = ns.utils = ns.utils || {};

    var getServerListWithCategoryName = function(options, errorCallback, receivers, timer) {
        options = options || {};
        options.postValidate = options.postValidate ? options.postValidate : function(that, data) {
            if (!data) {
                that.errorCallback(service, "getServerListWithCategoryName", "POST_VALIDATE", "NO_DATA");
                return false;
            }
            data = new wso2.xml.axiom.OMElement(data);
            if (data.getChildren().length <= 0) {
                that.errorCallback(service, "getServerListWithCategoryName", "POST_VALIDATE", "NO_SERVERS");
                return false;
            } else {
                return true;
            }
        };
        return wso2.bam.gauges.utils.getJSONData(options, errorCallback,
            //filter function
                options.wso2vis ? function(json) {
                    var servers = json.servers.server;
                    if (servers) {
                        servers = servers instanceof Array ? servers : [servers];
                        return wso2.bam.gauges.utils.simplifyBFJSON(servers);
                    } else {
                        return [];
                    }
                } : function(json) { return json; },
                receivers, timer,
            //params function
                function() {
                    return {
                        service : service,
                        action : "urn:getServerListWithCategoryName",
                        payload : {
                            "xsd:getServerListWithCategoryName": {
                                "@xmlns": {
                                    "xsd": "http://org.apache.axis2/xsd"
                                }
                            }
                        }
                    };
                }, [], this);
    };

    ns.getServerListWithCategoryName = getServerListWithCategoryName;
})();
