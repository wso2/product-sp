/**
 * wso2.bam.gauges.BAMConfigurationDS namespace
 */
(function() {
    var service = "BAMConfigurationDS";

    var ns = wso2.bam.gauges[service] = wso2.bam.gauges[service] || {};
    var uns = ns.utils = ns.utils || {};

    var getAllServers = function(options, errorCallback, receivers, timer) {
        options = options || {};
        options.postValidate = options.postValidate ? options.postValidate : function(that, data) {
            if(!data || !data.servers) {
                that.errorCallback(service, "getServerList", "POST_VALIDATE", "NO_DATA");
                return false;
            }
            var servers = data.servers.server;
            if ((servers instanceof Array && servers.length <= 0) || $.isEmptyObject(servers)) {
                that.errorCallback(service, "getServerList", "POST_VALIDATE", "NO_SERVERS");
                return false;
            } else {
                return true;
            }
        };
        return wso2.bam.gauges.utils.getJSONData(options, errorCallback,
            //filter function
                options.wso2vis ? function(json) {
                    var servers = json.servers.server;
                    if(servers) {
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
                        action : "urn:getAllServers"
                    };
                }, [], this);
    };

    /**
     * This method invokes getServiceList operation
     * @param options (null | options object). It can contain following
     * <ul>
     *  <li>preValidate : function(provider) { return true | false }
     *  <li>wso2vis : (true | false) filter data for wso2vis environment
     * </ul>
     * @param errorCallback (null | Function) a callback function to be called for errors at the provider
     * @param receivers data receivers. It can be
     * <ul>
     * <li>Function(data) {}</li>
     * <li>wso2vis.s.Subscriber</li>
     * <li>[func,... Subscriber,... [subscriber, filter], ....] where 
     * subscriber = func | Subscriber and filter = (func | Filter)</li>
     * </ul>
     * @param timer (wso2vis.t.Timer | interval in milliseconds | null)
     * @param serverIdFn callback function to retrieve serverId while the operation is being invoked.
     * @param that context where function get called
     * @return (null | wso2vis.t.Timer) depending on the value of timer parameter.
     */
    var getAllServices = function(options, errorCallback, receivers, timer, serverIdFn, that) {
        options = options || {};
        options.preValidate = options.preValidate ? options.preValidate : function(that) {
            var args = that.args;
            var json = args[1].apply(args[3], args[2]);
            var sid = parseInt(json.payload.getAllServices.serverID.$);
            if(!sid || sid == -1) {
                that.errorCallback("BAMConfigurationDS", "getServiceList", "PRE_VALIDATE", "NO_SERVER");
                return false;
            } else {
                return true;
            }
        };
        options.postValidate = options.postValidate ? options.postValidate : function(that, data) {
            if(!data || !data.services) {
                that.errorCallback(service, "getServiceList", "POST_VALIDATE", "NO_DATA");
                return false;
            }
            var services = data.services.service;
            if ((services instanceof Array && services.length <= 0) || $.isEmptyObject(services)) {
                that.errorCallback(service, "getServiceList", "POST_VALIDATE", "NO_SERVICES");
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
                        action : "urn:getAllServices",
                        payload : {
                            getAllServices : {
                                serverID : {
                                    $ : serverIdFn.call(that).toString()
                                }
                            }
                        }
                    };
                }, [serverIdFn, that], this);
    };

    ns.getAllServers = getAllServers;
    ns.getAllServices = getAllServices;
})();
