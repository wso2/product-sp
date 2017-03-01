function deployExecutionPlan(){
$(document).ready(function(){

var input= {
        "executionPlanName": "executionPlan1",
        "inputStream":[
            {
                "streamDefinition":"define stream cseEventStream (symbol string, price float, volume float);",
                "streamName":"cseEventStream",
                "streamAttributeDtos" : [
                                {
                                "attributeName":"symbol",
                                "attributeType":"String"
                                },
                                {
                                "attributeName":"price",
                                 "attributeType":"Float"
                                },
                                {
                                "attributeName":"volume",
                                "attributeType":"Float"
                                }
                                ]
            },
            {
                "streamDefinition":"define stream cseEventStream2 (symbol string, price float, volume float);",
                "streamName":"cseEventStream2",
                "streamAttributeDtos" : [
                                {
                                "attributeName":"symbol",
                                "attributeType":"String"
                                },
                                {
                                "attributeName":" price ",
                                 "attributeType":"Float"
                                },
                                {
                                "attributeName":" volume",
                                "attributeType":"Float"
                                }
                                ]
            }
        ],
        "OutputStream":[
            {
                "streamDefinition":"define stream outputStream (maximum double,fullName String);",
                "streamName":"outputStream",
                "streamAttributeDtos" : [
                                {
                                "attributeName":" maximum ",
                                 "attributeType":" Double"
                                },
                                {
                                "attributeName":"fullName",
                                "attributeType":"String"
                                }
//                                {
//                                "attributeName":"lastName",
//                                "attributeType":"String"
//                                }
                                ]
            }
        ],
        "Queries":[
            {
               "queryName" : "query1",
//               "queryDefinition":"@info(name = 'query1') from inputStream1 select maximum(price2, price3) as maximum,fullName as fullName insert into outputStream;"
//              "queryDefinition":"@info(name = 'query1') from cseEventStream#window.length(5) as cse1 join cseEventStream2#window.length(5) as cse2 on cse1.symbol==cse2.symbol select cse1.symbol insert into outputStream;"
             "queryDefinition":"@info(name = 'query1') from cseEventStream as cse1 join cseEventStream2#window.length(1) as cse2 select cse1.symbol as symbol, cse2.symbol as symbol2 insert into outputStream;"
            }
        ]
    };

//alert(JSON.stringify(input));


if (typeof input != 'undefined' ) {
                   if(typeof input !='null') {

                         $.ajax({
                                 url: "http://localhost:8080/ExecutionPlan/deploy",
                                 type: "POST",
                                 data: JSON.stringify(input),

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