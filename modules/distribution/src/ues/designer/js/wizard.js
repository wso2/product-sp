  var datasource, datasourceType;
  var previewData = [];
  var columns = [];
  var done = false;

  $(document).ready(function() {

  });

  $('#rootwizard').bootstrapWizard({
      onTabShow: function(tab, navigation, index) {
          console.log("** Index : " + index);
          done = false;
          if (index == 0) {
              getDatasources();
              $('#rootwizard').find('.pager .next').addClass("disabled");
              $('#rootwizard').find('.pager .finish').hide();

          } else if (index == 1) {
              // $('#rootwizard').find('.pager .next').show();
              $('#rootwizard').find('.pager .finish').hide();
              getColumns($("#dsList").val());
          } else if (index == 2) {
              done = true;
              // $('#rootwizard').find('.pager .next').hide();
              $('#rootwizard').find('.pager .finish').show();
              $("#previewChart").hide();

              //load preview data if it hasn't been loaded in step2
              if (previewData.length == 0) {
                  fetchData();
              }
              renderChartConfig();
          }
      },
      onNext: function(tab, navigation, index) {

      }
  });

  function getDatasources() {
      $.ajax({
          url: "/designer/apis/analytics?action=getDatasources",
          method: "GET",
          contentType: "application/json",
          success: function(data) {
              if (!data) {
                  //you have to be logged in at admin console
                  // var source = $("#not-loggedin-hbs").html();
                  var source = "<p>Log in !</p>";
                  var template = Handlebars.compile(source);
                  $("#rootwizard").append(template);
              } else {
                  var datasources = data.map(function(element, index) {
                      var item = {
                          name: element,
                          type: "batch"
                      };
                      return item;
                  });
                  // console.log(datasources); 
                  $("#dsList").empty();
                  $("#dsList").append($('<option/>').val("-1")
                      .html("--Select a Datasource--")
                      .attr("type", "-1")
                  );
                  datasources.forEach(function(datasource, i) {
                      var item = $('<option></option>')
                          .val(datasource.name)
                          .html(datasource.name)
                          .attr("data-type", datasource.type);
                      $("#dsList").append(item);
                  });
              }
          },
          error: function(error) {
              var source = "<p>Log in !</p>";
              var template = Handlebars.compile(source);
              $("#rootwizard").append(template);
          }
      });

  };

  function getColumns(table) {
      console.log("Fetching table schema for table: " + table);
      // var url = "/carbon/jsservice/jsservice_ajaxprocessor.jsp?type=11&tableName=" + table;
      var url = "/designer/apis/analytics?action=getSchema&tableName=" + table;
      $.getJSON(url, function(data) {
          console.log(data);
          if (data) {
              // columns = parseColumns(data);
              columns = data;
          }

      });
  };

  function fetchData(callback) {
      var timeFrom = new Date("1970-01-01").getTime();
      var timeTo = new Date().getTime();
      var request = {
          action: "getData",
          tableName: $("#dsList").val(),
          filter: $("#txtFilter").val(),
          timeFrom: timeFrom,
          timeTo: timeTo,
          start: 0,
          count: 10
      };
      $.ajax({
          // url: "/carbon/jsservice/jsservice_ajaxprocessor.jsp",
          url: "/designer/apis/analytics",
          method: "GET",
          data: request,
          contentType: "application/json",
          success: function(data) {
              previewData = makeRows(data);
              if (callback != null) {
                  callback(previewData);
              }
          }
      });
  };

  // function parseColumns(data) {
  //     if (data.columns) {
  //         var keys = Object.getOwnPropertyNames(data.columns);
  //         var columns = keys.map(function(key, i) {
  //             return column = {
  //                 name: key,
  //                 type: data.columns[key].type
  //             };
  //         });
  //         return columns;
  //     }
  // };

  function renderPreviewPane(rows) {
      $("#previewPane").empty();
      var table = jQuery('<table/>', {
          id: 'tblPreview',
          class: 'table table-bordered'
      }).appendTo('#previewPane');

      //add column headers to the table
      var thead = jQuery("<thead/>");
      thead.appendTo(table);
      var th = jQuery("<tr/>");
      columns.forEach(function(column, idx) {
          var td = jQuery('<th/>');
          td.append(column.name);
          td.appendTo(th);
      });
      th.appendTo(thead);

      rows.forEach(function(row, i) {
          var tr = jQuery('<tr/>');
          columns.forEach(function(column, idx) {
              var td = jQuery('<td/>');
              td.append(row[idx]);
              td.appendTo(tr);
          });

          tr.appendTo(table);

      });
  };

  function makeRows(data) {
      var rows = [];
      for (var i = 0; i < data.length; i++) {
          var record = data[i];
          var keys = Object.getOwnPropertyNames(record.values);
          var row = columns.map(function(column, i) {
              return record.values[column.name];
          });
          rows.push(row);
      };
      return rows;
  };

  function makeDataTable() {
      var dataTable = new igviz.DataTable();
      if (columns.length > 0) {
          columns.forEach(function(column, i) {
              var type = "N";
              if (column.type == "STRING" || column.type == "string") {
                  type = "C";
              }
              dataTable.addColumn(column.name, type);
          });
      }
      previewData.forEach(function(row, index) {
          for (var i = 0; i < row.length; i++) {
              if (dataTable.metadata.types[i] == "N") {
                  previewData[index][i] = parseInt(previewData[index][i]);
              }
          }
      });
      dataTable.addRows(previewData);
      return dataTable;
  };

  function renderChartConfig() {
      console.log("Rendering chart config");
      //hide all chart controls
      $(".attr").hide();
      $("#xAxis").empty();
      $("#yAxis").empty();
      // $("#yAxises").empty();
      //populate X and Y axis
      populateAxis("x", columns);
      populateAxis("y", columns);
      // $("#yAxises").append(item);

  };

  //TODO Refactor this shit out!
  function populateAxis(type, columns) {
      // $("#dsList").append($('<option/>').val("-1")
      //     .html("--Select a Datasource--")
      //     .attr("type", "-1")
      // );
      columns.forEach(function(column, i) {
          var item = $('<option></option>')
              .val(column.name)
              .html(column.name)
              .attr("data-type", column.type);
          if (type == "x") {
              $("#xAxis").append(item);
          } else if (type == "y") {
              $("#yAxis").append(item);
          }
      });
  };

  function getColumnIndex(columnName) {
      for (var i = 0; i < columns.length; i++) {
          if (columns[i].name == columnName) {
              return i;
          }
      }
  };



  ///////////////////////////////////////////// event handlers //////////////////////////////////////////

  $("#dsList").change(function() {
      if ($("#dsList").val() != "-1") {
          $('#rootwizard').find('.pager .next').removeClass("disabled");
      } else {
          $('#rootwizard').find('.pager .next').addClass("disabled");
      }
  });

  $("#btnPreview").click(function() {
      fetchData(renderPreviewPane);
  });

  $("#previewChart").click(function() {
      var dataTable = makeDataTable();
      var xAxis = getColumnIndex($("#xAxis").val());
      var yAxis = getColumnIndex($("#yAxis").val());
      console.log("X " + xAxis + " Y " + yAxis);

      var chartType = $("#chartType").val();
      var width = document.getElementById("chartDiv").offsetWidth;
      var height = 240; //canvas height
      var config = {
          "yAxis": yAxis,
          "xAxis": xAxis,
          "width": width,
          "height": height,
          "chartType": chartType
      }
      if (chartType === "table") {
          var style = $("#tableStyle").val();
          if (style === "color") {
              config.colorBasedStyle = true;
          } else if (style === "font") {
              config.fontBasedStyle = true;
          }
          igviz.draw("#chartDiv", config, dataTable);
      } else {
          var chart = igviz.setUp("#chartDiv", config, dataTable);
          chart.setXAxis({
                  "labelAngle": -35,
                  "labelAlign": "right",
                  "labelDy": 0,
                  "labelDx": 0,
                  "titleDy": 25
              })
              .setYAxis({
                  "titleDy": -30
              })
              .setDimension({
                  height: 270
              })
          chart.plot(dataTable.data);
      }

  });

  $("#chartType").change(function() {
      $(".attr").hide();
      var className = jQuery(this).children(":selected").val();
      $("." + className).show();
      $("#previewChart").show();

      $('#rootwizard').find('.pager .finish').removeClass('disabled');
  });

  $(".pager .finish").click(function() {
      //do some validations
      if ($("#title").val() == "") {
          alert("Gadget title must be provided!");
          return;
      }
      if (done) {
          console.log("*** Posting data for gadget [" + $("#title").val() + "]");
          //building the chart config depending on the chart type
          var chartType = $("#chartType").val();
          var config = {
              chartType: $("#chartType").val(),
              xAxis: getColumnIndex($("#xAxis").val()),
              yAxis: getColumnIndex($("#yAxis").val())
          };
          if (chartType === "table") {
              var style = $("#tableStyle").val();
              if (style === "color") {
                  config.colorBasedStyle = true;
              } else if (style === "font") {
                  config.fontBasedStyle = true;
              }
          } 
          var request = {
              id: $("#title").val().replace(/ /g, "_"),
              title: $("#title").val(),
              datasource: $("#dsList").val(),
              type: $("#dsList option:selected").attr("data-type"),
              filter: $("#txtFilter").val(),
              columns: columns,
              chartConfig: config

          };
          $.ajax({
              url: "/designer/gadgets",
              method: "POST",
              data: JSON.stringify(request),
              contentType: "application/json",
              success: function(d) {
                  console.log("***** Gadget [ " + $("#title").val() + " ] has been generated. " + d);
                  window.location.href = "/designer/dashboards";
              }
          });
      } else {
          console.log("Not ready");
      }
  });