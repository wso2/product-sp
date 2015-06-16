  var datasource, datasourceType;
  var previewData = [];
  var columns = [];
  var done = false;

  ///////////////////////////////////////////// event handlers //////////////////////////////////////////
  $(document).ready(function() {
      // $("#dsList").select2({
      //     placeholder: "Select a datasource",
      //     templateResult: formatDS
      // });
  });

  function formatDS(item) {
      if (!item.id) {
          return item.text;
      }
      var type = $(item.element).data("type");
      var $item;
      if (type === "realtime") {
          $item = $('<div><i class="fa fa-bolt"> </i> ' + item.text + '</div>');
      } else {
          $item = $('<div><i class="fa fa-clock-o"> </i> ' + item.text + '</div>');
      }
      // var $item = $(
      //     '<span><img src="vendor/images/flags/' + item.element.value.toLowerCase() + '.png" class="img-flag" /> ' + item.text + '</span>'
      //   );
      return $item;
  };

  $('#rootwizard').bootstrapWizard({
      onTabShow: function(tab, navigation, index) {
          console.log("** Index : " + index);
          done = false;
          if (index == 0) {
              getDatasources();
              $("#btnPreview").hide();
              $('#rootwizard').find('.pager .next').addClass("disabled");
              $('#rootwizard').find('.pager .finish').hide();
          } else if (index == 1) {
              $('#rootwizard').find('.pager .finish').show();
              $("#previewChart").hide();
              done = true;
              if (datasourceType === "batch") {
                  fetchData();
              }
              renderChartConfig();
          }
      }
  });

  $("#dsList").change(function() {
      datasource = $("#dsList").val();
      if (datasource != "-1") {
          $('#rootwizard').find('.pager .next').removeClass("disabled");
          datasourceType = $("#dsList option:selected").attr("data-type");
          getColumns(datasource, datasourceType);
          if (datasourceType == "batch") {
              $("#btnPreview").show();
          } else {
              $("#btnPreview").hide();
          }
      } else {
          $('#rootwizard').find('.pager .next').addClass("disabled");
      }
  });

  $("#btnPreview").click(function() {
      if ($("dsList").val() != -1) {
          fetchData(renderPreviewPane);
      }
  });

  $("#previewChart").click(function() {
      if (datasourceType === "realtime") {
          var streamId = $("#dsList").val();
          var url = "/portal/apis/cep?action=publisherIsExist&streamId=" + streamId;
          $.getJSON(url, function(data) {
              console.log(data);
              if (!data) {
                  alert("You have not deployed a Publisher adapter UI Corresponding to selected StreamID:" + streamId +
                      " Please deploy an adapter to Preview Data.")
              } else {
                  //TODO DOn't do this! read this from a config file
                  subscribe(streamId.split(":")[0], streamId.split(":")[1], '10', 'carbon.super',
                      onRealTimeEventSuccessRecieval, onRealTimeEventErrorRecieval, 'localhost', '9443', 'WEBSOCKET', "SECURED");
              }
          });
      } else {
          var dataTable = makeDataTable();
          console.log(dataTable);
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
          $("#chartDiv").empty(); //clean up the chart canvas
          //this is a temp hack to draw bar charts with x axis set to numerical values
          if (chartType === "line" && dataTable.metadata.types[xAxis] === "N") {
              dataTable.metadata.types[xAxis] = "C";
          }
          if (chartType === "tabular") {
              config.chartType = "table";
              var style = $("#tableStyle").val();
              if (style === "color") {
                  config.colorBasedStyle = true;
              } else if (style === "font") {
                  config.fontBasedStyle = true;
              }
              console.log(config);
              igviz.draw("#chartDiv", config, dataTable);
          } else {
              if (chartType === "line") {
                  var axis = [];
                  $('#yAxises :selected').each(function(i, selected) {
                      axis[i] = getColumnIndex($(selected).text());
                  });
                  // config.yAxis = axis;
                  config.yAxis = [1];
              }
              console.log(config);
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
              chart.plot(dataTable.data);
          }
      }

  });

  function setAsCategorical(dataTable, xAxis) {
      if (chartType === "bar" && dataTable.metadata.types[xAxis] === "N") {
          dataTable.metadata.types[xAxis] = "C";
      }
      return dataTable;
  };

  function onRealTimeEventSuccessRecieval(streamId, data) {
      drawRealtimeChart(data);
  };

  function onRealTimeEventErrorRecieval(dataError) {
      console.log(dataError);
  };

  $("#chartType").change(function() {
      $(".attr").hide();
      var className = jQuery(this).children(":selected").val();
      var chartType = this.value;
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
          if (chartType === "tabular") {
              config.chartType = "table";
              var style = $("#tableStyle").val();
              if (style === "color") {
                  config.colorBasedStyle = true;
              } else if (style === "font") {
                  config.fontBasedStyle = true;
              }
          } else if (chartType === "map") {
              config.chartType = "map";
              config.title = "Map By Country";
              config.padding = 65;
              config.pointColor = 1;
              config.pointSize = 1;
              config.mapLocation = 0;
              config.mode = "regions";

              if ($("#regionCode").val().trim() === "") {
                  config.region = "world";
              } else {
                  config.region = $("#regionCode").val();
              }
          }

          var request = {
              id: $("#title").val().replace(/ /g, "_"),
              title: $("#title").val(),
              datasource: $("#dsList").val(),
              type: $("#dsList option:selected").attr("data-type"),
              filter: $("#txtFilter").val(),
              columns: columns,
              maxUpdateValue: 10,
              chartConfig: config

          };
          $.ajax({
              url: "/portal/gadgets",
              method: "POST",
              data: JSON.stringify(request),
              contentType: "application/json",
              success: function(d) {
                  console.log("***** Gadget [ " + $("#title").val() + " ] has been generated. " + d);
                  window.location.href = "/portal/dashboards";
              }
          });
      } else {
          console.log("Not ready");
      }
  });

  ////////////////////////////////////////////////////// end of event handlers ///////////////////////////////////////////////////////////

  function getDatasources() {
      $.ajax({
          url: "/portal/apis/cep?action=getDatasources",
          method: "GET",
          contentType: "application/json",
          success: function(data) {
              if (data.length == 0) {
                  var source = $("#wizard-zerods-hbs").html();
                  var template = Handlebars.compile(source);
                  $("#rootwizard").empty();
                  $("#rootwizard").append(template());
                  return;
              }
              var datasources = data.map(function(element, index) {
                  var item = {
                      name: element.name,
                      type: element.type
                  };
                  return item;
              });
              $("#dsList").empty();
              $("#dsList").append($('<option/>').val("-1")
                  .html("--Select a Datasource--")
                  .attr("type", "-1")
              );
              datasources.forEach(function(datasource, i) {
                  var item = $('<option></option>')
                      .val(datasource.name)
                      .html(datasource.name + " [" + datasource.type + "]")
                      .attr("data-type", datasource.type);
                  $("#dsList").append(item);
              });
          },
          error: function(error) {
              var source = $("#wizard-error-hbs").html();;
              var template = Handlebars.compile(source);
              $("#rootwizard").empty();
              $("#rootwizard").append(template({
                  error: error
              }));
          }
      });
  };

  function getColumns(datasource, datasourceType) {
      if (datasourceType === "realtime") {
          console.log("Fetching stream definition for stream: " + datasource);
          var url = "/portal/apis/cep?action=getDatasourceMetaData&type=" + datasourceType + "&dataSource=" + datasource;
          $.getJSON(url, function(data) {
              if (data) {
                  columns = data;
              }
          });
      } else {
          console.log("Fetching schema for table: " + datasource);
          var url = "/portal/apis/analytics?type=10&tableName=" + datasource;
          $.getJSON(url, function(data) {
              if (data) {
                  columns = parseColumns(JSON.parse(data.message));
              }
          });
      }
  };

  function fetchData(callback) {
      var timeFrom = new Date("1970-01-01").getTime();
      var timeTo = new Date().getTime();
      var request = {
          type: 8,
          tableName: $("#dsList").val(),
          filter: $("#txtFilter").val(),
          timeFrom: timeFrom,
          timeTo: timeTo,
          start: 0,
          count: 10
      };
      $.ajax({
          url: "/portal/apis/analytics",
          method: "GET",
          data: request,
          contentType: "application/json",
          success: function(data) {
              var records = JSON.parse(data.message);
              console.log(records); 
              previewData = makeRows(records);
              if (callback != null) {
                  callback(previewData);
              }
          }
      });
  };

  function renderPreviewPane(rows) {
      console.log(rows);
      $("#previewPane").empty();
      $('#previewPane').show();
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

  function renderChartConfig() {
      //hide all chart controls
      $(".attr").hide();
      $("#xAxis").empty();
      $("#yAxis").empty();
      $("#yAxises").empty();

      //populate X and Y axis
      populateAxis("x", columns);
      populateAxis("y", columns);
      populateAxis("y2", columns);
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
          } else if (type == "y2") {
              $("#yAxises").append(item);
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

  var dataTable;
  var chart;
  var counter = 0;
  var globalDataArray = [];

  function drawRealtimeChart(data) {

      var chartType = $("#chartType").val();

      if (chartType == "map") {

          var region = "world";

          if ($("#regionCode").val().trim() != "") {
              region = $("#regionCode").val();
          }

          var config = {
              "chartType": "map",
              "title": "Map By Country",
              "padding": 65,
              "pointColor": 1,
              "pointSize": 1,
              "mapLocation": 0,
              "mode": "regions",
              "width": document.getElementById("chartDiv").offsetWidth,
              "height": 240,
              "region": region
          }

          if (counter == 0) {
              dataTable = makeMapDataTable(data);
              igviz.draw("#chartDiv", config, dataTable);
              counter++;
          } else {

              dataTable.addRows(data);
              igviz.draw("#chartDiv", config, dataTable);
          }
      }
      else {
          dataTable = createDataTable(data);
          if (counter == 0) {
              var xAxis = getColumnIndex($("#xAxis").val());
              var yAxis = getColumnIndex($("#yAxis").val());
              console.log("X " + xAxis + " Y " + yAxis);

              var width = document.getElementById("chartDiv").offsetWidth;
              var height = 240; //canvas height
              var config = {
                  "yAxis": yAxis,
                  "xAxis": xAxis,
                  "width": width,
                  "height": height,
                  "chartType": chartType
              }
              if (chartType === "bar" && dataTable.metadata.types[xAxis] === "N") {
                  dataTable.metadata.types[xAxis] = "C";
              }
              chart = igviz.setUp("#chartDiv", config, dataTable);
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

              globalDataArray.push(dataTable.data[0]);
              chart.plot(globalDataArray);
              counter++;
          } else if (counter == 5) {
              globalDataArray.shift();
              globalDataArray.push(dataTable.data[0]);
              chart.update(dataTable.data[0]);
          } else {
              globalDataArray.push(dataTable.data[0]);
              chart.plot(globalDataArray);
              counter++;
          }
      }
  };

  function parseColumns(data) {
      if (data.columns) {
          var keys = Object.getOwnPropertyNames(data.columns);
          var columns = keys.map(function(key, i) {
              return column = {
                  name: key,
                  type: data.columns[key].type
              };
          });
          return columns;
      }
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

  function createDataTable(data) {
      var realTimeData = new igviz.DataTable();
      if (columns.length > 0) {
          columns.forEach(function(column, i) {
              var type = "N";
              if (column.type == "STRING" || column.type == "string") {
                  type = "C";
              }
              realTimeData.addColumn(column.name, type);
          });
      }
      for (var i = 0; i < data.length; i++) {
          realTimeData.addRow(data[i]);
      }
      return realTimeData;
  };

  function makeMapDataTable(data) {
      var dataTable = new igviz.DataTable();
      if (columns.length > 0) {
          columns.forEach(function (column, i) {
              var type = "N";
              if (column.type == "STRING" || column.type == "string") {
                  type = "C";
              }
              dataTable.addColumn(column.name, type);
          });
      }
      data.forEach(function (row, index) {
          for (var i = 0; i < row.length; i++) {
              if (dataTable.metadata.types[i] == "N") {
                  data[index][i] = parseInt(data[index][i]);
              }
          }
      });
      dataTable.addRows(data);
      return dataTable;
  };