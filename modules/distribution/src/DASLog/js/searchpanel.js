/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var timeRangeInMs = 60 * 5 * 1000; // default value is for 5 mins

$(function() {
	$("#searchbtn").button({ //search button for searching logs
		text : false,
		icons : {
			primary : "ui-icon-search"
		}
	}).click(function(){
		if ($("#searchrange option:selected").val()
								.toLowerCase() == "custom-time-range-option"){
			createCustomTimeRangedGraph();
		} else {
			var currentTime = (new Date()).getTime();
			plotData(currentTime- timeRangeInMs, currentTime); //create the graph
			populateLogTable(currentTime - timeRangeInMs, currentTime, createAndReturnFilterList);//create the log table
		}
	});
	
	$("#searchrange").selectmenu({ //Time range for searching logs
		width : "250px"
	});

	$("#newfilterbtn").button({ // filter button for creating new filters
		text : false,
		icons : {
			primary : "ui-icon-plus"
		}
	});

	// Adding menu change event to Search range
	$("#searchrange")
			.on(
					"selectmenuchange",
					function(event, ui) {

						if ($("#searchrange option:selected").text()
								.toLowerCase() == "custom") {
							$("#customtime-form").dialog("open"); // open the dialog for custom search ranges
							
						} else if ($("#searchrange option:selected").val()
								.toLowerCase() == "custom-time-range-option") { //if already have a custom range
							createCustomTimeRangedGraph(); //create the custom log graph
							
						} else { //predefined time ranges
							var currentTime = (new Date()).getTime();
							var graphRange = $('#searchrange').val();
							timeRangeInMs = Number(graphRange.substring(
									0, graphRange.length - 1));
							var timeRangeType = graphRange.slice(-1);

							if (timeRangeType == "m") {
								timeRangeInMs *= 60 * 1000;
							} else if (timeRangeType == "h") {
								timeRangeInMs *= 60 * 60 * 1000;
							} else if (timeRangeType == "d") {
								timeRangeInMs *= 60 * 60 * 1000 * 24;
							}

							plotData(currentTime- timeRangeInMs, currentTime); //create the graph
							//create the log table
							populateLogTable(currentTime - timeRangeInMs, currentTime, createAndReturnFilterList);
						}
					});
});