package org.wso2.bam.integration.tests.hive;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Enumeration;

import static junit.framework.Assert.fail;
import static org.testng.Assert.assertTrue;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class BAMJDBCHandlerTestCase {

    public static final int NUMBER_OF_EVENTS = 100;
    private HiveExecutionServiceStub hiveStub;
    private final Log log = LogFactory.getLog(BAMJDBCHandlerTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private static final String HIVE_SERVICE = "/services/HiveExecutionService";

    private DataPublisher dataPublisher;

    private String streamId = null;



    private void initializeHiveStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                     ":" + FrameworkSettings.HTTPS_PORT + HIVE_SERVICE;
        hiveStub = new HiveExecutionServiceStub(configContext, EPR);
        ServiceClient client = hiveStub._getServiceClient();
        Options option = client.getOptions();
        option.setTimeOutInMilliSeconds(10*60000);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           loggedInSessionCookie);
    }

    @Test(groups = {"wso2.bam"})
    public void publishData(){

        String host = null;
        try {
            host = getLocalHostAddress().getHostAddress();
        } catch (SocketException e) {
            host = "127.0.0.1";
        }
        try {
            dataPublisher = new DataPublisher("tcp://" + host + ":7611", "admin", "admin");
            defineEventStream();
            publishEvent();
            runHiveDataTypeTest();
            runJDBCHandlerTest();
        } catch (Exception e) {
           fail("Can't get data publisher: " + e.getMessage());
        }

    }

    private void runJDBCHandlerTest() {
        String[] queries = getHiveQueries("HiveJDBCHandlerPrimaryKeyTest");
        try {
            hiveStub.executeHiveScript(null, queries[0].trim());
            hiveStub.executeHiveScript(null, queries[1].trim());
            hiveStub.executeHiveScript(null, queries[2].trim());
            hiveStub.executeHiveScript(null, queries[3].trim());
            HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, queries[4].trim());

            assertTrue(null != results && results.length != 0, "No results are returned from jdbc handler test");
            HiveExecutionServiceStub.QueryResultRow[] rows = results[0].getResultRows();
            assertTrue(null != rows && rows.length != 0, "No results are returned from jdbc handler test");
            String[] vals = rows[0].getColumnValues();
            assertTrue(null != vals && vals.length != 0, "No results are returned from jdbc handler test");

            boolean resultsAreCorrect = false;
            if(Integer.parseInt(vals[2])==((event2RequestCount + event4RequestCount)*NUMBER_OF_EVENTS) &&
                    Integer.parseInt(vals[3])== ((faultCount2+faultCount4)*NUMBER_OF_EVENTS)  &&
                    Double.parseDouble(vals[4]) == ((avg_response_time2*NUMBER_OF_EVENTS + avg_response_time4* NUMBER_OF_EVENTS)/(2*NUMBER_OF_EVENTS)) &&
                    Float.parseFloat(vals[5]) == responseTime4 &&
                    Float.parseFloat(vals[6])==minResponseTime4){
                 resultsAreCorrect = true;
            }

            assertTrue(resultsAreCorrect,"Results are different from expected one: " + vals[2]+ ":" + vals[3]+ ":"
            +vals[4]+ ":" + vals[5] + ":" + vals[6]);

            hiveStub.executeHiveScript(null, queries[5].trim());
            hiveStub.executeHiveScript(null, queries[6].trim());
            hiveStub.executeHiveScript(null, queries[7].trim());

            HiveExecutionServiceStub.QueryResult[] newResults = hiveStub.executeHiveScript(null, queries[8].trim());
            assertTrue(null != newResults && newResults.length != 0, "No results are returned from jdbc handler test");
            HiveExecutionServiceStub.QueryResultRow[] newRow = newResults[0].getResultRows();
            assertTrue(null != newRow && newRow.length != 0, "No results are returned from jdbc handler test");
            String[] newValue = newRow[0].getColumnValues();
            assertTrue(null != newValue && newValue.length != 0, "No results are returned from jdbc handler test");

            boolean newResultsAreCorrect = false;
            if(Integer.parseInt(newValue[2]) == event4RequestCount * NUMBER_OF_EVENTS  &&
                    Integer.parseInt(newValue[3]) == faultCount4 * NUMBER_OF_EVENTS &&
                    Double.parseDouble(newValue[4]) == (avg_response_time4 * NUMBER_OF_EVENTS)/NUMBER_OF_EVENTS &&
                    Float.parseFloat(newValue[5]) == responseTime4 &&
                    Float.parseFloat(newValue[6]) == minResponseTime4){
                newResultsAreCorrect = true;
            }
            assertTrue(newResultsAreCorrect,"Results are different from expected one: " + newValue[2]+ ":" + newValue[3]+ ":"
                                         +newValue[4]+ ":" + newValue[5] + ":" + newValue[6]);

        } catch (RemoteException e) {
            fail("Failed while executing hive script HiveJDBCHandlerPrimaryKeyTest " + e.getMessage());
        } catch (HiveExecutionServiceHiveExecutionException e) {
            fail("Failed while executing hive script HiveJDBCHandlerPrimaryKeyTest " + e.getMessage());
        }
    }


    private void runHiveDataTypeTest() {
        try {
            String[] queries = getHiveQueries("TestScriptForHiveDataTypes");
            hiveStub.executeHiveScript(null, queries[0].trim());
            HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, queries[1].trim());
            assertTrue(null != results && results.length != 0, "No results are returned from jdbc handler test");
            HiveExecutionServiceStub.QueryResultRow[] rows = results[0].getResultRows();
            assertTrue(null != rows && rows.length != 0, "No results are returned from jdbc handler test");
            String[] vals = rows[0].getColumnValues();
            assertTrue(null != vals && vals.length != 0, "No results are returned from jdbc handler test");

            for (String val : vals) {
                assertTrue(null != val && !val.isEmpty(), "Value is null or empty");
            }
        } catch (HiveExecutionServiceHiveExecutionException e) {
            fail("Failed while excecuting hive script " + e.getMessage());
        }catch (Exception e){
            fail("Error when trying to run hive script: "+ e.getMessage());
        }
    }


    private String[] getHiveQueries(String resourceName){
        String[] queries = null;
        try {
            initializeHiveStub();
        } catch (Exception e) {
            fail("Error while initializing hive stub: " + e.getMessage());
        }

        URL url = BAMJDBCHandlerTestCase.class.getClassLoader().getResource(resourceName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String script ="";
            String line = null;
            while ((line = bufferedReader.readLine())!=null){
                script += line;
            }
            queries =script.split(";");
        } catch (Exception e) {
            fail("Error while reading resource : " + resourceName);
        }
        return queries;
    }

    private String defineEventStream(){
            try {
                streamId = dataPublisher.defineStream(getStreamDef("jdbc.handler.integration.test.stream","1.0.0"));
                log.info("JDBC HANDLER SREAM DEFINED stream id is:" + streamId);
            } catch (Exception e) {
                fail("Failed when defining stream: " + e.getMessage() );
            }
            return streamId;
        }

    private StreamDefinition getStreamDef(String streamName, String version)
            throws MalformedStreamDefinitionException {
        StreamDefinition eventStreamDefinition = new StreamDefinition(streamName,version);

        eventStreamDefinition.addMetaData("ip_address",
                                          AttributeType.STRING);

        eventStreamDefinition.addPayloadData("api",
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData("api_version",
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData("user_id",
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData("consumer_key",
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData("request_count",
                                             AttributeType.LONG);
        eventStreamDefinition.addPayloadData("fault_count", AttributeType.INT);
        eventStreamDefinition.addPayloadData("avg_response_time",
                                             AttributeType.DOUBLE);
        eventStreamDefinition.addPayloadData("max_response_time",
                                             AttributeType.FLOAT);
        eventStreamDefinition.addPayloadData("min_response_time",
                                             AttributeType.FLOAT);
        eventStreamDefinition.addPayloadData("is_api_name_added_today",
                                             AttributeType.BOOL);
        return eventStreamDefinition;
    }

        private void publishEvent() throws AgentException {
            for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
                Event event1 =getEvent1();
                dataPublisher.publish(event1);
                Event  event2=getEvent2();
                dataPublisher.publish(event2);
                Event event3 = getEvent3();
                dataPublisher.publish(event3);
                Event event4=getEvent4();
                dataPublisher.publish(event4);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            }
        }

    private long event1RequestCount = 4;
    private long event2RequestCount = 10;
    private long event3RequestCount = 8;
    private long event4RequestCount = 20;

    private double avg_response_time1= 1.25;
    private double avg_response_time2 = 1.5;
    private double avg_response_time3 = 2.25;
    private double avg_response_time4 = 4.5;

    private int faultCount2 = 1;
    private int faultCount4 = 0;

    private float responseTime2 = 3.3f;
    private float responseTime4 =  3.5f;

    private float minResponseTime2 = 0.9f;
    private float minResponseTime4 = 0.7f;


    private Event getEvent1() {
       Event event = new Event(streamId, System.currentTimeMillis(), new Object[]{"10.100.3.175"}, null,
                  new Object[]{"twitter","1.0.0","Kasun","WQEEWWSJDSDIKHDSSDBSDGJHGGDSDSHJ",
                  event1RequestCount,0,avg_response_time1,2.1f,0.6f,true});
       return event;
    }

    private Event getEvent2(){
        Event event = new Event(streamId, System.currentTimeMillis(),new Object[]{"10.100.3.176"},null,
                                new Object[]{"facebook","1.0.5","Kushan","GJSKDSKJDHSFHSIURSJSBDJSBDSDS",
                                             event2RequestCount,faultCount2,avg_response_time2,responseTime2,
                                        minResponseTime2,false});
        return event;
    }

    private Event getEvent3(){
        Event event = new Event(streamId,System.currentTimeMillis(),new Object[]{"10.100.3.177"},null,
                                new Object[]{"OpenCalais","2.3.5","Rangana","KULOMJHFGDFDFDTYYTYYUDXXCBVM",
                                             event3RequestCount,0,avg_response_time3,4.7f,0.2f,false});
        return event;
    }

    private Event getEvent4(){
        Event event = new Event(streamId, System.currentTimeMillis(),new Object[]{"10.100.3.178"},null,
                                new Object[]{"facebook","1.0.5","Nuwan","ZXXZXVCVCVCBVBBMNMNCVBVBNBNMN",
                                             event4RequestCount,faultCount4,avg_response_time4,responseTime4,
                                        minResponseTime4,false});
        return event;
    }


    private InetAddress getLocalHostAddress() throws SocketException {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }

            return null;
        }
    }

