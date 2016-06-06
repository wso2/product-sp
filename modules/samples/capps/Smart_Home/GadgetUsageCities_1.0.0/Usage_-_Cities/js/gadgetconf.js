var config = {

	providerConfig : {
		tableName : "SPEED",
		schema: [
			"metro_area" : "ordinal",
			"min_usage" : "linear",
		],
		timeFrom : 123456,
		timeTo : 123456,
		filter: "foo"
	},
	chartConfig: {
		x: "metro_area",
        charts: [{ type: "bar", y: "avg_usage" }],
        padding: { "top": 20, "left": 50, "bottom": 20, "right": 80 },
        range: false,
        height: 300
	}

};