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
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;

import java.net.SocketException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Databridge Thrift Server which accepts Thrift/Binary events
 */
public class DatabridgeTestServer {
    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";
    private static final Logger log = Logger.getLogger(DatabridgeTestServer.class);
    private ThriftDataReceiver thriftDataReceiver;
    private InMemoryStreamDefinitionStore streamDefinitionStore;
    private AtomicInteger numberOfEventsReceived;
    private RestarterThread restarterThread;
    private static final String LOCAL_HOST = "localhost";
    private static final String STREAM_DEFN = "{" +
            "  'name':'" + STREAM_NAME + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'STRING'}," +
            "          {'name':'price','type':'DOUBLE'}," +
            "          {'name':'volume','type':'INT'}," +
            "          {'name':'max','type':'DOUBLE'}," +
            "          {'name':'min','type':'Double'}" +
            "  ]" +
            "}";

    public static void main(String args[]) throws DataBridgeException, InterruptedException,
            StreamDefinitionStoreException, MalformedStreamDefinitionException {
        DatabridgeTestServer databridgeTestServer = new DatabridgeTestServer();
        databridgeTestServer.addStreamDefinition(STREAM_DEFN);
        databridgeTestServer.start(7611);
        Thread.sleep(100000000);
        databridgeTestServer.stop();
    }

    public void addStreamDefinition(StreamDefinition streamDefinition)
            throws StreamDefinitionStoreException {
        streamDefinitionStore.saveStreamDefinitionToStore(streamDefinition);
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

    public void start(int receiverPort) throws DataBridgeException {
        WSO2EventServerUtil.setKeyStoreParams();
        streamDefinitionStore = getStreamDefinitionStore();
        numberOfEventsReceived = new AtomicInteger(0);
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
                numberOfEventsReceived.addAndGet(eventList.size());
                log.info("Received events : " + numberOfEventsReceived);
            }

        });

        String address = "localhost";
        log.info("Test Server starting on " + address);
        thriftDataReceiver.start(address);
        log.info("Test Server Started");
    }

    public int getNumberOfEventsReceived() {
        if (numberOfEventsReceived != null) {
            return numberOfEventsReceived.get();
        } else {
            return 0;
        }
    }

    public void resetReceivedEvents() {
        numberOfEventsReceived.set(0);
    }

    public void stop() {
        thriftDataReceiver.stop();
        log.info("Test Server Stopped");
    }

    public void stopAndStartDuration(int port, long stopAfterTimeMilliSeconds, long startAfterTimeMS)
            throws SocketException, DataBridgeException {
        restarterThread = new RestarterThread(port, stopAfterTimeMilliSeconds, startAfterTimeMS);
        Thread thread = new Thread(restarterThread);
        thread.start();
    }

    public int getEventsReceivedBeforeLastRestart() {
        return restarterThread.eventReceived;
    }


    class RestarterThread implements Runnable {
        int eventReceived;
        int port;

        long stopAfterTimeMilliSeconds;
        long startAfterTimeMS;

        RestarterThread(int port, long stopAfterTime, long startAfterTime) {
            this.port = port;
            stopAfterTimeMilliSeconds = stopAfterTime;
            startAfterTimeMS = startAfterTime;
        }

        public void run() {
            try {
                Thread.sleep(stopAfterTimeMilliSeconds);
            } catch (InterruptedException e) {
            }
            if (thriftDataReceiver != null) {
                thriftDataReceiver.stop();
            }

            eventReceived = getNumberOfEventsReceived();

            log.info("Number of events received in server shutdown :" + eventReceived);
            try {
                Thread.sleep(startAfterTimeMS);
            } catch (InterruptedException e) {
            }

            try {
                if (thriftDataReceiver != null) {
                    thriftDataReceiver.start(LOCAL_HOST);
                } else {
                    start(port);
                }
            } catch (DataBridgeException e) {
                log.error(e);
            }

        }
    }
}
