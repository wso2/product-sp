var gadgetConfig = {
    "id": "High_Traffic_Country",
    "title": "High Traffic Country",
    "datasource": "HTTPD_COUNTRY_LOG_SUMMARY",
    "type": "batch",
    "columns": [{"name": "countryCode", "type": "STRING"}, {
        "name": "countryName",
        "type": "STRING"
    }, {"name": "req_count", "type": "INTEGER"}],
    "maxUpdateValue": 10,
    "chartConfig": {"yAxis": 2, "xAxis": 0, "chartType": "bar"}
};