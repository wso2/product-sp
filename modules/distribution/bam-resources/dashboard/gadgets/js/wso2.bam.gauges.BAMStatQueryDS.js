/**
 * wso2.bam.gauges.BAMStatQueryDS namespace
 */
(function() {
    var service = "BAMStatQueryDS";

    var ns = wso2.bam.gauges[service] = wso2.bam.gauges[service] || {};
    var uns = ns.utils = ns.utils || {};

    var getLatestDataForServices = function(options, errorCallback, receivers, timer, serviceIdFn, that) {
        options = options || {};
        options.preValidate = options.preValidate ? options.preValidate : function(that) {
            var args = that.args;
            var json = args[1].apply(args[3], args[2]);
            var services = json.payload.getLatestDataForServices.serviceID;
            if(!services || services.length < 1) {
                that.errorCallback(service, "getLatestDataForServices", "PRE_VALIDATE", "NO_SERVER");
                return false;
            } else {
                return true;
            }
        };
        options.postValidate = options.postValidate ? options.postValidate : function(that, data) {
            /*if(!data || !data.services) {
                that.errorCallback("POST_VALIDATE", that, "NO_DATA");
                return false;
            }
            var services = data.services.service;
            if (services && services.length > 0) {
                return true;
            } else {
                that.errorCallback("POST_VALIDATE", that, "NO_SERVICES");
                return false;
            }*/
            return true;
        };
        return wso2.bam.gauges.utils.getJSONData(options, errorCallback,
            //filter function
                options.wso2vis ? function(json) {
                    var data = json.serviceData.data;
                    if(data) {
                        data = data instanceof Array ? data : [data];
                        return wso2.bam.gauges.utils.simplifyBFJSON(data);
                    } else {
                        return [];
                    }
                } : function(json) { return json; },
                receivers, timer,
            //params function
                function(serviceIdFn, that) {
                    var ids = serviceIdFn.call(that);
                    var serviceIds = [];
                    for(var i=0;i<ids.length;i++) {
                        serviceIds.push({$ : ids[i]});
                    }
                    return {
                        service : service,
                        action : "urn:getLatestDataForServices",
                        payload : {
                            getLatestDataForServices : {
                                serviceID : serviceIds
                            }
                        }
                    };
                }, [serviceIdFn, that], this);
    };

    var getReqResFaultCountForServices = function(options, errorCallback, receivers, timer, serverIdFn, that) {
        options = options || {};
        options.preValidate = options.preValidate ? options.preValidate : function(that) {
            var args = that.args;
            var json = args[1].apply(args[3], args[2]);
            var sid = parseInt(json.payload.getReqResFaultCountForServices.serverID.$);
            if(!sid || sid == -1) {
                that.errorCallback(service, "getReqResFaultCountForServices", "PRE_VALIDATE", "NO_SERVER");
                return false;
            } else {
                return true;
            }
        };
        options.postValidate = options.postValidate ? options.postValidate : function(that, data) {
            if(!data || !data.services) {
                that.errorCallback(service, "getReqResFaultCountForServices", "POST_VALIDATE", "NO_DATA");
                return false;
            }
            var services = data.services.service;
            if ((services instanceof Array && services.length <= 0) || $.isEmptyObject(services)) {
                that.errorCallback(service, "getReqResFaultCountForServices", "POST_VALIDATE", "NO_SERVICES");
                return false;
            } else {
                return true;
            }
        };
        return wso2.bam.gauges.utils.getJSONData(options, errorCallback,
            //filter function
                options.wso2vis ? function(json) {
                    var services = json.services.service;
                    if(services) {
                        services = services instanceof Array ? services : [services];
                        return wso2.bam.gauges.utils.simplifyBFJSON(services);
                    } else {
                        return [];
                    }
                } : function(json) { return json; },
                receivers, timer,
            //params function
                function(serverIdFn, that) {
                    return {
                        service : service,
                        action : "urn:getReqResFaultCountForServices",
                        payload : {
                            getReqResFaultCountForServices : {
                                serverID : {
                                    $ : serverIdFn.call(that).toString()
                                }
                            }
                        }
                    };
                }, [serverIdFn, that], this);
    };

    ns.getLatestDataForServices = getLatestDataForServices;
    ns.getReqResFaultCountForServices = getReqResFaultCountForServices;
})();
