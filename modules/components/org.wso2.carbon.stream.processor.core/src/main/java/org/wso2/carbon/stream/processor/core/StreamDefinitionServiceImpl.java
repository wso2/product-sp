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

import java.util.LinkedHashMap;

/**
 * Class which provides stream definition details
 */
public class StreamDefinitionServiceImpl implements StreamDefinitionService {
    private static Logger log = LoggerFactory.getLogger(StreamDefinitionServiceImpl.class);

    public LinkedHashMap<String, StreamDefinitionRetriever.Type> streamDefinitionService(String streamName) {
        log.info("Stream definition service : Stream name : " + streamName);
        return StreamDefinitionRetriever.getStreamDefinitions(streamName);
    }


}
