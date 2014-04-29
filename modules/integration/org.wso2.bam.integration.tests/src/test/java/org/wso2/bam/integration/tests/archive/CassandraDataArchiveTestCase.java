package org.wso2.bam.integration.tests.archive;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.bam.cassandra.data.archive.stub.CassandraArchivalServiceException;
import org.wso2.carbon.bam.cassandra.data.archive.stub.CassandraArchivalServiceStub;
import org.wso2.carbon.bam.cassandra.data.archive.stub.util.ArchiveConfiguration;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

/** After running the cassandra archive data test, We'll run BAMJDBCHandlerTestCase to verify everything working fine
 * after running the archival job
 */
public class CassandraDataArchiveTestCase {

    public static final String ARCHIVAL_TEST_STREAM_NAME = "jdbc_handler_test_stream_arch_test";
    public static final String ARCHIVAL_TEST_VERSION = "1.0.1";
    private HiveExecutionServiceStub hiveStub;
    private CassandraArchivalServiceStub cassandraArchivalServiceStub;
    private final Log log = LogFactory.getLog(CassandraDataArchiveTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private static final String HIVE_SERVICE = "/services/HiveExecutionService";
    private static final String CASSANDRA_ARCHIVAL_SERVICE ="/services/CassandraArchivalService";

    private DataPublisher dataPublisher;

    private String streamId = null;



    private void initializeStubs() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

        initHiveSStub(loggedInSessionCookie,configContext);
        initCassandraArchiveStub(loggedInSessionCookie,configContext);
    }

    private void initCassandraArchiveStub(String loggedInSessionCookie, ConfigurationContext configContext) throws AxisFault {
        String cassandraArchivalServiceEPR = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + CASSANDRA_ARCHIVAL_SERVICE;
        cassandraArchivalServiceStub = new CassandraArchivalServiceStub(configContext, cassandraArchivalServiceEPR);
        ServiceClient client = cassandraArchivalServiceStub._getServiceClient();
        Options option = client.getOptions();
        option.setTimeOutInMilliSeconds(10*60000);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    private void initHiveSStub(String loggedInSessionCookie, ConfigurationContext configContext) throws AxisFault {
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
            //Publish yesterday's data
            publishEvent(-1);
            publishEvent(0);
            Thread.sleep(5000);
            runArchivalJob();
            runJDBCHandlerTest();
        } catch (Exception e) {
            fail("Can't get data publisher: " + e.getMessage());
        }

    }

    private void runArchivalJob() throws ParseException {
        try {
            initializeStubs();
        } catch (Exception e) {
            fail("Error while initializing hive stub: " + e.getMessage());
        }
        ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration();
        archiveConfiguration.setStreamName(ARCHIVAL_TEST_STREAM_NAME);
        archiveConfiguration.setVersion(ARCHIVAL_TEST_VERSION);
        archiveConfiguration.setSchedulingOn(false);
        Date pastDate =  new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2012");
        archiveConfiguration.setStartDate(pastDate);
        archiveConfiguration.setEndDate(Calendar.getInstance().getTime());
        archiveConfiguration.setUserName("admin");
        archiveConfiguration.setPassword("admin");
        try {
            cassandraArchivalServiceStub.archiveCassandraData(archiveConfiguration);
        } catch (RemoteException e) {
            fail("Failed when trying to archive data: " + e.getMessage());
        } catch (CassandraArchivalServiceException e) {
            fail("Failed when archiving Cassandra data: " + e.getMessage());
        }
    }

    private void runJDBCHandlerTest() {
        String[] queries = getHiveQueries("ArchiveCassandraDataTest");
        try {
            hiveStub.executeHiveScript(null, queries[0].trim());
            HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, queries[1].trim());

            assertTrue(null != results && results.length != 0, "No results are returned from jdbc handler test");
            HiveExecutionServiceStub.QueryResultRow[] rows = results[0].getResultRows();
            assertTrue(null != rows && rows.length != 0, "No results are returned from jdbc handler test");
            String[] vals = rows[0].getColumnValues();
            assertTrue(null != vals && vals.length != 0, "No results are returned from jdbc handler test");

            boolean resultsAreCorrect = false;
            if(vals[2].equals("1600") && vals[3].equals("0") && vals[4].equals("2.25") && vals[5].equals("4.7") && vals[6].equals("0.2")){
                resultsAreCorrect = true;
            }

            assertTrue(resultsAreCorrect,"Results are different from expected one: " + vals[2]+ ":" + vals[3]+ ":"
                    +vals[4]+ ":" + vals[5] + ":" + vals[6]);

        } catch (RemoteException e) {
            fail("Failed while executing hive script ArchiveCassandraDataTest " + e.getMessage());
        } catch (HiveExecutionServiceHiveExecutionException e) {
            fail("Failed while executing hive script ArchiveCassandraDataTest " + e.getMessage());
        }
    }


    private String[] getHiveQueries(String resourceName){
        String[] queries = null;
        URL url = CassandraDataArchiveTestCase.class.getClassLoader().getResource(resourceName);

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
            streamId = dataPublisher.defineStream(getStreamDef(ARCHIVAL_TEST_STREAM_NAME, ARCHIVAL_TEST_VERSION));
        } catch (Exception e) {
            fail("Failed when defining stream: " + e.getMessage() );
        }
        return streamId;
    }

    private StreamDefinition getStreamDef(String streamName, String version)
            throws MalformedStreamDefinitionException {
        StreamDefinition eventStreamDefinition = new StreamDefinition(streamName,version);
        eventStreamDefinition.setNickName("archive_test");
        eventStreamDefinition.setDescription("Archive data test");

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

    private void publishEvent(int noOfPastDays) throws AgentException {
        for (int i = 0; i < 100; i++) {
            Event event1 =getEvent1(noOfPastDays);
            dataPublisher.publish(event1);
            Event  event2=getEvent2(noOfPastDays);
            dataPublisher.publish(event2);
            Event event3 = getEvent3(noOfPastDays);
            dataPublisher.publish(event3);
            Event event4=getEvent4(noOfPastDays);
            dataPublisher.publish(event4);
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

    private Event getEvent1(int noOfPastDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, noOfPastDays);
        Event event = new Event(streamId, calendar.getTime().getTime(), new Object[]{"10.100.3.175"}, null,
                new Object[]{"twitter", "1.0.0", "Kasun", "WQEEWWSJDSDIKHDSSDBSDGJHGGDSDSHJ",
                        event1RequestCount, 0, avg_response_time1, 2.1f, 0.6f, true});
        return event;
    }

    private Event getEvent2(int noOfPastDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, noOfPastDays);
        Event event = new Event(streamId, calendar.getTime().getTime(), new Object[]{"10.100.3.176"}, null,
                new Object[]{"facebook", "1.0.5", "Kushan", "GJSKDSKJDHSFHSIURSJSBDJSBDSDS",
                        event2RequestCount, 1, avg_response_time2, 3.3f, 0.9f, false});
        return event;
    }

    private Event getEvent3(int noOfPastDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, noOfPastDays);
        Event event = new Event(streamId, calendar.getTime().getTime(), new Object[]{"10.100.3.177"}, null,
                new Object[]{"OpenCalais", "2.3.5", "Rangana", "KULOMJHFGDFDFDTYYTYYUDXXCBVM",
                        event3RequestCount, 0, avg_response_time3, 4.7f, 0.2f, false});
        return event;
    }

    private Event getEvent4(int noOfPastDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, noOfPastDays);
        Event event = new Event(streamId, calendar.getTime().getTime(), new Object[]{"10.100.3.178"}, null,
                new Object[]{"facebook", "1.0.5", "Nuwan", "ZXXZXVCVCVCBVBBMNMNCVBVBNBNMN",
                        event4RequestCount, 0, avg_response_time4, 3.5f, 0.7f, false});
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
