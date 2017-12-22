/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sample.server;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.core.utils.AgentSession;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.binary.internal.BinaryDataReceiver;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;

import java.io.IOException;
import java.util.List;

/**
 * Databridge Thrift Server which accepts Thrift/Binary events.
 */
public class DatabridgeTestServer {
    private static final String STREAM_NAME1 = "sweet.stream";
    private static final String VERSION = "1.0.0";
    private static final Logger log = Logger.getLogger(DatabridgeTestServer.class);
    private ThriftDataReceiver thriftDataReceiver;
    private BinaryDataReceiver binaryDataReceiver;
    private InMemoryStreamDefinitionStore streamDefinitionStore;
    private static final String STREAM_DEFN = "{" +
            "  'name':'" + STREAM_NAME1 + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'payloadData':[" +
            "          {'name':'name','type':'STRING'}," +
            "          {'name':'amount','type':'DOUBLE'}" +
            "  ]" +
            "}";
    public static void main(String args[]) throws DataBridgeException, InterruptedException,
            StreamDefinitionStoreException, MalformedStreamDefinitionException {
        DatabridgeTestServer databridgeTestServer = new DatabridgeTestServer();
        databridgeTestServer.addStreamDefinition(STREAM_DEFN);
        databridgeTestServer.start(args[0], Integer.parseInt(args[1]), args[2]);
        Thread.sleep(100000000);
        databridgeTestServer.stop();
    }


    public void addStreamDefinition(String streamDefinitionStr)
            throws StreamDefinitionStoreException, MalformedStreamDefinitionException {
        StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinitionStr);
        getStreamDefinitionStore().saveStreamDefinitionToStore(streamDefinition);
    }

    private InMemoryStreamDefinitionStore getStreamDefinitionStore() {
        if (streamDefinitionStore == null) {
            streamDefinitionStore = new InMemoryStreamDefinitionStore();
        }
        return streamDefinitionStore;
    }

    public void start(String host, int receiverPort, String protocol) throws DataBridgeException {
        WSO2EventServerUtil.setKeyStoreParams();
        streamDefinitionStore = getStreamDefinitionStore();
        DataBridge databridge = new DataBridge(new AuthenticationHandler() {

            public boolean authenticate(String userName,
                                        String password) {
                return true; // allays authenticate to true

            }

            public void initContext(AgentSession agentSession) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void destroyContext(AgentSession agentSession) {

            }
        }, streamDefinitionStore, WSO2EventServerUtil.getDataBridgeConfigPath());

        thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

        databridge.subscribe(new AgentCallback() {

            public void definedStream(StreamDefinition streamDefinition) {
                log.info("StreamDefinition " + streamDefinition);
            }

            public void removeStream(StreamDefinition streamDefinition) {
                log.info("StreamDefinition remove " + streamDefinition);
            }

            public void receive(List<Event> eventList, Credentials credentials) {
                log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " +
                        credentials.getUsername());
            }

        });


        if (protocol.equalsIgnoreCase("binary")) {
            binaryDataReceiver = new BinaryDataReceiver(new BinaryDataReceiverConfiguration(receiverPort + 100,
                    receiverPort), databridge);
            try {
                binaryDataReceiver.start();
            } catch (IOException e) {
                log.error("Error occurred when reading the file : " + e.getMessage(), e);
            }
        } else {
            thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);
            thriftDataReceiver.start(host);
        }

        log.info("Test Server Started");
    }

    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
    }


}
