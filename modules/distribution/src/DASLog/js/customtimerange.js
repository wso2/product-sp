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

var graphStartTime;
var graphEndTime;

$(function() {
	var customTimeRangeDialog, startDateTextBox, endDateTextBox;
	/**
	 * Create a custom time range in the search Range.
	 */
	function createCustomTimeRange() {
		
		graphStartTime = (new Date(startDateTextBox.val())).getTime();
		graphEndTime = (new Date(endDateTextBox.val())).getTime();
		createCustomTimeRangedGraph();
		$("#searchrange option[value='custom-time-range-option']").remove();
		$('#searchrange').prepend(
				'<option value="custom-time-range-option">'
						+ startDateTextBox.val() + ' To '
						+ endDateTextBox.val() + '</option>');
		$("#searchrange").val("custom-time-range-option");
		$("#searchrange").selectmenu("refresh");
		customTimeRangeDialog.dialog("close");
	};
  
	/**
	 * create the log graph for the custom time range.
	 */
	createCustomTimeRangedGraph = function() {
		timeRangeInMs = graphEndTime - graphStartTime;
		stopRefreshingGraphAndTable();
		plotData(graphStartTime, graphEndTime);
		populateLogTable(graphStartTime, graphEndTime, createAndReturnFilterList);
	}

	//create the popup dialog for select the time range	
	startDateTextBox = $('#starttime');
	endDateTextBox = $('#endtime');
	customTimeRangeDialog = $("#customtime-form").dialog({
		autoOpen : false,
		modal : true,
		buttons : {
			"Save" : createCustomTimeRange,
			Cancel : function() {
				$("#searchrange").val("1"); // if cancelled 1st item
				// gets selected otherwise, #searchrange crashes!
				$("#searchrange").selectmenu("refresh"); // call this once the select menu updated
				customTimeRangeDialog.dialog("close");
			}
		},
		close : function() {
			form[0].reset();
		},
		minWidth : 400,
		minHeight : 185
	});

	startDateTextBox.datetimepicker({
		timeFormat : 'HH:mm:ss',
		onClose : function(dateText, inst) {
			if (endDateTextBox.val() != '') {
				var testStartDate = startDateTextBox.datetimepicker('getDate');
				var testEndDate = endDateTextBox.datetimepicker('getDate');
				if (testStartDate > testEndDate)
					endDateTextBox.datetimepicker('setDate', testStartDate);
			} else {
				endDateTextBox.val(dateText);
			}
		},
		onSelect : function(selectedDateTime) {
			endDateTextBox
					.datetimepicker('option', 'minDate', selectedDateTime);
		}
	});
	endDateTextBox.datetimepicker({
		timeFormat : 'HH:mm:ss',
		onClose : function(dateText, inst) {
			if (startDateTextBox.val() != '') {
				var testStartDate = startDateTextBox.datetimepicker('getDate');
				var testEndDate = endDateTextBox.datetimepicker('getDate');
				if (testStartDate > testEndDate)
					startDateTextBox.datetimepicker('setDate', testEndDate);
			} else {
				startDateTextBox.val(dateText);
			}
		},
		onSelect : function(selectedDateTime) {
			startDateTextBox.datetimepicker('option', 'maxDate',
					selectedDateTime);
		}
	});

	form = customTimeRangeDialog.find("form").on("submit", function(event) {
		event.preventDefault();
		createCustomTimeRange();
	});				
});