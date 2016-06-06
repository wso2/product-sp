var views = [{
    id: "chart-0",
    schema: [{
        "metadata": {
            "names": ["house_id", "usage_range"],
            "types": ["ordinal", "linear"]
        }
    }],
    chartConfig: {
        x: "house_id",
        charts: [{ type: "area", y: "usage_range"}],
        padding: { "top": 20, "left": 50, "bottom": 20, "right": 80 },
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
        var TABLE_NAME = "PEAK_DEVICE_USAGE_RANGE";
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
                  var result = [values["house_id"], values["usage_range"]];
                  results.push(result);
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
