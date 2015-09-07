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
package org.wso2.carbon.das.smarthome.sample;

import org.apache.log4j.PropertyConfigurator;
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


public class SmartHomeAgent {
    private static final String SMART_HOME_STREAM = "org.wso2.das.sample.smart.home.data";
    private static final String VERSION = "1.0.0";
    private static final int defaultThriftPort = 7611;
    private static final int defaultBinaryPort = 9611;
    private static final Random RAND = new Random();
    private static int count;

    private static final String[] CITIES = {"New York", "Los Angeles", "Chicago", "Dallas",
            "Miami", "Salt Lake City", "Seattle", "Phoenix", "San Francisco", "Indianapolis"};

    private static final String[] STATES = {"New York", "California", "Illinois", "Texas", "Florida", "Utah", "Washington", "Arizona", "California", "Indiana"};

    public static void main(String[] args) throws DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException,
            TransportException,
            DataEndpointException,
            DataEndpointConfigurationException,
            FileNotFoundException,
            SocketException,
            UnknownHostException {

        String log4jConfPath = "./src/main/resources/log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);

        System.out.println("Starting DAS Smart Home Agent");
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

        if (args[0] == null || args[0].isEmpty() || args[0].equals("count")) {
            count = 3000;
        } else {
            count = Integer.parseInt(args[0]);
        }

        DataPublisher dataPublisher = new DataPublisher(type, url, authURL, username, password);

        String streamId = DataBridgeCommonsUtils.generateStreamId(SMART_HOME_STREAM, VERSION);
        publishEvents(dataPublisher, streamId);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // do nothing
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

    private static void publishEvents(DataPublisher dataPublisher, String streamId) throws FileNotFoundException, SocketException, UnknownHostException {
        int ctr = 0;

        for (int i = 0; i < count; i++) {
            int idx = RAND.nextInt(10);
            Object[] payload = new Object[]{
                    RAND.nextInt(21) + 1,
                    CITIES[idx],
                    STATES[idx],
                    RAND.nextInt(7) + 1,
                    RAND.nextFloat() * (RAND.nextInt(10) + 1) * 100,
                    RAND.nextBoolean()
            };
            Event event = new Event(streamId, System.currentTimeMillis(), null, null, payload);
            dataPublisher.publish(event);
            ctr++;
        }
        System.out.println("Published " + ctr + " events.");

    }

    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
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
        if (result == null || result.length() == 0 || result.equals("")) {
            result = def;
        }
        return result;
    }

}