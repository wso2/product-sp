# Twitter Analytics
## Prerequisites

+ Create a new Twitter application from the [Twitter Application Management page](https://apps.twitter.com/).

+ Open the created application, and click on the **Permissions** tab. In this tab, select the **Read and Write** 
option.

+ Click on the **Keys And Access Tokens** tab. Generate new access token by clicking **Create My Access Token**.

+ Collect following values from the **Keys and Access Tokens** tab:
  - Consumer Key
  - Consumer Secret
  - Access Token
  - Access Token Secret

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

+ Select **Twitter Analytics** template group.

+ Select **Popular Tweets Analysis** rule template.

+ Fill in the business rule form with your preferred values.
  > Use the **Consumer Key**, **Consumer Secret**, **Access Token** and **Access Token Secret**, that were collected 
  from the Twitter application.
  
+ Click on **SAVE & DEPLOY** button.

##2. Viewing the Twitter Analytics Dashboard
+ From the dashboard run time, access the Dashboard Portal via one of the following URLs:
    - `http://<SP_HOST>:<HTTP_PORT>/portal` **`(eg: http://localhost:9290/portal)`**
    - `https://<SP_HOST>:<HTTPS_PORT>/portal` **`(eg: http://localhost:9643/portal)`**

+ Click the **View** button, on the **Twitter Analytics** dashboard.

+ Widgets in the dashboard will display Twitter Analysis data. 