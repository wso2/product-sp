# HTTP Analytics

##1. Creating a Business Rule
+ Go to <SP_HOME> from the terminal and start the Dashboard runtime of WSO2 SP with one of the following commands:
    - On Windows:  `dashboard.bat --run`
    - On Linux/Mac OS:  `./dashboard.sh`

+ Start the Worker runtime of WSO2 SP with one of the following commands:
    - On Windows:  `worker.bat --run`
    - On Linux/Mac OS:  `./worker.sh`

+ Access the Business Rules Manager via one of the following URLs.
    - `http://<SP_HOST>:<HTTP_PORT>/business-rules` **`(eg: http://0.0.0.0:9090/business-rules)`**
    - `https://<SP_HOST>:<HTTPS_PORT>/business-rules` **`(eg: https://0.0.0.0:9443/business-rules)`**

+ Click on the **CREATE** button (when there are no business rules yet), or the **+** button (when at least one 
business 
rule exists).

+ Select **From Template** mode.

+ Select **HTTP-Analytics** template group.

+ Select **HTTP-Analytics-Processing** rule template.

+ Fill in the business rule form with your preferred values.
  > Default value points to the following, Requests Stream Source: A default http source,  Store for HTTP Analytics: HTTP_ANALYTICS_DB wso2 datasource pointting to an H2 database.
      
+ Click on **SAVE & DEPLOY** button.

##2. Viewing the HTTP Analytics Dashboard
+ Use the following curl command to send some request data,
  ```
  curl -v -X POST \
    http://<HOST>:8280/<NAME_OF_CREATED_BUSINESS_RULE>_0/RequestsStream \
    -H 'content-type: application/json' \
    -d '{
    "event": {
      "timestamp": <CURRENT_TIMESTAMP_IN_MILLISECOND>*,
      "serverName": "localhost",
      "serviceName": "A",
      "serviceMethod": "GET",
      "responseTime": 1000.00,
      "httpResponseCode": 200,
      "userAgent": "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405",
      "requestIP": "127.0.0.1"
    }
  }'
  ```
  *When sending multiple requests ensure that timestamp is changed so that the later requests have higher timestamp value.

+ From the dashboard run time, access the Dashboard Portal via one of the following URLs:
    - `http://<SP_HOST>:<HTTP_PORT>/portal` **`(eg: http://localhost:9290/portal)`**
    - `https://<SP_HOST>:<HTTPS_PORT>/portal` **`(eg: http://localhost:9643/portal)`**

+ Click the **View** button, on the **HTTP Analytics** dashboard.

+ Widgets in the dashboard will display HTTP Analysis data. 