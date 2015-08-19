var gadgetConfig = {
    "id": "CountryVsRequest",
    "title": "Country Vs Request",
    "datasource": "HTTPD_COUNTRY_LOG_SUMMARY",
    "type": "batch",
    "columns": [{"name": "countryCode", "type": "STRING"}, {
        "name": "countryName",
        "type": "STRING"
    }, {"name": "req_count", "type": "INTEGER"}],
    "maxUpdateValue": 10,
    "chartConfig": {"chartType": "table", "xAxis": 0}
};