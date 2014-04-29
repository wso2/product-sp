package org.wso2.carbon.bam.httpdlogs.sample;

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Scanner;
import java.lang.String;

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
public class HttpdLogAgent {
    private static final String HTTPD_LOG_STREAM = "org.wso2.sample.httpd.logs";
    private static final String VERSION = "1.0.0";
    private static final int MAX_LOGS = 1000;
    private static final String SAMPLE_LOG_PATH = System.getProperty("user.dir") + "/resources/access.log";

    public static void main(String[] args) throws SocketException, AgentException, MalformedURLException, AuthenticationException, TransportException, StreamDefinitionException, MalformedStreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, FileNotFoundException, UnknownHostException {
        System.out.println("Starting BAM HttpLog Agent");
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        Agent agent = new Agent(agentConfiguration);
        String host;

        if (getLocalAddress() != null) {
           host = getLocalAddress().getHostAddress();
        } else {
           host = "localhost"; // Defaults to localhost
        }

        String url = getProperty("url", "tcp://" + host + ":" + "7611");
        String username = getProperty("username", "admin");
        String password = getProperty("password", "admin");

        //create data publisher

        DataPublisher dataPublisher = new DataPublisher(url, username, password, agent);
        String streamId = null;

        try {
            streamId = dataPublisher.findStream(HTTPD_LOG_STREAM, VERSION);
            System.out.println("Stream already defined");

        } catch (NoStreamDefinitionExistException e) {
            streamId = dataPublisher.defineStream("{" +
                    "  'name':'" + HTTPD_LOG_STREAM + "'," +
                    "  'version':'" + VERSION + "'," +
                    "  'nickName': 'Httpd_Log_Stream'," +
                    "  'description': 'Sample of Httpd logs'," +
                    "  'metaData':[" +
                    "          {'name':'clientType','type':'STRING'}" +
                    "  ]," +
                    "  'payloadData':[" +
                    "          {'name':'log','type':'STRING'}" +
                    "  ]" +
                    "}");
//            //Define event stream
        }
        if (null != streamId && !streamId.isEmpty()) {
              publishLogEvents(dataPublisher, streamId);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        dataPublisher.stop();
    }

    private static void publishLogEvents(DataPublisher dataPublisher, String streamId) throws FileNotFoundException, AgentException {
        Scanner scanner = new Scanner(new FileInputStream(SAMPLE_LOG_PATH));
        try {
            int i=1;
            while (scanner.hasNextLine()) {
                System.out.println("Publish log event : "+ i);
                String aLog = scanner.nextLine();
                Event event = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                        new Object[]{aLog});
                dataPublisher.publish(event);
                i++;
            }
        } catch (AgentException e) {
            e.printStackTrace();
            throw e;
        } finally {
            scanner.close();
        }
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
