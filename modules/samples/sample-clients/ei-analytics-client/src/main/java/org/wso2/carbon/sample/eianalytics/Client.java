/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.eianalytics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WSO2 Enterprise Integrator Analytics - Sample Event Client
 */
public class Client {
    private static final Log log = LogFactory.getLog(Client.class);
    private static final String FLOW_ENTRY_STREAM_NAME = "org.wso2.esb.analytics.stream.FlowEntry";
    private static final String CONFIG_ENTRY_STREAM_NAME = "org.wso2.esb.analytics.stream.ConfigEntry";
    private static final String VERSION = "1.0.0";
    private static final String agentConfigFileName = "sync.data.agent.config.yaml";

    public static void main(String[] args) {
        DataPublisherUtil.setKeyStoreParams();
        DataPublisherUtil.setTrustStoreParams();
        log.info("These are the provided configurations: " + Arrays.deepToString(args));
        String protocol = args[0];
        String host = args[1];
        String port = args[2];
        int sslPort = Integer.parseInt(port) + 100;
        String username = args[3];
        String password = args[4];
        int noOfRequests = Integer.parseInt(args[5]);
        int noOfBatches = noOfRequests / 3;

        try {
            log.info("Starting EI Analytics Event Client");
            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath(agentConfigFileName));
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port,
                    "ssl://" + host + ":" + sslPort, username, password);

            // Publish config events first.
            Event configEntryEvent = new Event();
            configEntryEvent.setStreamId(DataBridgeCommonsUtils.generateStreamId(CONFIG_ENTRY_STREAM_NAME, VERSION));
            for (Map eventData : loadConfigEventData()) {
                dataPublisher.publish(injectEventData(configEntryEvent, eventData));
            }

            // Then publish flow events.
            Event flowEntryEvent = new Event();
            flowEntryEvent.setStreamId(DataBridgeCommonsUtils.generateStreamId(FLOW_ENTRY_STREAM_NAME, VERSION));
            flowEntryEvent.setCorrelationData(null);
            for (int i = 0; i < noOfBatches; i++) {
                for (Map eventData : loadFlowEventData()) {
                    dataPublisher.publish(injectEventData(flowEntryEvent, eventData));
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e);
            }
            dataPublisher.shutdown();
            log.info("Events published successfully");
        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static Event injectEventData(Event event, Map eventData) {
        List metaList = (List) eventData.get("metaData");
        List payloadList = (List) eventData.get("eventData");
        event.setMetaData(metaList.toArray());
        event.setPayloadData(payloadList.toArray());
        event.setTimeStamp(System.currentTimeMillis());
        return event;
    }

    private static List<Map> loadEventData(String file) {
        TypeReference<HashMap<String, Object>> typeRef =
                new TypeReference<HashMap<String, Object>>() {
                };
        ObjectMapper mapper = new ObjectMapper();
        String filePath = DataPublisherUtil.getResourceFilePath(file);
        List<Map> results = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            for (String line; (line = br.readLine()) != null; ) {
                Map m = mapper.readValue(line, typeRef);
                results.add(m);
            }
        } catch (Throwable t) {
            log.error("Error occurred while reading " + file, t);
        }
        return results;
    }

    private static List<Map> loadFlowEventData() {
        return loadEventData("flowEvents.json");
    }

    private static List<Map> loadConfigEventData() {
        return loadEventData("configEvents.json");
    }
}
