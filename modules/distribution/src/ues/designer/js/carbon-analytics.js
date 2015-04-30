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
    this.authHeader;

    /**
     * Lists all the tables.
     * @param callback The callback functions which has one argument containing the response data.
     */
    this.listTables = function (callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_LIST_TABLES,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Creates a table with a given name.
     * @param tableName The table name.
     * @param callback The callback function which has one argument containing the response message.
     */
    this.createTable = function (tableName, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_CREATE_TABLE + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Delete a table with a given name.
     * @param tableName The table name.
     * @param callback The callback function which has one argument containing the response message.
     */
    this.deleteTable = function (tableName, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_DELETE_TABLE + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Clears  all the indexed data for a specific table.
     * @param tableName The table name of which the index data to be removed.
     * @param callback The callback function which has one argument containing the response message.
     */
    this.clearIndexData = function (tableName, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_CLEAR_INDICES + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password != null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Gets the records given the table name and the timestamp range and pagination information.
     * @param rangeInfo Information containing the table name, range and pagination information.
     *  e.g. rangeInfo = {
     *          tableName : "TEST",
     *          timeFrom : 243243245354532,
     *          timeTo : 364654656435343,
     *          start : 0,
     *          count : 10,
     *      }
     * @param callback The callback function which has one argument containing the response message.
     */
    this.getRecordsByRange = function (rangeInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_BY_RANGE + "&tableName=" + rangeInfo["tableName"] +
                        "&timeFrom=" + rangeInfo["timeFrom"] + "&timeTo=" + rangeInfo["timeTo"] +
                        "&start=" + rangeInfo["start"] + "&count=" + rangeInfo["count"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }
    /**
     * Gets the records given the record Ids.
     * @param recordsInfo The object which contains the record ids.
     *  e.g. recordsInfo = {
     *          tableName : "TEST",
     *          ids : [ "id1", "id2", "id3"]
     *      }
     * @param callback The callback function which has one argument containing the response message.
     */
    this.getRecordByIds = function (recordsInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_BY_ID + "&tableName=" + recordsInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(recordsInfo["ids"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Returns the total record count.
     * @param tableName The table name
     * @param callback The callback function which has one argument containing the response message.
     */
    this.getRecordCount = function (tableName, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_RECORD_COUNT + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Delete records by records ids.
     * @param recordsInfo The object which contains the record information.
     *  e.g. recordsInfo = {
     *          tableName : "TEST",
     *          ids : [ "id1", "id2", "id3"]
     *      }
     * @param callback The callback function which has one argument containing the response message.
     */
    this.deleteRecordsByIds = function (recordsInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_DELETE_BY_ID + "&tableName=" + recordsInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(recordsInfo["ids"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Deletes the records given the time ranges.
     * @param rangeInfo The object information which contains the timestamp range.
     *  e.g rangeInfo = {
     *          tableName : "TEST",
     *          timeFrom : 12132143242422,
     *          timeTo : 3435353535335
     *      }
     * @param callback The callback function which has one argument containing the response message.
     */
    this.deleteRecordsByRange = function (rangeInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_DELETE_BY_RANGE + "&tableName=" + rangeInfo["tableName"]
                        + "&timeFrom=" + rangeInfo["timeFrom"] + "&timeTo=" + rangeInfo["timeTo"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Insert records given a table.
     * @param recordsInfo Records information containing the records array.
     *  e.g. recordsInfo = {
     *          tableName : "TEST",
     *          records : [
     *              {
     *                  values : {
     *                      "field1" : "value1",
     *                      "field2" : "value2"
     *                  }
     *              },
     *              {
     *                  values : {
     *                      "field1" : "value1",
     *                      "facetField" : [ "category", "subCategory", "subSubCategory" ]
     *                  }
     *              }
     *          ]
     * @param callback The callback function which has one argument containing the array of
     * ids of records inserted.
     */
    this.insertRecords = function (recordsInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_PUT_RECORDS + "&tableName=" + recordsInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(recordsInfo["records"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }
    /**
     * Search records in a given table using lucene queries.
     * @param queryInfo Query information which contains the table name and search parameters.
     *  e.g. queryInfo = {
     *          tableName : "TEST",
     *          searchParams : {
     *              query : "logFile : wso2carbon.log",
     *              start : 0,
     *              count : 10,
     *          }
     *      }
     * @param callback The callback function which has one argument containing the array of
     * matching records.
     */
    this.searchCount = function(queryInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_SEARCH_COUNT + "&tableName=" + queryInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(queryInfo["searchParams"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Returns the search count of records in a given table using lucene queries.
     * @param queryInfo Query information which contains the table name and search parameters.
     *  e.g. queryInfo = {
     *          tableName : "TEST",
     *          searchParams : {
     *              query : "logFile : wso2carbon.log",
     *              start : 0,
     *              count : 10,
     *          }
     *      }
     * @param callback The callback function which has one argument containing the number of
     * matched records
     */
    this.search = function (queryInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_SEARCH + "&tableName=" + queryInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(queryInfo["searchParams"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Sets the schema for a table.
     * @param schemaInfo The object which contains the schema information
     *  e.g. schemaInfo = {
     *          tableName : "TEST",
     *          schema : {
     *              columns : {
     *                  "column1" : {
     *                      "type" : "STRING",
     *                      "isIndex : true,
     *                      "isScoreParam" : false
     *                  }
     *              }
     *          }
     *      }
     * @param callback The callback function which has one argument containing the response message
     */
    this.setSchema = function (schemaInfo, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_SET_SCHEMA + "&tableName=" + schemaInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(schemaInfo["schema"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Gets the schema of a table.
     * @param tableName the table name.
     * @param callback The callback function which has one argument containing the table schema.
     */
    this.getSchema = function (tableName, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_GET_SCHEMA + "&tableName=" + tableName,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Returns if the underlying AnalyticsService supports pagination.
     * @param callback The callback function which has one argument containing true/false.
     */
    this.PaginationSupported = function (callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_PAGINATION_SUPPORTED,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Waits till the indexing completes.
     * @param callback The callback function which has one argument which contains the response message.
     */
    this.waitForIndexing = function (callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_WAIT_FOR_INDEXING,
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   type: HTTP_GET,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Returns the immediate sub categories of a facet field.
     * @param drilldownReq drilldown information.
     *  e.g. drillDownReq = {
     *          tableName : "TEST",
     *          drillDownInfo : {
     *              fieldName : "facetField1",
     *              categoryPath : [ "category", "subCategory"]
     *              query : "logFile : wso2carbon.log"
     *              scoreFunction : "sqrt(weight)"
     *          }
     *      }
     * @param callback The callback function which has one argument which contains the subcategories.
     */
    this.drillDownCategories = function (drilldownReq, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_DRILLDOWN_CATEGORIES + "&tableName=" + drilldownReq["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(drilldownReq["drillDownInfo"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Returns the records which match the drill-down query given the table.
     * @param drillDownReq The object which contains the drillDown information.
     *  e.g. drillDownReq = {
     *          tableName : "TEST",
     *          drillDownInfo : {
     *              categories : [
     *               {
     *                  fieldName : "facetField1",
     *                  path : ["A", "B", "C"]
     *              },
     *              {
     *                  fieldName : "facetField2",
     *                  path : [ "X", "Y", "Z"]
     *              }]
     *              query : "field1 : value1",
     *              recordStart : 0,
     *              recordCount : 50,
     *              scoreFunction : "scoreParamField * 2"
     *          }
     * @param callback The callback function which has one argument which contains the matching records
     */
    this.drillDownSearch = function (drillDownReq, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_DRILLDOWN_SEARCH + "&tableName=" + drillDownReq["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(drillDownReq["drillDownInfo"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }

    /**
     * Returns number of the records which match the drill-down query given the table.
     * @param drillDownReq The object which contains the drillDown information.
     *  e.g. drillDownReq = {
     *          tableName : "TEST",
     *          drillDownInfo : {
     *              categories : [
     *               {
     *                  fieldName : "facetField1",
     *                  path : ["A", "B", "C"]
     *              },
     *              {
     *                  fieldName : "facetField2",
     *                  path : [ "X", "Y", "Z"]
     *              }]
     *              query : "field1 : value1",
     *              recordStart : 0,
     *              recordCount : 50,
     *              scoreFunction : "scoreParamField * 2"
     *          }
     * @param callback The callback function which has one argument which contains the count.
     */
    this.drillDownSearchCount = function (drillDownReq, callback) {
        $.ajax({
                   url: this.url + "?type=" + TYPE_DRILLDOWN_SEARCH_COUNT + "&tableName=" + schemaInfo["tableName"],
                   dataType: DATA_TYPE_JSON,
                   contentType: CONTENT_TYPE_JSON,
                   data: JSON.stringify(schemaInfo["drillDownInfo"]),
                   type: HTTP_POST,
                   beforeSend: function (request) {
                       if (username != null && password == null) {
                           request.setRequestHeader(AUTHORIZATION_HEADER, this.authHeader);
                       }
                   },
                   success: function (data) {
                       callback(data);
                   }
               });
    }
}

/**
 * Construct an AnalyticsClient object given the username, password and serverUrl.
 * @param username the username
 * @param password the password
 * @param svrUrl the server url
 * @returns {AnalyticsClient} AnalyticsClient object
 */
AnalyticsClient.prototype.init = function (username, password, svrUrl) {
    this.url = svrUrl;
    this.authHeader = generateBasicAuthHeader(username, password);
    function generateBasicAuthHeader(username, password) {
        return "Authorization:Basic " + btoa(username + ":" + password);
    }
    return this;
}

/**
 * Construct an AnalyticsClient object given the serverUrl.
 * @param svrUrl the server url.
 * @returns {AnalyticsClient} AnalyticsClient object.
 */
AnalyticsClient.prototype.init = function (svrUrl) {
    this.url = svrUrl;
    return this;
}

/**
 * Create an AnalyticsClient object with default server url
 * https://localhost:9443/carbon/jsservice/jsservice_ajaxprocessor.jsp
 * @returns {AnalyticsClient} AnalyticsClient object
 */
AnalyticsClient.prototype.init = function () {
    this.url = "https://localhost:9443/carbon/jsservice/jsservice_ajaxprocessor.jsp";
    return this;
}
