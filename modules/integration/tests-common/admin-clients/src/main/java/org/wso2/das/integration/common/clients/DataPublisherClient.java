/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.das.integration.common.clients;

import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.File;
import java.util.List;

public class DataPublisherClient {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String URL = "tcp://localhost:8311";
    private DataPublisher dataPublisher;

    public DataPublisherClient(String url) throws Exception {
        String resourceDir = new File(this.getClass().getClassLoader().getResource("datapublisher").toURI()).getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", resourceDir + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceDir + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
        AgentHolder.setConfigPath(resourceDir + File.separator + "data-agent-config.xml");
        this.dataPublisher = new DataPublisher(url, USERNAME, PASSWORD);
    }

    public DataPublisherClient() throws Exception {
        String resourceDir = new File(this.getClass().getClassLoader().getResource("datapublisher").toURI()).getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", resourceDir + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceDir + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
        AgentHolder.setConfigPath(resourceDir + File.separator + "data-agent-config.xml");
        this.dataPublisher = new DataPublisher(URL, USERNAME, PASSWORD);
    }

    public void shutdown() throws DataEndpointException {
        dataPublisher.shutdown();
    }

    public void publish(String streamName, String version, List<Event> events) throws DataEndpointException {
        String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
        for (Event event : events) {
            event.setStreamId(streamId);
            dataPublisher.publish(event);
        }
    }

    public void publish(String streamName, String version, Event event) throws DataEndpointException {
        String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, version);
        event.setStreamId(streamId);
        dataPublisher.publish(event);
    }
}
