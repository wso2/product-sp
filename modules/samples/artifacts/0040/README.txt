1. Copy {WSO2SPHome}/modules/samples/artifacts/0040/EmailSinkSiddhiAppTest.sddhi file to {WSO2_SP_Home}/wso2/worker/deployment/siddhi-files/

2. Replace the value given under'username', 'passowrd' and 'address' according to your gmail account. 
   *If you haven't enabled access to "less secure apps" enable it via https://myaccount.google.com/lesssecureapps 

3. Start the worker using ./{WSO2SPHome}/bin/worker.sh

4. Run following curls commands to send some login events

   curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "EmailSinkSiddhiAppTest",
      "streamName": "FooStream",
      "timestamp": null,
      "data": [
        "JONE",
        "25",
        "USA",
      ]
    }'

 curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "EmailSinkSiddhiAppTest",
      "streamName": "FooStream",
      "timestamp": null,
      "data": [
        "RICKY",
        "25",
        "UK",
      ]
    }'

 curl -X POST \
      http://localhost:9090/simulation/single \
      -H 'content-type: text/plain' \
      -d '{
      "siddhiAppName": "EmailSinkSiddhiAppTest",
      "streamName": "FooStream",
      "timestamp": null,
      "data": [
        "AMILA",
        "24",
        "SL",
      ]
    }'

5. check the mails in your email account.


