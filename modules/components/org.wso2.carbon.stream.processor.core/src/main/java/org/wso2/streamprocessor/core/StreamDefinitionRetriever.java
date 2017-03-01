package org.wso2.streamprocessor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ruwini on 2/15/17.
 */
public class StreamDefinitionRetriever {
    private static Logger logger = LoggerFactory.getLogger(StreamDefinitionRetriever.class);
    public enum Type {INTEGER, LONG, FLOAT, DOUBLE, STRING, BOOLEAN }

//    public static StreamDefinitionRetriever getInstance(){return new StreamDefinitionRetriever();}

    private static LinkedHashMap<String,Type> StreamDefinitionRetriever(String streamName) {
        logger.info("Stream definition retriever (streamDefinitionRetriever : Stream name : " + streamName);

        HashMap<String,LinkedHashMap<String,Type>> streamDefinitions = new HashMap<String,LinkedHashMap<String,Type>>() ;

        streamDefinitions.put("stream1", new LinkedHashMap<String, Type>() {{
                put("timestamp", Type.LONG);
                put("name", Type.STRING);
                put("price", Type.FLOAT);
            }});

        streamDefinitions.put("stream2", new LinkedHashMap<String, Type>() {{
                put("name", Type.STRING);
                put("price", Type.FLOAT);
                put("volume", Type.FLOAT);
            }});

        streamDefinitions.put("stream3", new LinkedHashMap<String, Type>() {{
                put("timestamp", Type.LONG);
                put("name", Type.STRING);
            }});

        streamDefinitions.put("stream4", new LinkedHashMap<String, Type>() {{
            put("name", Type.STRING);
        }});

        streamDefinitions.put("stream5", new LinkedHashMap<String, Type>() {{
            put("volume", Type.FLOAT);
        }});

        streamDefinitions.put("stream6", new LinkedHashMap<String, Type>() {{
            put("timestamp", Type.LONG);
        }});

        streamDefinitions.put("stream7", new LinkedHashMap<String, Type>() {{
            put("boolean", Type.BOOLEAN);
        }});

        streamDefinitions.put("stream8", new LinkedHashMap<String, Type>() {{
            put("price", Type.DOUBLE);
        }});

        streamDefinitions.put("stream9", new LinkedHashMap<String, Type>() {{
            put("count", Type.INTEGER);
        }});

        if (streamDefinitions.containsKey(streamName)) {
            return streamDefinitions.get(streamName);
        } else {
           throw new IllegalArgumentException("Stream '" + streamName + "' does not exist");
        }
    }

    public static LinkedHashMap<String,Type> getStreamDefinitions(String streamName) {
        logger.info("Stream definition retriever (getStreamDefinition) : Stream name : " + streamName);

        return StreamDefinitionRetriever(streamName);
    }
}
