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

var plot = null;
var customTimeRangeInMs = 0;
var updateInterval = 1000;
var logHits =[];
var graphUpdateFunc;

$(document).ready(function() {
	var data = [];
	var xAxisFormatString = '%H:%M:%S';
	var mouseDown = false;
	var isZoomed = false;
  
	$.ajax({
		url:"http://localhost:9763/analytics/logtable",
		type:"GET",
		success: function(data){
			var currentTime = (new Date()).getTime();
			var startTime = currentTime - timeRangeInMs;
			if(data == false) {
				disableUserInput();
				noty({
					text : "Table \"logtable\" not found",
					theme : "relax"
						});
			} else {
				plotData(startTime, currentTime);
				populateLogTable(startTime, currentTime, createAndReturnFilterList);
			}
		}
	});
	
	/**
	 * Disables the user input,searchrange, refreshrates, and searchbuttons
	 */
	disableUserInput = function(){
		$("#refreshRate input").button("disable");
		$("#searchbtn").button("disable");
		$("#searchrange").selectmenu("disable");
	}
	/**
	 * Create the log graph, which is used to draw the log graph periodically.
	 * @param startTime
	 * @param endTime
	 */
	plotData = function(startTime,endTime) {
		
		getRangedCount(
				$("#searchbox").val(), startTime,
				endTime, function(countData){
			data = countData;
			if(plot == null) {
				plot = $.plot($("#logchart"), [{color : "#0060FF", data :data}], options); //create the initial graph
			} else {
				plot.setData([{data :data}]);
				plot.getOptions().xaxes[0].min = startTime;
				plot.getOptions().xaxes[0].max = endTime;
				plot.setupGrid();
				plot.draw();
			}
		}); 
	};
	
	/**
	 * Expand the clicked row on the log table.
	 * @param titleArray titles of the header row of the log table
	 * @param dataArray data to be displayed in the table
	 * @returns A HTML table
	 */
	expandLogRow = function(titleArray, dataArray) {

		detailedLogHTML = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">';
		for (var i = 0; i < titleArray.length; i++) {
			detailedLogHTML += '<tr>' + '<td class="detailedLoglineTitle">'
				+ titleArray[i].title + '</td>' + '<td style="white-space: pre-wrap;">' //cannot apply a class since the class does 
																						// not override other classes, so using 'style'
				+ dataArray[i] + '</td>' + '</tr>';
		}
		detailedLogHTML += '</table>';
		return detailedLogHTML;
	}

	/**
	 * Create a logSummary variable to pass into microPanel.
	 * @param value value of a tag
	 * @param field name of the tag
	 */
	createLogSummary = function(value, field){
		
		if (field in logSummary){
			
			if(value in logSummary[field]){
				logSummary[field][value] += 1;
			} else {
				
				logSummary[field][value] = 1
			}
		} else {
			
			logSummary[field]  ={};
			logSummary[field][value] = 1;
		}
	}
			
	//options to pass into the graph
	var options = {
						series : {
							bars : {
								show : true
							},
							colors :["#0060FF"]
						},
						bars : {
							align : "center",
							barWidth : 0.5
						},
						
						colors : ["#0060FF"],
						
						axisLabels: {
				            show: true
				        },
				        
						xaxis : {
							mode : "time",
							timezone : "browser",
							min : (new Date()).getTime() - timeRangeInMs,
							max : (new Date()).getTime(),
							
							tickFormatter : function(v, axis) {
								
					        	var date = new Date(v);

								var hours = date.getHours() < 10 ? "0"
											+ date.getHours() : date.getHours();
											
								var minutes = date.getMinutes() < 10 ? "0"
											+ date.getMinutes() : date
											.getMinutes();
											
								var seconds = date.getSeconds() < 10 ? "0"
											+ date.getSeconds() : date
											.getSeconds();
											
								var dateNumber = date.getDate() <10 ? "0" +date.getDate() : date.getDate();
								
								var month = date.getMonth() <10 ? "0" +date.getMonth() : 
									date.getMonth();
								
								if(timeRangeInMs <= 1 * 60 * 60 * 1000){
									
									return hours + ':' + minutes + ':'
											+ seconds;
								} else {

									return hours + ':' + minutes + '<br/>'
											+ dateNumber + '-' + month;
								}	
							},
							axisLabel : "Time",
							axisLabelUseCanvas : true,
							axisLabelFontSizePixels : 12,
							axisLabelFontFamily : 'Verdana, Arial',
							axisLabelPadding : 10
		    },
		    yaxis: {
		    	min : 0,
		        axisLabel: "Log Hits",
		        axisLabelUseCanvas: true,
		        axisLabelFontSizePixels: 12,
		        axisLabelFontFamily: 'Verdana, Arial',
		        axisLabelPadding: 6
		    },
		    legend: {        
		        labelBoxBorderColor: "#fff"
		    }
		};
	/**
	 * get the log hit Count from ElasticSearch.
	 * @param client the elasticSearch client
	 * @param countIndex index from which the logs should be pulled
	 * @param searchQuery the search query
	 * @param startTime start time of the time range
	 * @param endTime end time of the time range
	 * @param callback callback on what to do when the data is available
	 */
	getRangedCount = function(searchQuery,
			startTime, endTime, callback) {
		var timeRange = endTime - startTime;
		var samplingRange = 5000;
		var noOfSamples = timeRange / samplingRange ;
		var xAxisTime = startTime;
		var hitCount = [];
		for(var i = 0; i < noOfSamples; i++) {
			var samplingStartTime = startTime + (i * samplingRange) -1;
			var samplingEndTime = samplingStartTime + samplingRange;
			$.ajax({
				url:"http://localhost:9763/analytics/search_count",
				type:"POST",
				data:JSON.stringify({
					    		tableName: "logtable",
					    		language: "lucene",
					    		query:  searchQuery ==''? "_timestamp:["+ samplingStartTime + " TO " + 
					    				samplingEndTime + "]" : searchQuery + " AND _timestamp:["+ samplingStartTime + 
					    				" TO " + samplingEndTime + "]",
					    		start: 0, 
					    		count: -1
						
				}),
				contentType:"application/json;",
				dataType:"json",
				success: function(data){
					xAxisTime += samplingRange;
					hitCount.push([xAxisTime, parseInt(data)]);
			  	}
			});
		}
		$(document).ajaxStop(function() {
			callback(hitCount);
		});
	}
	
	/**
	 * Get the detailed information from ElasticSearch.
	 * @param client the ElasticSearchClient
	 * @param searchIndex Index in which the logs should be pulled from
	 * @param searchQuery the search query
	 * @param startTime start time of the time range
	 * @param endTime end time of the time range
	 * @param callback callback to be called when the data is available
	 */
	getRangedFullDetails = function(searchQuery,
			startTime, endTime, callback) {

		$.ajax({
			url:"http://localhost:9763/analytics/search",
			type:"POST",
			data:JSON.stringify({
		    		tableName: "logtable",
		    		language: "lucene",
		    		query:  searchQuery ==''? "_timestamp:["+ startTime + " TO " + endTime + "]" : searchQuery + 
		    				" AND _timestamp:["+ startTime + " TO " + endTime + "]",
		    		start: 0, 
		    		count: 100
				
			}),
			contentType:"application/json;",
			dataType:"json",
			success: function(data){
				callback(data);
			}
		});

	}

	//load the log-graph into #grapharea.
		//update function for the graph and the table according to the refreshRate.
	function update() {
		var currentTime = (new Date()).getTime();
		var startTime = currentTime - timeRangeInMs;
			
		plotData(startTime, currentTime);
		populateLogTable(startTime, currentTime, createAndReturnFilterList);
	    graphUpdateFunc = setTimeout(update, milliSecRefreshRate);
	}

	//Add events to refreshRate button set.
	$("#refreshRate input[type=radio]").change(
			function() {
					
				var refreshRate = this.value;
				clearTimeout(graphUpdateFunc);
					
				if (refreshRate != "off") {

					var refreshRateNumber = Number(refreshRate.substring(0,
								refreshRate.length-1));
					var refreshType = refreshRate.slice(-1);
						
					if($("#searchrange option:selected").val().toLowerCase()=="custom-time-range-option"){
						$("#searchrange").val("5m");
						$("#searchrange").selectmenu("refresh");
						timeRangeInMs = 5 * 60 * 1000;
					}
					if (refreshType == "s") {
						refreshRateNumber *= 1000;
					} else if (refreshType == "m") {
						refreshRateNumber *= 60 * 1000;
					} else if (refreshType == "h") {
						refreshRateNumber *= 60 * 60 * 1000;
					} else if (refreshType == "d") {
						refreshRateNumber *= 60 * 60 * 1000 * 24;	
					}
					milliSecRefreshRate = refreshRateNumber;
					update();
						
				} else {
						
				}
					
			});

	stopRefreshingGraphAndTable = function(){
		clearTimeout(graphUpdateFunc); //stop refrshing the previous graph
		$('#refreshRate input[type=radio]').filter("[value='off']").prop("checked", true).button("refresh");
	}
});
