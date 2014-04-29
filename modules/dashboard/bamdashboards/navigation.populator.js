var esb_mediation_stats = ['ESB Mediation Statistics',[['ESB - Proxy','esb_proxy.jsp'],['ESB - Sequence','esb_sequence.jsp'],['ESB - Endpoint','esb_endpoint.jsp']]];
var as_service_stats = ['AS Service Statistics',[['Service Statistics','index.jsp']]];

$(document).ready(function(){
			var dashboardurl = 'https://'+window.location.host+'/bamdashboards/';
			$.ajax({
				type: "GET",
				url: "../dashboard.xml",
				dataType: "xml",
				success: function(xml) {
					var Dashboard;
					//alert($(xml))
					$(xml).find('dashboard').each(function(){
						if($(this).find('name').text() == 'esb'){
							var navstring = '<li class="nav-header">'+esb_mediation_stats[0]+'</li>';
							for(var i=0;i<esb_mediation_stats[1].length;i++){
	  							navstring = navstring + '<li><a href="'+dashboardurl+'mediation_stats/'+esb_mediation_stats[1][i][1]+'">'+esb_mediation_stats[1][i][0]+'</a></li>';
							}
							$("#leftnav").append(navstring);
						}
						if($(this).find('name').text() == 'as'){
							var navstring = '<li class="nav-header">'+as_service_stats[0]+'</li>';
							for(var i=0;i<as_service_stats[1].length;i++){
	  							navstring = navstring + '<li><a href="'+dashboardurl+'service_stats/'+as_service_stats[1][i][1]+'">'+as_service_stats[1][i][0]+'</a></li>';
							}
							$("#leftnav").append(navstring);
						}	
					});
				}
			});
		});
