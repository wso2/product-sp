function singleEventSimulation(){
$(document).ready(function(){


 var attributeValue=["WSO2","34"];

 var singleEventSimulationConfig={
            "streamName":"cseEventStream",
            "attributeValues":["WSO2"]
            };

// alert(JSON.stringify(singleEventSimulationConfig));


if (typeof singleEventSimulationConfig != 'undefined' ) {
                   if(typeof singleEventSimulationConfig !='null') {

                         $.ajax({
                                 url: "http://localhost:8080/EventSimulation/singleEventSimulation",
                                 type: "POST",
                                 data: JSON.stringify(singleEventSimulationConfig),

                                 success: function(response) {
                                    console.log("success");
                                     console.log(response);
                                 },
                                 error: function(e) {
                                 console.log("failure");
                                     console.log(JSON.stringify(e));
                                 }
                         });

                    }
                 }
});
}