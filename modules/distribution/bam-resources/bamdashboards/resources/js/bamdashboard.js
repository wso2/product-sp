var redirectToDashboard = function(){ 
			$.ajax({
				type: "GET",
				url: "getDeployedToolboxes.jag",
				dataType: "json",
				success: function(json) {
					var deployedToolboxes = json;
					if(deployedToolboxes.toolboxes.length == 0){
						location.href = "index.jag?noToolboxes=true";
					}
					else{					
						for (var i=0; i<deployedToolboxes.toolboxes.length; i++){
							for (var k=0; k<deployedToolboxes.toolboxes[i].childDashboards.length; k++){
								location.href = deployedToolboxes.toolboxes[i].childDashboards[k][1];
								break;
							}
							break;
						}
					}
				}
			});
};
		
