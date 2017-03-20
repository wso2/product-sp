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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Temp Class for Stream Definition Retrieval
 */
public class StreamDefinitionRetriever {
    private static Logger logger = LoggerFactory.getLogger(StreamDefinitionRetriever.class);

    private static LinkedHashMap<String, Type> getStreamDefinition(String streamName) {
        logger.info("Stream definition retriever (streamDefinitionRetriever : Stream name : " + streamName);

        HashMap<String, LinkedHashMap<String, Type>> streamDefinitions =
                new HashMap<String, LinkedHashMap<String, Type>>();

        streamDefinitions.put("stream1", new LinkedHashMap<String, Type>() {
            {
                put("timestamp", Type.LONG);
                put("name", Type.STRING);
                put("price", Type.FLOAT);
            }
        });

        streamDefinitions.put("stream2", new LinkedHashMap<String, Type>() {
            {
                put("name", Type.STRING);
                put("price", Type.FLOAT);
                put("volume", Type.FLOAT);
            }
        });

        streamDefinitions.put("stream3", new LinkedHashMap<String, Type>() {
            {
                put("timestamp", Type.LONG);
                put("name", Type.STRING);
            }
        });

        streamDefinitions.put("stream4", new LinkedHashMap<String, Type>() {
            {
                put("name", Type.STRING);
            }
        });

        streamDefinitions.put("stream5", new LinkedHashMap<String, Type>() {
            {
                put("volume", Type.FLOAT);
            }
        });

        streamDefinitions.put("stream6", new LinkedHashMap<String, Type>() {
            {
                put("timestamp", Type.LONG);
            }
        });

        streamDefinitions.put("stream7", new LinkedHashMap<String, Type>() {
            {
                put("boolean", Type.BOOLEAN);
            }
        });

        streamDefinitions.put("stream8", new LinkedHashMap<String, Type>() {
            {
                put("price", Type.DOUBLE);
            }
        });

        streamDefinitions.put("stream9", new LinkedHashMap<String, Type>() {
            {
                put("count", Type.INTEGER);
            }
        });

        streamDefinitions.put("stream10", new LinkedHashMap<String, Type>() {
            {
                put("name", Type.STRING);
                put("timestamp", Type.LONG);
            }
        });

        if (streamDefinitions.containsKey(streamName)) {
            return streamDefinitions.get(streamName);
        } else {
            throw new IllegalArgumentException("Stream '" + streamName + "' does not exist");
        }
    }

    public static LinkedHashMap<String, Type> getStreamDefinitions(String streamName) {
        logger.info("Stream definition retriever (getStreamDefinition) : Stream name : " + streamName);

        return getStreamDefinition(streamName);
    }

    /**
     * Stream Attribute Types
     */
    public enum Type {
        INTEGER, LONG, FLOAT, DOUBLE, STRING, BOOLEAN
    }
}
