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

$(function() {

	var createfilterdialog, form, filterCount = 0, isFilterEdit = false, currentEditFilter = null;
	
	/**
	 * create the titlebar for the filter dialog.
	 * @filter id of the filter
	 * @tempFilterCount current count of the filters
	 */
	function createHeader(filter, tempFilterCount) {
		var editbtn = filter.find(".filterheader .editfilter")[0]
		var removebtn = filter.find(".filterheader .removefilter")[0];

		$(editbtn).button({
			text : false,
			icons : {
				primary : "ui-icon-newwin"
			}
		}).click(function(e) {
			isFilterEdit = true;
			currentEditFilter = filter;
			createfilterdialog.dialog("open");
		});
		
		//Enable button for enabling the filter
		enablebtn = $("<input />").addClass("enablefilter").appendTo(
				filter.find(".filterheader .filterconfig")[0]);

		enablebtn.attr({
			"id" : "enablefilter" + tempFilterCount
		}).prop({
			"type" : "checkbox"
		});
		enablebtn.after($("<label />").addClass("enablefilter").attr({
			"for" : "enablefilter" + tempFilterCount,
			"title" : "enable filter"
		})).button({
			text : false,
			icons : {
				primary : "ui-icon-check"
			}
		}).click(function(e) {
			$(this).button("option", {
				icons : {
					primary : (!$(this)[0].checked) ? "ui-icon-check" : ""
				}
			});
		});

		//Remove button for removing the current filter
		$(removebtn).button({
			text : false,
			icons : {
				primary : "ui-icon-close"
			}
		}).click(function(e) {
			filter.remove();
		});

	}
	;

	//Create the content of the filter dialog.
	function createContent(filter) {

		filter.find(".createdquerystring").val(
				createfilterdialog.find(".newfilterquerystring").val());

		filter.find(".createdquery").val(
				createfilterdialog.find(".newfiltersearchstring").val());

		currentEditFilter = null;
		isFilterEdit = false;

		createfilterdialog.dialog("close");
	}

	//load the createdfilter to #filtercontainer.
	function createFilter() {
		filterCount = $("#filtercontainer .createdfilter").length;
		var newFilter = $("<div />").addClass("createdfilter").appendTo(
				$("#filtercontainer"));

		newFilter.attr({

			id : "filter" + filterCount

		}).load("./createdfilter.jag", function() {

			createHeader(newFilter, filterCount);
			createContent(newFilter);
		});

	};

	//Popup dialog for creating the filters.
	createfilterdialog = $("#newfilter-form").dialog({
		autoOpen : false,
		modal : true,
		buttons : {
			"Save Filter" : function() {

				if (isFilterEdit == false) {

					createFilter();
				} else if (isFilterEdit == true && currentEditFilter != null) {
					createContent(currentEditFilter);
				}

			},
			Cancel : function() {
				createfilterdialog.dialog("close");
			}
		},
		close : function() {
			form[0].reset();
		},
		minWidth : 400,
		minHeight : 185
	});

	$("#newfilterbtn").click(function() {
		$("#newfilter-form").dialog("open");
	});

	form = createfilterdialog.find("form").on("submit", function(event) {
		event.preventDefault();
		createFilter();
	});
		

});