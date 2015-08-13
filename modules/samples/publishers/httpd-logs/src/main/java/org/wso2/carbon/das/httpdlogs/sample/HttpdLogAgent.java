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

package org.wso2.carbon.das.httpdlogs.sample;

import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.lang.String;


public class HttpdLogAgent {
    private static final String HTTPD_LOG_STREAM = "org.wso2.sample.httpd.logs";
    private static final String VERSION = "1.0.0";
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
        System.out.println("Starting DAS HttpLog Agent");
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

        String streamId = DataBridgeCommonsUtils.generateStreamId(HTTPD_LOG_STREAM, VERSION);
        publishLogEvents(dataPublisher, streamId);
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
        Scanner scanner = new Scanner(new FileInputStream(SAMPLE_LOG_PATH));
        int i = 1;
        while (scanner.hasNextLine()) {
            System.out.println("Publish log event : " + i);
            String aLog = scanner.nextLine();
            String[] aLogElements = aLog.split("\\s");
            /*
             aLogElements[0] -> remoteIp
             aLogElements[3] -> request_date
             aLogElements[6] -> request
             aLogElements[8] -> httpcode
             aLogElements[9] -> length
             */
            Event event = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                    new Object[]{aLogElements[0], aLogElements[3], aLogElements[6], aLogElements[8], aLogElements[9]
                    });
            dataPublisher.publish(event);
            i++;
        }

        scanner.close();
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
