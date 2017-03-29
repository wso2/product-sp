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

package org.wso2.das.tcp.server;

import org.apache.log4j.Logger;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.tcp.transport.TCPNettyServer;
import org.wso2.siddhi.tcp.transport.callback.LogStreamListener;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;

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
        StreamDefinition streamDefinition = StreamDefinition.id("UsageStream")
                .attribute("houseId", Attribute.Type.INT).attribute("maxVal", Attribute.Type.FLOAT)
                .attribute("minVal", Attribute.Type.FLOAT).attribute("avgVal", Attribute.Type.DOUBLE);

        TCPNettyServer tcpNettyServer = new TCPNettyServer();
        tcpNettyServer.addStreamListener(new LogStreamListener(streamDefinition));
//        tcpNettyServer.addStreamListener(new StatisticsStreamListener(streamDefinition));

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setHost(args[0]);
        serverConfig.setPort(Integer.parseInt(args[1]));

        tcpNettyServer.bootServer(serverConfig);
        try {
            log.info("Server started, it will shutdown in 100000 millis.");
            Thread.sleep(100000);
        } catch (InterruptedException e) {
        } finally {
            tcpNettyServer.shutdownGracefully();
        }
    }
}
