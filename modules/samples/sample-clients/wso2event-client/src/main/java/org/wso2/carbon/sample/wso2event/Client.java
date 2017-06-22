/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sample.wso2event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.Arrays;

/**
 * WSO2Event Client Publisher
 */
public class Client {
    private static Log log = LogFactory.getLog(Client.class);
    private static final String STREAM_NAME = "FooStream";
    private static final String VERSION = "1.0.0";
    private static String agentConfigFileName = "sync.data.agent.config.yaml";
    private static final String LOCAL_HOST = "localhost";

    public static void main(String[] args) {

        DataPublisherUtil.setKeyStoreParams();
        DataPublisherUtil.setTrustStoreParams();

        log.info(Arrays.deepToString(args));
        try {
            log.info("Starting WSO2 Event Client");

            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath(agentConfigFileName));
            String hostName = LOCAL_HOST;
            DataPublisher dataPublisher = new DataPublisher("Thrift", "tcp://" + hostName + ":7611",
                    "ssl://" + hostName + ":7711", "admin", "admin");
            Event event = new Event();
            event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
            event.setMetaData(new Object[]{"127.0.0.1"});
            event.setCorrelationData(null);
            event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

            int numberOfEventsSent = 1000;
            for (int i = 0; i < numberOfEventsSent; i++) {
                dataPublisher.publish(event);
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

}
