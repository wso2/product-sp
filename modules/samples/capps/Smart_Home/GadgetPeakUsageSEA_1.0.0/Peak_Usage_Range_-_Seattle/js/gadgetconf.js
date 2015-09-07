var gadgetConfig = {
    "id": "Peak_Usage_Range_-_Seattle",
    "title": "Peak Usage Range - Seattle",
    "datasource": "PEAK_DEVICE_USAGE_RANGE",
    "type": "batch",
    "columns": [ {
        "name": "usage_range",
        "type": "FLOAT"
    },{
        "name": "house_id",
        "type": "INTEGER"
    }],
    "maxUpdateValue": 0,
    "chartConfig": {
        "yAxis": 0,
        "xAxis": 1,
        "chartType": "area"
    },
    "domain": "carbon.super"
};