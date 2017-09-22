# test-results-service - MS4FJ Service for Collectin Siddhi Events

The service can be used for validating events which received to siddhi and published from siddhi.
has two main operations one for collecting(POST) events and another for retrieving(GET) those events

- HashMap is used to store events in the service.
- header parameter named  className is used as the key and the event details as the value in HashMap
- when retrieving events className should be provided as the path-parameter in the GET request
- Once the event verified the HashMap should be cleared out

##Deployment of the service:##
With the proposed solution, the service should be deployed on a docker container of the kubernetes cluster, so that the service should have to be placed inside the docker image which we used to create containers in the cluster.

