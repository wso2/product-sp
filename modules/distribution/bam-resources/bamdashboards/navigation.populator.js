$(document).ready(function(){
			var dashboardurl = '..';
			$.ajax({
				type: "GET",
				url: "../getDeployedToolboxes.jag",
				dataType: "json",
				success: function(json) {
					var deployedToolboxes = json;
                    var str = "";
                    var iconClass = "icon-gears";

                    for (var i = 0; i < deployedToolboxes.toolboxes.length; i++) {
                        var data_value = deployedToolboxes.toolboxes[i].dashboard.split('>')[1].split('</')[0];
                        if(data_value == "JMX Statistics"){ iconClass = "icon-cogs"}
                        if(data_value == "ESB Mediation Statistics"){ iconClass = "icon-desktop"}
                        if(data_value == "Mobile/Web Channel Monitoring"){ iconClass = "icon-mobile-phone"}
                        if(data_value == "AS Service Statistics"){ iconClass = "icon-bar-chart"}
                        if(data_value == "AS Webapp Statistics"){ iconClass = "icon-globe"}
                        console.info(data_value);
                        if(data_value == currentTab){
                            str += '<li class="active">';
                        }else{
                            str += '<li>';
                        }
                        if( deployedToolboxes.toolboxes[i].childDashboards.length > 1){
                            str += '<a class="dropdown-toggle" data-toggle="dropdown"><i class="'+iconClass+'"></i> <span><br>' + deployedToolboxes.toolboxes[i].dashboard + ' <b class="caret"></b></span> </a>'+
                                '<ul class="dropdown-menu">';
                            for (var k = 0; k < deployedToolboxes.toolboxes[i].childDashboards.length; k++) {
                                str += '<li><a href="' + dashboardurl + '/' + deployedToolboxes.toolboxes[i].childDashboards[k][1] + '">' + deployedToolboxes.toolboxes[i].childDashboards[k][0] + '</a></li>';
                            }
                            str += '</ul>' +
                                '</li>';
                        }else if(deployedToolboxes.toolboxes[i].childDashboards.length == 1){
                            str += '<a href="' + dashboardurl + '/' + deployedToolboxes.toolboxes[i].childDashboards[0][1] + '"><i class="'+iconClass+'"></i> <span><br>' + deployedToolboxes.toolboxes[i].childDashboards[0][0] + '</span></a></li>';
                        }
                    }
                    if (str != "") {
                        $("#leftnav").html(str);
                    }
				}
			});
		});
