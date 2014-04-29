/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.rt.traffic.sample;

import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.lang.String;

/**
 * Realtime traffic sample agent.
 */
public class RTTrafficAgent {

    private static final String RT_TRAFFIC_STREAM = "org.wso2.sample.rt.traffic";

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) throws Exception {
        System.out.println("Starting BAM Realtime Traffic Agent");
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String currentDir = System.getProperty("user.dir");
        System.setProperty("javax.net.ssl.trustStore", currentDir
                + "/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        Agent agent = new Agent(agentConfiguration);
        String host;

        if (getLocalAddress() != null) {
            host = getLocalAddress().getHostAddress();
        } else {
            host = "localhost";
        }

        String url = getProperty("url", "tcp://" + host + ":" + "7611");
        String username = getProperty("username", "admin");
        String password = getProperty("password", "admin");

        DataPublisher dataPublisher = new DataPublisher(url, username, password, agent);
        String streamId = null;
        streamId = dataPublisher.findStreamId(RT_TRAFFIC_STREAM, VERSION);
        if (streamId == null) {
            streamId = dataPublisher.defineStream("{" +
                    "  'name':'" + RT_TRAFFIC_STREAM + "'," +
                    "  'version':'" + VERSION + "'," +
                    "  'nickName':" + RT_TRAFFIC_STREAM + "," +
                    "  'description': 'Stream to hold realtime traffic information'," +
                    "  'payloadData':[" +
                    "          {'name':'entry','type':'STRING'}" +
                    "  ]" +
                    "}");
        } else {
            System.out.println("Stream already defined");
        }
        if (null != streamId && !streamId.isEmpty()) {
            try {
                publishRTEvents(dataPublisher, streamId);
            } finally {
                dataPublisher.stop();
            }
        }
    }

    private static void publishRTEvents(DataPublisher dataPublisher, String streamId) throws Exception {
        try {
            System.out.println("Sending data...");
            Event event;
            while (true) {
                event = new Event(streamId, System.currentTimeMillis(),
                        null, null, new Object[] { "" + Math.random() });
                dataPublisher.publish(event);
                Thread.sleep(5 + (int) (Math.random() * 50));
            }
        } catch (AgentException e) {
            throw e;
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
