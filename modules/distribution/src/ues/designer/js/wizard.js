  var datasource, datasourceType;
  var previewData = [];
  var columns = [];
  var done = false;

  $( document ).ready(function() {
      console.log( "ready!" );
      //load up the datasource when doucment is ready
      
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
          } else if(index == 2) {
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
      $.getJSON("/carbon/jsservice/jsservice_ajaxprocessor.jsp?type=10", function(data) {
          // console.log(data);
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
      });
  };

  function getColumns(table) {
      console.log("Fetching table schema for table: " + table);
      var url = "/carbon/jsservice/jsservice_ajaxprocessor.jsp?type=11&tableName=" + table;
      $.getJSON(url, function(data) {
          if (data.columns) {
              columns = parseColumns(data);
          }

      });
  };

  function fetchData(callback) {
      var timeFrom = new Date("1970-01-01").getTime();
      var timeTo = new Date().getTime();
      var request = {
          type: 9,
          tableName: $("#dsList").val(),
          timeFrom: timeFrom,
          timeTo: timeTo,
          start: 0,
          count: 10
      };
      $.ajax({
          url: "/carbon/jsservice/jsservice_ajaxprocessor.jsp",
          method: "GET",
          data: request,
          contentType: "application/json",
          success: function(data) {
              previewData = makeRows(JSON.parse(data));
              if (callback != null) {
                  callback(previewData);
              }
          }
      });
  };

  function parseColumns(data) {
      if (data.columns) {
          var keys = Object.getOwnPropertyNames(data.columns);
          var columns = keys.map(function(key, i) {
              return column = {
                  name: key,
                  type: data.columns[key]
              };
          });
          return columns;
      }
  };

  function renderPreviewPane(rows) {
      //now draw the rows in the table
      var source = $("#preview-data-hbs").html();
      var template = Handlebars.compile(source);
      $("#previewPane").append(template({
          columns: columns,
          rows: rows
      }));
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
      if (previewData.length > 0) {
          dataTable.addRows(previewData);
      }
      return dataTable;
  };

  function renderChartConfig() {
      //hide all chart controls
      $(".attr").hide();
      $("#xAxis").empty();
      $("#yAxis").empty();
      //populate X and Y axis
      populateAxis("x", columns);
      populateAxis("y", columns);



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

  $("#dsList").change(function () {
    if($("#dsList").val() != "-1") {
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

      var width = document.getElementById("chartDiv").offsetWidth;
      var height = 240; //canvas height
      var config = {
          "yAxis": yAxis,
          "xAxis": xAxis,
          "width": width,
          "height": height,
          "chartType": $("#chartType").val()
      }
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
  });

  $("#generate").click(function() {
      //create the gagdget config object to be sent to the backend
      var request = {
          id: $("#title").val().replace(/ /g, "_"),
          title: $("#title").val(),
          datasource: $("#dsList").val(),
          type: $("#dsList option:selected").attr("data-type"),
          filter: "",
          columns: columns,
          chartConfig: {
              chartType: $("#chartType").val(),
              xAxis: getColumnIndex($("#xAxis").val()),
              yAxis: getColumnIndex($("#yAxis").val())
          }

      };
      $.ajax({
          url: "/designer/gadgets",
          method: "POST",
          data: JSON.stringify(request),
          contentType: "application/json",
          success: function(d) {
              console.log("***** Gadget [ " + $("#title").val() + " ] has been generated. " + d);
          }
      });
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
      if($("#title").val() == "") {
        alert("Gadget title must be provided!");
        return;
      }
      if (done) {
          console.log("*** Posting data for gadget [" + $("#title").val() + "]");
          var request = {
              id: $("#title").val().replace(/ /g, "_"),
              title: $("#title").val(),
              datasource: $("#dsList").val(),
              type: $("#dsList option:selected").attr("data-type"),
              filter: "",
              columns: columns,
              chartConfig: {
                  chartType: $("#chartType").val(),
                  xAxis: getColumnIndex($("#xAxis").val()),
                  yAxis: getColumnIndex($("#yAxis").val())
              }

          };
          $.ajax({
              url: "/designer/gadgets",
              method: "POST",
              data: JSON.stringify(request),
              contentType: "application/json",
              success: function(d) {
                  console.log("***** Gadget [ " + $("#title").val() + " ] has been generated. " + d);
              }
          });
      } else {
          console.log("not ready");
      }
  });