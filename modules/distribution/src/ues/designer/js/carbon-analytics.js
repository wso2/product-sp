/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This Javascript module exposes all the data analytics API as Javascript methods. This can be used
 * to develop custom webapps which use Analytics API.
 */

function AnalyticsClient() {
    var TYPE_CLEAR_INDICES = 1;
    var TYPE_CREATE_TABLE = 2;
    var TYPE_DELETE_BY_ID = 3;
    var TYPE_DELETE_BY_RANGE = 4;
    var TYPE_DELETE_TABLE = 5;
    var TYPE_GET_INDICES = 6;
    var TYPE_GET_RECORD_COUNT = 7;
    var TYPE_GET_BY_ID = 8;
    var TYPE_GET_BY_RANGE = 9;
    var TYPE_LIST_TABLES = 10;
    var TYPE_GET_SCHEMA = 11;
    var TYPE_PUT_RECORDS = 12;
    var TYPE_SEARCH = 13;
    var TYPE_SEARCH_COUNT = 14;
    var TYPE_SET_INDICES = 15;
    var TYPE_SET_SCHEMA = 16;
    var TYPE_TABLE_EXISTS = 17;
    var TYPE_WAIT_FOR_INDEXING = 18;
    var TYPE_PAGINATION_SUPPORTED = 19;
    var TYPE_DRILLDOWN_CATEGORIES = 20;
    var TYPE_DRILLDOWN_SEARCH = 21;
    var TYPE_DRILLDOWN_SEARCH_COUNT = 22;
    var HTTP_GET = "GET";
    var HTTP_POST = "POST";
    var DATA_TYPE_JSON = "json";
    var CONTENT_TYPE_JSON = "application/json";
    var AUTHORIZATION_HEADER = "Authorization";
    this.url;

    this.listTables = function (username, password, callback_func) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_LIST_TABLES,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback_func(data);
                   }
               });
    }

    this.createTable = function (username, password, callback, tableName) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_CREATE_TABLE + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.deleteTable = function (username, password, callback, tableName) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_DELETE_TABLE + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.createIndices = function (username, password, callback, indexInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_SET_INDICES + "&tableName=" + indexInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(indexInfo["indices"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.clearIndices = function (username, password, callback, tableName) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_CLEAR_INDICES + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.getRecordsByRange = function (username, password, callback, rangeInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_BY_RANGE + "&tableName=" + rangeInfo["tableName"] +
                        "&timeFrom=" + rangeInfo["timeFrom"] + "&timeTo=" + rangeInfo["timeTo"] +
                        "&start=" + rangeInfo["start"] + "&count=" + rangeInfo["count"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.getRecordByIds = function (username, password, callback, recordsInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_BY_ID + "&tableName=" + recordsInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(recordsInfo["ids"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.getRecordCount = function (username, password, callback, tableName) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_RECORD_COUNT + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.deleteRecordsByIds = function (username, password, callback, recordsInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_DELETE_BY_ID + "&tableName=" + recordsInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(recordsInfo["ids"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.deleteRecordsByRange = function (username, password, callback, rangeInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_DELETE_BY_RANGE + "&tableName=" + rangeInfo["tableName"]
                        + "&timeFrom=" + rangeInfo["timeFrom"] + "&timeTo=" + rangeInfo["timeTo"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.insertRecords = function (username, password, callback, recordsInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_PUT_RECORDS + "&tableName=" + recordsInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(recordsInfo["records"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.searchCount = function(username, password, callback, queryInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_SEARCH_COUNT + "&tableName=" + queryInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(queryInfo["searchParams"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.search = function (username, password, callback, queryInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_SEARCH + "&tableName=" + queryInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(queryInfo["searchParams"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.setSchema = function (username, password, callback, schemaInfo) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_SET_SCHEMA + "&tableName=" + schemaInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(schemaInfo["schema"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.getSchema = function (username, password, callback, tableName) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_SCHEMA + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.PaginationSupported = function (username, password, callback) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_PAGINATION_SUPPORTED,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.waitForIndexing = function (username, password, callback) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_WAIT_FOR_INDEXING,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.drillDownCategories = function (username, password, callback, drilldownReq) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_DRILLDOWN_CATEGORIES + "&tableName=" + drilldownReq["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(drilldownReq["drillDownInfo"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.drillDownSearch = function (username, password, callback, drillDownReq) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_DRILLDOWN_SEARCH + "&tableName=" + drillDownReq["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(drillDownReq["drillDownInfo"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    this.drillDownSearchCount = function (username, password, callback, drillDownReq) {
        var authHeader = generateBasicAuthHeader(username, password);
        $.ajax({
                   url: this.url + "?type=" + TYPE_DRILLDOWN_SEARCH_COUNT + "&tableName=" + schemaInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(schemaInfo["drillDownInfo"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    function generateBasicAuthHeader(username, password) {
        return "Authorization:Basic " + btoa(username + ":" + password);
    }
}

AnalyticsClient.prototype.init = function (svrUrl) {
    this.url = svrUrl;
    return this;
}

AnalyticsClient.prototype.init = function () {
    this.url = "https://localhost:9443/carbon/jsservice/jsservice_ajaxprocessor.jsp";
    return this;
}
