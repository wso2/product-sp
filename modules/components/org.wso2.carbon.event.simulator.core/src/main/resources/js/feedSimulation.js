function feedSimulationExecutor(){
$(document).ready(function(){


 var feedSimulationConfiguration= {
             	        "orderByTimeStamp" : "false",
                        "streamConfiguration" :[
//             		                        {
//            	 			                "simulationType" : "RandomDataSimulation",
//            								"streamName": "cseEventStream",
//            								"events": "10",
//            								"delay": "1000",
//            								"attributeConfiguration":[
////            								    {
////            										"type": "PROPERTYBASED",
////            								        "category": "Contact",
////            								        "property": "Full Name",
////            								    },
//           								        {
//                                                    "type": "CUSTOMDATA",
//            								        "list": "WSO2,IBM"
//            								    },
//            								    {
//                                                    "type": "REGEXBASED",
//            								        "pattern": "[+]?[0-9]*\\.?[0-9]+"
//            								    },
//            								    {
//            								        "type": "PRIMITIVEBASED",
//            								        "min": "2",
//            								        "max": "200",
//            								        "length": "2",
//            								    }
//
//            								  ]
//            	    						},
                                            {
                                            "simulationType" : "RandomDataSimulation",
                                            "streamName": "cseEventStream2",
                                            "events": "20",
                                            "delay": "1000",
                                            "attributeConfiguration":[
//            								    {
//            										"type": "PROPERTYBASED",
//            								        "category": "Contact",
//            								        "property": "Full Name",
//            								    },
                                                {
                                                    "type": "CUSTOMDATA",
                                                    "list": "WSO2,IBM"
                                                },
                                                {
                                                    "type": "REGEXBASED",
                                                    "pattern": "[+]?[0-9]*\\.?[0-9]+"
                                                },
                                                {
                                                    "type": "PRIMITIVEBASED",
                                                    "min": "2",
                                                    "max": "200",
                                                    "length": "2",
                                                }

                                              ]
                                            },
            	    						{
             								"simulationType" : "FileFeedSimulation",
             			 					"streamName" : "cseEventStream",
             							    "fileName"   : "cseteststream.csv",
             							    "delimiter"  : ",",
             							    "delay"		 : "1000"
             							 	}
  //           							 	{
//                                            "simulationType" : "FileFeedSimulation",
//                                            "streamName" : "cseEventStream2",
//                                            "fileName"   : "cseteststream2.csv",
//                                            "delimiter"  : ",",
//                                            "delay"		 : "1000"
//                                            }
//             			 					{
//             								"simulationType" : "RandomDataSimulation",
//            	 							"streamName": "inputStream3",
//            								"events": "5",
//            								"delay": "200",
//            								"attributeConfiguration": [
//            								    {
//
//            								        "type": "PROPERTYBASED",
//            								        "category": "Contact",
//            								        "property": "Full Name",
//            								    },
//            								    {
//
//            								        "type": "REGEXBASED",
//            								        "pattern": "[+]?[0-9]*\\.?[0-9]+"
//            								    },
//            								    {
//
//            								        "type": "PRIMITIVEBASED",
//            								        "min": "2",
//            								        "max": "200",
//            								        "length": "2",
//            								    },
//            								    {
//
//            								         "type": "custom",
//            								        "list": "2,3,4"
//            								    },
//            								]
//            	    						}
            	       ]
             		};

//alert(JSON.stringify(feedSimulationConfiguration));


if (typeof feedSimulationConfiguration != 'undefined' ) {
                   if(typeof feedSimulationConfiguration !='null') {

                         $.ajax({
                                 url: "http://localhost:8080/EventSimulation/feedSimulation",
                                 type: "POST",
                                 data: JSON.stringify(feedSimulationConfiguration),

                                 success: function(response) {
                                     console.log(response);
                                 },
                                 error: function(e) {
                                     console.log(e.statusText);
                                 }
                         });

                    }
                 }
});
//});

}

function stopSimulation(){
$(document).ready(function(){

 $.ajax({
         url: "http://localhost:8080/EventSimulation/feedSimulation/stop",
         type: "POST",
         success: function(response) {
             console.log(response);
         },
         error: function(e) {
             console.log(e.statusText);
         }
 });


});
}

function pauseSimulation(){
$(document).ready(function(){

 $.ajax({
         url: "http://localhost:8080/EventSimulation/feedSimulation/pause",
         type: "POST",
         success: function(response) {
             console.log(response);
         },
         error: function(e) {
             console.log(e.statusText);
         }
 });
});
}

function resumeSimulation(){
$(document).ready(function(){

 $.ajax({
         url: "http://localhost:8080/EventSimulation/feedSimulation/resume",
         type: "POST",
         success: function(response) {
             console.log(response);
         },
         error: function(e) {
             console.log(e.statusText);
         }
 });
});
}