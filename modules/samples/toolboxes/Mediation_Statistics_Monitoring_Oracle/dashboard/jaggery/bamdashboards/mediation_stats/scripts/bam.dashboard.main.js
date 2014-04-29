$(function () {
    $("#server-dd").change(function(){
	    var selectedServer = $("#server-dd option:selected").text();
		var rType = $("#resource_type").val();
		$("#rname-dd").find('option').remove();
		if(selectedServer==''){
			triggerCollect();
		}
		else{
			populateResourceNameCombo(selectedServer,rType);		
		}
	});
    $("#rname-dd").change(function(){
	    triggerCollect();
	});
    //$("#rname-dd").ufd({log:true});
    //$("#operation-dd").ufd({log:true});
    $("#clearSelectionBtn").click(function(){
        $("#server-dd option:first-child").attr("selected", "selected");
	$("#rname-dd").find('option').remove();
        
	triggerCollect();
        $("#rname-dd").find('option').remove();
	triggerCollect();
    });
    $("#timely-dd button").click(function(){
        $("#timely-dd button").removeClass('btn-primary');
        $(this).addClass('btn-primary');
        triggerCollect();
    });
});
function triggerCollect(){
	var resourceType = $("#resource_type").val();
        var selectedServer = $("#server-dd").find('option:selected').text();
        var selectedResourceName = $("#rname-dd").find('option:selected').text();
        var timeGrouping = $("#timely-dd button.btn-primary").text();
        reloadIFrame({server:selectedServer,
            rname:selectedResourceName,rtype:resourceType,timeGroup:timeGrouping});
};
function reloadIFrame(param){
    var params = param || {};
    var server = param.server || "";
    var rname = param.rname || "";
    var rtype = param.rtype || "";
    var t = param.timeGroup || "";
    $("iframe").each(function(){
        //var id = $(this).attr('id');
        var currentUrl = $(this).attr('src');
        if(currentUrl.indexOf('?')){
            var absUrl = currentUrl.split('?');
            currentUrl = absUrl[0];
        }
        var newUrl = currentUrl+"?server="+encodeURI(server)+"&rtype="+
            encodeURI(rtype)+"&rname="+encodeURI(rname)+"&t="+t;
        $(this).attr('src',newUrl);
    });
};
function populateCombo(id,data){
	
}
$(document).ready(function(){
	var resourceType = $("#resource_type").val();
	$.ajax({
       		url:'populate_combos_ajaxprocessor.jag?rtype='+resourceType+'',
		dataType:'json', 
		success:function(result){
			
			var options = "<option value='__default__'></option>";
			if(result){
				for(var i=0;i<result.length;i++){
					var data = result[i];
					for(var key in data){
						options = options + "<option>"+data[key]+"</option>"
					}
				}
			}
            $("#server-dd").find('option').remove();
            $("#server-dd").append(options);
		    //$("#server-dd").ufd({log:true,addEmphasis: true});
  	    }
		
	});

    //If no user action, reload page to prevent session timeout.
    var wintimeout;
    function setWinTimeout() {
        wintimeout = window.setTimeout("location.reload(true)",1740000); //setting timeout for 29 minutes. Actual timeout is 30 minutes.
    }
    $('body').click(function() {
        window.clearTimeout(wintimeout);
        setWinTimeout();
    });
    setWinTimeout();
    /*$.getJSON("populate_combos_ajaxprocessor.jag?server=10.150.3.174:9443",
        function(data){
          alert(data);
        });*/
});
function populateResourceNameCombo(server,rtype){
     $.ajax({
       		url:'populate_combos_ajaxprocessor.jag?server='+server+'&rtype='+rtype+'',
		dataType:'json',
		success:function(result){
			console.info(result);
			var options = "<option value='__default__'></option>";
			if(result){			
				for(var i=0;i<result.length;i++){
					var data = result[i];
					for(var key in data){
						options = options + "<option>"+data[key]+"</option>"
					}
				}
			}
            
            $("#rname-dd").append(options);
		    triggerCollect();//$("#rname-dd").ufd({log:true,addEmphasis: true});
  	    }
	

	});
	
};



