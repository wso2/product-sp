#WSO2 Stream Processor


Running Samples
---------------

To start the WSO2 Stream Processor with a selected sample configuration

* Open a shell in Linux and go to the <CARBON_HOME>\bin directory.
* Execute one of the following commands,  where <file_name> denotes the configuration file name.
  On Linux/Solaris/Mac: ./run-sample.sh -sn <file_name>

For example, to start the Integration server with the passthrough sample configuration on Linux/Solaris/Mac, run the following command:
./run-sample.sh passthrough.iflow


Also configuration can be manually deployed to server by dropping any sample configuration file from <CARBON_HOME>/samples/basic-routing/
to <CARBON_HOME>/deployment/integration-flows/ directory.

MSF4J SimpleStockQuote fat jar sample is used as the backend service for the integration flow samples. Follow the instruction in the
README.md resides in <CARBON_HOME>/samples/Services/StockquoteService/ to start the backend service.


How to test the sample
----------------------

Use following cURL commands.

curl http://localhost:9090/stockquote/stocks

You should get a successful response according the sample configurations if everything worked fine.
