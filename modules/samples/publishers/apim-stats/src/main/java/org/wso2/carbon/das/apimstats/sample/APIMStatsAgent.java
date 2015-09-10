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

package org.wso2.carbon.das.apimstats.sample;

import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.Enumeration;
import java.util.Random;


public class APIMStatsAgent {
    private static final String HTTPD_LOG_STREAM = "org.wso2.apimgt.statistics.request";
    private static final String STREAM_VERSION = "1.0.0";

    private static final String[] CONSUMER_KEY = {"consumerKey1", "consumerkey2", "consumerkey3", "consumerKey4"};
    private static final String[] CONTEXT = {"sales", "billing", "inventory"};
    private static final String[] API_VERSION = {"1.0.0", "2.0.0", "3.0.0"};
    private static final String[] API = {"opportunities", "consumers", "stocks", "usage", "customers"};
    private static final String[] RESOURCE_PATH = {"res1", "res2", "res3"};
    private static final String[] METHOD = {"GET", "POST", "PUT", "DELETE"};
    private static final String[] VERSION = {"1.0.1", "2.0.1", "3.0.1"};
    private static final int[] REQUEST = {1, 10, 20, 500, 1000};
    private static final String[] USER_ID = {"user1", "user2", "user3"};
    private static final String[] TENANT_DOMAIN = {"abc.com", "cde.lk", "maninda.us"};
    private static final String[] HOST_NAME = {"127.0.0.1", "127.0.0.2", "127.0.0.3", "127.0.0.4"};
    private static final String[] API_PUBLISHER = {"pub1", "pub2", "pub3", "pub4", "pub5"};
    private static final String[] APP_NAME = {"app1", "app2", "app3", "app4", "app5"};
    private static final String[] APP_ID = {"appID1", "appID2", "appID3", "appID4", "appID5"};
    private static final String[] USER_AGENT = {"ua1", "ua2", "ua3", "ua4", "ua5"};
    private static final String[] TIER = {"gold", "silver", "bronze", "unlimited"};
    private static final Random RANDOM_GEN = new Random();


    private static final String SAMPLE_LOG_PATH = System.getProperty("user.dir") + "/resources/access.log";
    private static final int defaultThriftPort = 7611;
    private static final int defaultBinaryPort = 9611;

    public static void main(String[] args) throws DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            TransportException,
            DataEndpointException,
            DataEndpointConfigurationException,
            FileNotFoundException,
            SocketException,
            UnknownHostException {
        System.out.println("Starting APIM Statistics Agent");
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        AgentHolder.setConfigPath(getDataAgentConfigPath());
        String host = getLocalAddress().getHostAddress();

        String type = getProperty("type", "Thrift");
        int receiverPort = defaultThriftPort;
        if (type.equals("Binary")) {
            receiverPort = defaultBinaryPort;
        }
        int securePort = receiverPort + 100;

        String url = getProperty("url", "tcp://" + host + ":" + receiverPort);
        String authURL = getProperty("authURL", "ssl://" + host + ":" + securePort);
        String username = getProperty("username", "admin");
        String password = getProperty("password", "admin");

        DataPublisher dataPublisher = new DataPublisher(type, url, authURL, username, password);

        String streamId = DataBridgeCommonsUtils.generateStreamId(HTTPD_LOG_STREAM, STREAM_VERSION);
        publishLogEvents(dataPublisher, streamId);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        dataPublisher.shutdown();
    }

    public static String getDataAgentConfigPath() {
        File filePath = new File("src" + File.separator + "main" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-agent-conf.xml";
    }

    private static void publishLogEvents(DataPublisher dataPublisher, String streamId) throws FileNotFoundException {
        while (true) {
            int i = 1;
            while (i <= 10) {
                Event event = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null, getPayloadData());
                dataPublisher.publish(event);
                i++;
            }
            System.out.println("Published 10 log events.");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }

    private static Object[] getPayloadData() {
        return new Object[]{
                getRandomConsumerKey(),
                getRandomContext(),
                getRandomAPIVersion(),
                getRandomAPI(),
                getRandomResourcePath(),
                getRandomMethod(),
                getRandomVersion(),
                getRandomRequest(),
                getRandomRequestTime(),
                getRandomUserID(),
                getRandomTenantDomain(),
                getRandomHostName(),
                getRandomAPIPublisher(),
                getRandomAppName(),
                getRandomAppID(),
                getRandomUserAgent(),
                getRandomTier()
        };
    }

    private static String getRandomConsumerKey() {
        return CONSUMER_KEY[getRandomId(4)];
    }

    private static String getRandomContext() {
        return CONTEXT[getRandomId(3)];
    }

    private static String getRandomAPIVersion() {
        return API_VERSION[getRandomId(3)];
    }

    private static String getRandomAPI() {
        return API[getRandomId(5)];
    }

    private static String getRandomResourcePath() {
        return RESOURCE_PATH[getRandomId(3)];
    }

    private static String getRandomMethod() {
        return METHOD[getRandomId(4)];
    }

    private static String getRandomVersion() {
        return VERSION[getRandomId(3)];
    }

    private static int getRandomRequest() {
        return REQUEST[getRandomId(5)];
    }

    private static long getRandomRequestTime() {
        return System.currentTimeMillis() - 1;
    }

    private static String getRandomUserID() {
        return USER_ID[getRandomId(3)];
    }

    private static String getRandomTenantDomain() {
        return TENANT_DOMAIN[getRandomId(3)];
    }

    private static String getRandomHostName() {
        return HOST_NAME[getRandomId(4)];
    }

    private static String getRandomAPIPublisher() {
        return API_PUBLISHER[getRandomId(5)];
    }

    private static String getRandomAppName() {
        return APP_NAME[getRandomId(5)];
    }

    private static String getRandomAppID() {
        return APP_ID[getRandomId(5)];
    }

    private static String getRandomUserAgent() {
        return USER_AGENT[getRandomId(5)];
    }

    private static String getRandomTier() {
        return TIER[getRandomId(4)];
    }

    private static int getRandomId(int i) {
        return RANDOM_GEN.nextInt(i);
    }

    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
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
        return InetAddress.getLocalHost();
    }


    private static String getProperty(String name, String def) {
        String result = System.getProperty(name);
        if (result == null || result.length() == 0 || result == "") {
            result = def;
        }
        return result;
    }

}