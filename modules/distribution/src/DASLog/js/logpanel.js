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

var logCount = 0;
var logTable = null;
var logSummary = {};
var microPanel = null;
$(function() {
	// override the "contains" css selector to match words in case-insensative
	// way
	jQuery.expr[':'].Contains = function(a, i, m) {
		return (a.textContent || a.innerText || "").toUpperCase().indexOf(
				m[3].toUpperCase()) >= 0;
	};
	
	/**
	 * Create the log table.
	 * @param startTime start time of the log table
	 * @param endTime end time of the log table
	 * @param createFilterList function for creating the filter list
	 */
	populateLogTable = function(startTime, endTime, createFilterList){
		
		//Get the full details from Elastic search to display on the log table
		getRangedFullDetails($("#searchbox").val(), startTime, endTime, function(logs){
			var titleArray =[];
			logSummary = {};
			//create a log array out of the elasticsearch data to pass into the log table
			var logArray = $.map(logs, function(logHit){
				var rowArray = $.map(logHit.values, function(value, field ){	
					createLogSummary(value, field);
					return value;
				});
				return [rowArray];
			});
			
			if ( logTable != null) {
				logTable.clear();
				logTable.rows.add(logArray);
				logTable.draw();
			} else {
				if(logs.length > 0 && createFilterList != null){
					titleArray = createFilterList(logs, logs.length);
					logTable = $('#report').DataTable({ //Create the Datatable
						"bAutoWidth" : false,
						"iDisplayLength" : 100,
						"stripeClasses": [ 'ui-widget-content', 'evenlogtablerows' ],
						"data" : logArray,
						"columns" : titleArray
					});
					
					//add click event to table rows to display and hide detailed view
					$('#report tbody').on( 'click', 'tr', function () {
				        var row = logTable.row( this );
				        if ( row.child.isShown() ) {
				            row.child.hide();
				           // tr.removeClass('shown');
				        }
				        else {
				        	stopRefreshingGraphAndTable();
				            row.child(expandLogRow(titleArray, row.data()) ).show();
				         //   tr.addClass('shown');
				        }
					} );
					
					//add events to checkboxes to show and hide log tables columns
					$("#logcontent input:checkbox:not(:checked)").each(function() {
						var column = logTable.column( $(this).attr('data-column') );
				        column.visible( ! column.visible() );
					});
					
					$("#logcontent input:checkbox").click(function(e) {
						var column = logTable.column( $(this).attr('data-column') );
						column.visible( ! column.visible() );
					});
				}
			}
		});
	}
	
	/**
	 * Attach the micro panel to each of the tags in the filterlist.
	 * 
	 * @param field the field or the tag given in the conf file
	 * @param totalCount the total hitcount
	 */
	bindMicroPanelToFilterList = function(field, totalCount){
		
		//create a 2D array with each tag and hitcount of the tag value as a percentage
		var fieldCounts = $.map(logSummary["" + field], function(count, value){
			var color = Math.random().toString(16).slice(2, 8);
			var complementaryColor = ('0xffffff' ^ ''+ color).toString(16).slice(2,8);
			var fieldCount = count < totalCount ? (count/totalCount).toPrecision(2) * 100 : 100;
			var percentageBar = '<div style="background:none repeat scroll 0% 0% #' + color + ';color:# '+ complementaryColor + 
								';width:'+ fieldCount + '%">' + fieldCount.toFixed(2) + '%</div>';
			return [[value, percentageBar]];
		});
			    
		$("#micropanel").dialog({
			title : "Summary of Field '" + field + "'",
			resizable : false,
			close : function(){
				if(microPanel != null) microPanel.destroy();
				$('#micropaneltable').empty();
			}
		});

		microPanel = $('#micropaneltable').DataTable({
			"bAutoWidth" : false,
			"iDisplayLength" : 10,
			"stripeClasses" : [ 'ui-widget-content', 'evenlogtablerows' ],
			"data" : fieldCounts,
			"columns" : [ {
				"title" : "Value"
			}, {
				"title" : "Count"
			} ],
			"order" : [ [ "1", "desc" ] ]
		});
	
	}
	
	/**
	 * Add the mouseclick event to tags in the filter list.
	 * @param cssClass the css class when hover
	 * @param totalCount hitcount which is needed for micropanel
	 */
	
	addMouseClickListenerForFields = function(cssClass, totalCount){
		
		if (cssClass != null){
			$("" + cssClass).click(function(){
				bindMicroPanelToFilterList($(this).text(), totalCount);
			});
		}
	}

	/**
	 * Create the UI for filterlist containning the tags and return the tags.
	 * @param logs
	 * @param totalCount
	 * @returns {___anonymous_titleArray}
	 */
	createAndReturnFilterList = function(logs, totalCount){
		var i  = 0;
		$('.logfilterset').html('');
		titleArray = $.map(logs[0].values,function(value, field){
				$('.logfilterset').append('<div class="filterdiv"><input id="checkbox'+(i+1)+'"' 
						+'" type="checkbox" checked= "true" data-column="'+ i + '"> <label title="" class="logfilterlabel">'
						+ field + 
						'</label>'
		+'</div>');
			i++;
			return {"title" : "" + field };
		});
		addMouseClickListenerForFields(".logfilterlabel", totalCount);
		return titleArray;
	}
	
	//add the change event to log filter's text box
	$(".logfiltersearchstring").change(
			function() {
				var searchString = $(".logfiltersearchstring").val();

				if (searchString) {
					$(".logfilterset").find(
							".logfilterlabel:not(:Contains(" + searchString
									+ "))").parent().slideUp();
					$(".logfilterset").find(
							".logfilterlabel:Contains(" + searchString + ")")
							.parent().slideDown();
				} else {
					$(".filterdiv").slideDown();
				}

			}).keyup(function() {
		$(this).change();
	});

});