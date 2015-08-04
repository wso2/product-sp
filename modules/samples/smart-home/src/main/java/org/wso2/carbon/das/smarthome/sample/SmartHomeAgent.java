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
import java.util.UUID;


public class SmartHomeAgent {
    private static final String SMART_HOME_STREAM = "org.wso2.sample.smart.home.data";
    private static final String VERSION = "1.0.0";
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

        DataPublisher dataPublisher = new DataPublisher(type, url, authURL, username, password);

        String streamId = DataBridgeCommonsUtils.generateStreamId(SMART_HOME_STREAM, VERSION);
        publishLogEvents(dataPublisher, streamId);
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

    private static void publishLogEvents(DataPublisher dataPublisher, String streamId) throws FileNotFoundException, SocketException, UnknownHostException {
        String host;
        Random rand = new Random();
        int ctr = 0;

        if (getLocalAddress() != null) {
            host = getLocalAddress().getHostAddress();
        } else {
            host = "localhost"; // Defaults to localhost
        }
        for (int i = 0; i < 3000; i++) {
            Object[] payload = new Object[]{
                    UUID.randomUUID().toString(),
                    rand.nextFloat() * 500,
                    rand.nextBoolean(),
                    rand.nextInt(7),
                    rand.nextInt(21),
                    rand.nextInt(6)
            };
            Event event = new Event(streamId, System.currentTimeMillis(), new Object[]{host}, null, payload);
            dataPublisher.publish(event);
            ctr++;
        }
        System.out.println("Published " + ctr + " events.");

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
        if (result == null || result.length() == 0 || result.equals("")) {
            result = def;
        }
        return result;
    }

}
