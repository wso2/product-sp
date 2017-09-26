# test-results-service - MS4FJ Service for Collecting Siddhi Events

The service can be used for validating events which received to siddhi and published from siddhi.
has two main operations one for collecting(POST) events and another for retrieving(GET) those events

- HashMap is used to store events in the service.
- header parameter named  className is used as the key and the event details as the value in HashMap
- when retrieving events className should be provided as the path-parameter in the GET request
- Once the event verified the HashMap should be cleared out

##Deployment of the service:
With the proposed solution, the service should be deployed on a docker container of the kubernetes cluster, so that the service should have to be placed inside the docker image which we used to create containers in the cluster. In that case user can start this service automatically or when it is required.

##How to use the service:
When user perform tests on shiddi, it will produce related events. User should publish those events to this service using
 POST request to http://<hostname>:8080/testresults. For this, the user can use siddhi_http_sink feature.
 So that all relevant events are stored in the HashMap
 Then for the verification part user can retrieve events using following requests.
    
 1. Retrieve a single event 
 
    ```http://<hostname>:8080/testresults/{testCaseName}?eventIndex={index}```
   
 2. Retrieve multiple events
    
    ```http://<hostname>:8080/testresults/{testCaseName}```
 
 3. Retrieve events count of particular testcase
 
    ```http://<hostname>:8080/testresults/{testCaseName}/count```