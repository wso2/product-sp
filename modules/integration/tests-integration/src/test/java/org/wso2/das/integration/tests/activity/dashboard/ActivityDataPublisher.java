/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.das.integration.tests.activity.dashboard;

import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

public class ActivityDataPublisher {
    private DataPublisher dataPublisher;

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public static final int EVENT_COUNT = 100;

    public ActivityDataPublisher(String url) throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
            TransportException, DataEndpointException, DataEndpointConfigurationException, URISyntaxException {
        setSystemProperties();
        this.dataPublisher = new DataPublisher(url, USERNAME, PASSWORD);
    }

    private void setSystemProperties() throws URISyntaxException {
        String resourceDir = new File(this.getClass().getClassLoader().getResource("datapublisher").toURI()).getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", resourceDir + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceDir + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
        AgentHolder.setConfigPath(resourceDir + File.separator + "data-agent-config.xml");

    }

    public ActivityDataPublisher(String receiverUrl, String type)
            throws URISyntaxException, DataEndpointAuthenticationException, DataEndpointAgentConfigurationException,
            TransportException, DataEndpointException, DataEndpointConfigurationException {
        setSystemProperties();
        this.dataPublisher = new DataPublisher(type, receiverUrl, null, USERNAME, PASSWORD);
    }


    public void publish(String streamName, String version, List<String> activityIds) throws DataEndpointException {
        String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
        for (int i = 0; i < EVENT_COUNT; i++) {
            Event event = new Event(streamId, System.currentTimeMillis(), getMetadata(), getCorrelationdata(activityIds),
                    getPayloadData());
            dataPublisher.publish(event);
        }
    }

    public void publish(String streamName, String version, List<String> activityIds, long delay) throws DataEndpointException, InterruptedException {
        String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
        for (int i = 0; i < EVENT_COUNT; i++) {
            Event event = new Event(streamId, System.currentTimeMillis(), getMetadata(), getCorrelationdata(activityIds),
                    getPayloadData());
            dataPublisher.publish(event);
            Thread.sleep(delay);
        }
    }

    public void shutdown() throws DataEndpointException {
        dataPublisher.shutdown();
    }

    private Object[] getMetadata() {
        return new Object[]{
                "UTF-8",
                "192.168.1.2:9764",
                "POST",
                "text/xml",
                "127.0.0.1",
                "localhost",
                "https://my:8244",
                123456,
                "/services/Simple_Stock_Quote_Service_Proxy"
        };
    }

    private Object[] getCorrelationdata(List<String> activityIds) {
        return new Object[] {
                "[" + activityIds.get(getRandomId(activityIds.size())) + "]"
        };
    }

    private static Object[] getPayloadData() {
        return new Object[]{
                "<soapenv:body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><m0:getfullquote xmlns:m0=\"http://services.samples\"><m0:request><m0:symbol>aa</m0:symbol></m0:request></m0:getfullquote></soapenv:body>",
                "<soapenv:header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><wsa:to>https://my:8244/services/Simple_Stock_Quote_Service_Proxy</wsa:to><wsa:messageid>urn:uuid:c70bae36-b163-4f3e-a341-d7079c58f1ba</wsa:messageid><wsa:action>urn:getFullQuote</wsa:action><ns:bamevent activityid=\"6cecbb16-6b89-46f3-bd2f-fd9f7ac447b6\" xmlns:ns=\"http://wso2.org/ns/2010/10/bam\"></ns:bamevent></soapenv:header>",
                "IN",
                "urn:uuid:c70bae36-b163-4f3e-a341-d7079c58f1ba",
                "mediate",
                "Simple_Stock_Quote_Service_Proxy",
                System.currentTimeMillis()
        };
    }


    private static int getRandomId(int i) {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(i);
    }
}
