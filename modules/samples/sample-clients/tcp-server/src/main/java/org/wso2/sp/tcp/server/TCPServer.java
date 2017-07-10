/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.sp.tcp.server;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.tcp.transport.TCPNettyServer;
import org.wso2.extension.siddhi.io.tcp.transport.callback.StreamListener;
import org.wso2.extension.siddhi.io.tcp.transport.config.ServerConfig;
import org.wso2.extension.siddhi.map.binary.sourcemapper.SiddhiEventConverter;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.nio.ByteBuffer;

/**
 * Test Server for TCP source
 */
public class TCPServer {
    static Logger log = Logger.getLogger(TCPServer.class);

    /**
     * Main method to start the test Server
     *
     * @param args host and port are passed as args
     */
    public static void main(String[] args) {
        /*
         * Stream definition:
         * OutStream (houseId int, maxVal float, minVal float, avgVal double);
         */
        final StreamDefinition streamDefinition = StreamDefinition.id("UsageStream")
                .attribute("houseId", Attribute.Type.INT)
                .attribute("maxVal", Attribute.Type.FLOAT)
                .attribute("minVal", Attribute.Type.FLOAT)
                .attribute("avgVal", Attribute.Type.DOUBLE);

        final Attribute.Type[] types = new Attribute.Type[]{Attribute.Type.INT,
                Attribute.Type.FLOAT,
                Attribute.Type.FLOAT,
                Attribute.Type.DOUBLE};
        TCPNettyServer tcpNettyServer = new TCPNettyServer();
//        tcpNettyServer.addStreamListener(new LogStreamListener("UsageStream"));
//        tcpNettyServer.addStreamListener(new StatisticsStreamListener(streamDefinition));
        tcpNettyServer.addStreamListener(new StreamListener() {

            public String getChannelId() {
                return streamDefinition.getId();
            }

            public void onMessage(byte[] message) {
                onEvents(SiddhiEventConverter.toConvertToSiddhiEvents(ByteBuffer.wrap(message), types));
            }

            public void onEvents(Event[] events) {
                for (Event event : events) {
                    onEvent(event);
                }
            }

            public void onEvent(Event event) {
                log.info(event);
            }
        });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setHost("localhost");
        serverConfig.setPort(Integer.parseInt("9893"));

        tcpNettyServer.start(serverConfig);
        try {
            log.info("Server started, it will shutdown in 100000 millis.");
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
        } finally {
            tcpNettyServer.shutdownGracefully();
        }
    }
}
