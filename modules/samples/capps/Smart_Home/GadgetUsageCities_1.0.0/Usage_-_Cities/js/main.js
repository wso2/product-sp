var views = [{
    id: "chart-0",
    schema: [{
        "metadata": {
            "names": ["metro_area", "usage_type", "usage"],
            "types": ["ordinal", "linear", "linear"]
        }
    }],
    chartConfig: {
        x: "metro_area",
        charts: [{ type: "line", y: "usage", color : "usage_type" }],
        padding: { "top": 20, "left": 50, "bottom": 20, "right": 140 },
        range: false,
        height: 300
    },
    callbacks: [{
        type: "click",
        callback: function() {
            //wso2gadgets.load("chart-1");
            // alert("Clicked on bar chart of chart-0. You can implement your own callback handler for this.");
        }
    }],
    subscriptions: [{
        topic: "range-selected",
        callback: function(topic, data, subscriberData) {
            //do some stuff
        }
    }],
    data: function() {
        var SERVER_URL = "/portal/apis/analytics";
        var TABLE_NAME = "CITY_USAGE";
        var client = new AnalyticsClient().init(null, null, SERVER_URL);
        var params = {
            tableName: TABLE_NAME,
            start: 0,
            count: 100
        };
        client.getRecordsByRange(
            params,
            function(response) {
                var results = [];
                var data = JSON.parse(response.message);
                data.forEach(function(record, i) {
                    var values = record.values;
                    results.push([values["metro_area"], "Minimum Usage", values["min_usage"]]);
                    results.push([values["metro_area"], "Average Usage", values["avg_usage"]]);
                    results.push([values["metro_area"], "Maximum Usage", values["max_usage"]]);
                });
                console.log(results);
                //Call the framework to draw the chart with received data. Note that data should be in VizGrammar ready format
                wso2gadgets.onDataReady(results);
            },
            function(e) {
                //throw it to upper level
                onError(e);
            }
        );
    }
}];

$(function() {
    try {
        wso2gadgets.init("#canvas",views);
        var view = wso2gadgets.load("chart-0");
    } catch (e) {
        console.error(e);
    }

});
