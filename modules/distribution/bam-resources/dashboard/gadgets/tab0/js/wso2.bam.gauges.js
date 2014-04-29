/**
 * wso2.bam.gauges and wso2.bam.gauges.utils namespaces
 */
(function() {
    var ns = wso2.bam.gauges = wso2.bam.gauges || {};
    var uns = ns.utils = ns.utils || {};

    var getAjaxProcessor = function(type) {
        if (type == "json") {
            return "carbon/gauges/jsp/json_ajaxprocessor.jsp";
        }
    };

    var getHelpsRegPath = function() {
        return "registry/resource/_system/config/repository/dashboards/gadgets/help/";
    };

    var getRequestURL = function(baseUrl, paramFn, args, that) {
        var url = baseUrl;
        var first = true;
        $.each(paramFn.apply(that, args), function(key, value) {
            value = key == "payload" ? encodeURIComponent(wso2.utils.base64(wso2.xml.utils.bf2xml(value))) : value;
            if (first) {
                url += "?" + key + "=" + value;
                first = false;
            } else {
                url += "&" + key + "=" + value;
            }
        });
        return url;
    };

    var getCallbackFilter = function(provider, filter) {
        var receiver = null;
        $.each(provider.drList, function(index, value) {
            value = value instanceof wso2vis.f.Filter ? value.filterData : value;
            if (value === filter) {
                receiver = value;
                return false;
            }
        });
        return receiver;
    };

    var getData = function(options, provider, filter, receivers, timer) {
        var subscriber;
        if (timer) {
            timer = !(timer instanceof wso2vis.u.Timer) ? new wso2vis.u.Timer(timer) : timer;
            timer.tick = function() {
                provider.pullData();
            };
        }
        filter = !(filter instanceof wso2vis.f.Filter) ? new wso2vis.f.CallbackFilter(filter) : filter;
        if (receivers instanceof Array) {
            $.each(receivers, function(index, value) {
                var f = filter;
                if (value instanceof Array) {
                    subscriber = value[0];
                    var val = value[1];
                    if (!val) f = null;
                    else if (val instanceof wso2vis.f.Filter) f = val;
                    else if (val instanceof Function) f = new wso2vis.f.CallbackFilter(val);
                } else {
                    subscriber = value;
                }
                if (!(subscriber instanceof wso2vis.s.Subscriber)) {
                    var fun = subscriber;
                    subscriber = new wso2vis.s.Subscriber();
                    subscriber.pushData = fun;
                }

                if (f) {
                    var existingFilter = getCallbackFilter(provider, f);
                    if (existingFilter != null) {
                        existingFilter.addDataReceiver(subscriber);
                    } else {
                        provider.addDataReceiver(f);
                        f.addDataReceiver(subscriber);
                    }
                } else {
                    provider.addDataReceiver(subscriber);
                }
            });
        } else {
            if (!(receivers instanceof wso2vis.s.Subscriber)) {
                subscriber = new wso2vis.s.Subscriber();
                subscriber.pushData = receivers;
            } else {
                subscriber = receivers;
            }
            provider.addDataReceiver(filter);
            filter.addDataReceiver(subscriber);
        }
        provider.initialize();
        if(!timer) {
            provider.pullData();
            return null;
        }
        return timer;
    };

    /**
     * Get Request Response and Fault count for a server
     * @param serverId server id of the monitoring server
     * @param receivers receivers can be once of the following options
     *      1. Callback function
     *      2. wso2vis.s.Subscriber instance
     *      3. An array in the format [callback, boolean] where boolean specify whether to get default filtered data
     *      4. An array in the format [callback, filter] where filter is a custom filtering function
     *      5. An array of above 1-4 options
     * @param timer timer is the refreshing timer instance of wso2vis.u.Timer or just the time in milliseconds
     * @return if user has specified the timer parameter, then wso2vis.u.Timer object will be returned, otherwise null
     * will be returned.
     */
    var getJSONData = function(options, errorCallback, filter, receivers, timer, paramsFn, args, that) {
        var provider = new wso2vis.p.ProviderGETJSON(
                getRequestURL, [getAjaxProcessor("json"), paramsFn, args, that], this, true);

        if(options) {
            provider.preValidate = options.preValidate instanceof Function ? options.preValidate : provider.preValidate;
            provider.postValidate = options.postValidate instanceof Function ? options.postValidate : provider.postValidate;
            if(options.flowStart) provider.flowStart = options.flowStart;
        }
        provider.errorCallback = errorCallback instanceof Function ? errorCallback : provider.errorCallback;
        return getData(options, provider, filter, receivers, timer);
    };

    var simplifyBFJSON = function(json) {
        return json;
    };

    uns.getAjaxProcessor = getAjaxProcessor;
    uns.getHelpsRegPath = getHelpsRegPath;
    uns.getJSONData = getJSONData;
    uns.simplifyBFJSON = simplifyBFJSON;
})();
