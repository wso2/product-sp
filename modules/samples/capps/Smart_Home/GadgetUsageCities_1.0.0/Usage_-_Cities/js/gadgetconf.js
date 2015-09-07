var gadgetConfig = {
    "id": "usage-cities",
    "title": "usage-cities",
    "datasource": "CITY_USAGE",
    "type": "batch",
    "columns": [{
        "name": "max_usage",
        "type": "FLOAT"
    }, {
        "name": "metro_area",
        "type": "STRING"
    }, {
        "name": "avg_usage",
        "type": "FLOAT"
    }, {
        "name": "min_usage",
        "type": "FLOAT"
    }],
    "maxUpdateValue": 0,
    "chartConfig": {
        "chartType": "line",
        "yAxis": [0, 2, 3],
        "xAxis": 1,
        "interpolationMode": "monotone"
    },
    "domain": "carbon.super"
};