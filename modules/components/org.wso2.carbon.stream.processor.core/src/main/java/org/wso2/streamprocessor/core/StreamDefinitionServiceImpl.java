package org.wso2.streamprocessor.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by ruwini on 2/15/17.
 */
public class StreamDefinitionServiceImpl implements StreamDefinitionService {
    private static Logger logger = LoggerFactory.getLogger(StreamDefinitionServiceImpl.class);

    public LinkedHashMap<String,StreamDefinitionRetriever.Type> streamDefinitionService(String streamName) {
        logger.info("Stream definition service : Stream name : " + streamName);
        return StreamDefinitionRetriever.getStreamDefinitions(streamName);
    }


}
