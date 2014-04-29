$(document).ready(function(){
			$.ajax({
				type: "GET",
				url: "dashboard.xml",
				dataType: "xml",
				success: function(xml) {
					var primaryDashboard;
					//alert($(xml))
					$(xml).find('dashboard').each(function(){
						var isPrimary = $(this).attr('isPrimary');
						if(isPrimary=="true"){
							primaryDashboard = $(this).find('name').text();	
						}
					});
					if(primaryDashboard == "esb"){
						location.href = "https://"+window.location.host+"/bamdashboards/mediation_stats/index.jsp";
					}
					else if(primaryDashboard == "as"){
						location.href = "https://"+window.location.host+"/bamdashboards/service_stats/index.jsp";
					}
				}
			});
		});
