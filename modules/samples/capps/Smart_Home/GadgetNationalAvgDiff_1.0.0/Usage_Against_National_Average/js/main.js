var views = [{
    id: "chart-0",
    schema: [{
        "metadata": {
            "names": ["state", "avg_usage_difference"],
            "types": ["ordinal", "linear"]
        }
    }],
    chartConfig: {
        x: "state",
        charts: [{ type: "map", y: "avg_usage_difference", mapType: "usa" }],
        range: false,
        width:800,
        renderer: "canvas",
        helperUrl : "../../portal/templates/geojson/usaInfo.json",
        geoCodesUrl : "../../portal/templates/geojson/usa.json"
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
        var TABLE_NAME = "STATE_USAGE_DIFFERENCE";
        var client = new AnalyticsClient().init(null, null, SERVER_URL);
        var params = {
            tableName: TABLE_NAME,
            start: 0,
            count: 10
        };
        client.getRecordsByRange(
            params,
            function(response) {
                var results = [];
                var data = JSON.parse(response.message);
                data.forEach(function(record, i) {
                    var values = record.values;
                    var result = [values["state"], values["avg_usage_difference"]];
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

    $("#update").click(function() {
        var data = [
            ["Colombo", 23.4, 45.6, 25.6],
            ["Galle", 12, 65, 56]
        ];
        wso2gadgets.onDataReady(data,"append");
    });

    $("#next").click(function() {
        wso2gadgets.load("chart-1");
    });

    $("#prev").click(function() {
        wso2gadgets.load("chart-0");
    });



});
