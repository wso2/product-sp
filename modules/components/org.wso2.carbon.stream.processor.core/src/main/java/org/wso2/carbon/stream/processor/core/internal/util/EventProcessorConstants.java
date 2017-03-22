/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.stream.processor.core.internal.util;


/**
 * Class which contains public constants
 */
public class EventProcessorConstants {

    private EventProcessorConstants() {
        // Prevents instantiation.
    }

    public static final String EP_CONF_NS = "http://wso2.org/carbon/eventprocessor";
    public static final String EP_PREFIX = "ep";
    public static final String EP_ELE_ROOT_ELEMENT = "executionPlan";
    public static final String EP_ELE_DESC = "description";
    public static final String EP_ELE_SIDDHI_CONFIG = "siddhiConfiguration";
    public static final String EP_ELE_IMP_STREAMS = "importedStreams";
    public static final String EP_ELE_EXP_STREAMS = "exportedStreams";
    public static final String EP_ELE_QUERIES = "queryExpressions";
    public static final String EP_ELE_STREAM = "stream";
    public static final String EP_ELE_PROPERTY = "property";
    public static final String EP_ATTR_STATISTICS = "statistics";
    public static final String EP_ATTR_TRACING = "trace";
    public static final String EP_ENABLE = "enable";
    public static final String EP_DISABLE = "disable";
    public static final String EP_ATTR_PASSTHROUGH_FLOW = "passthroughFlow";
    // For inputs - siddhi stream.
    public static final String EP_ATTR_AS = "as";
    // For outputs - siddhi stream.
    public static final String EP_ATTR_VALUEOF = "valueOf";
    public static final String EP_ATTR_NAME = "name";
    public static final String EP_ATTR_VERSION = "version";

    public static final String STREAM_SEPARATOR = ":";
    public static final String ATTRIBUTE_SEPARATOR = "_";

    public static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";

    public static final String EP_ELE_DIRECTORY = "executionplans";
    public static final String XML_EXTENSION = ".xml";
    public static final String SIDDHIQL_EXTENSION = ".siddhiql";

    public static final String EVENT_PROCESSOR = "Event Processor";
    public static final String EVENT_STREAM = "Event Stream";

    public static final String SIDDHI_DISTRIBUTED_PROCESSING = "siddhi.enable.distributed.processing";
    public static final String SIDDHI_SNAPSHOT_INTERVAL = "siddhi.persistence.snapshot.time.interval.minutes";
    public static final String EP_CONFIG_FILE_EXTENSION_WITH_DOT = ".siddhiql";

    public static final String META = "meta";
    public static final String CORRELATION = "correlation";
    public static final String META_PREFIX = "meta_";
    public static final String CORRELATION_PREFIX = "correlation_";

    public static final String HAZELCAST_INSTANCE = "hazelcast.instance";
    public static final String NO_DEPENDENCY_INFO_MSG = "No dependency information available for this event formatter";

    // For storm query plan builder.
    public static final String OPENING_BRACKETS = " ( ";
    public static final String SPACE = " ";
    public static final String COMMA = ", ";
    public static final String SEMICOLEN = ";";
    public static final String CLOSING_BRACKETS = ")" + SEMICOLEN;
    public static final String STORM_QUERY_PLAN = "storm-query-plan";
    public static final String INPUT_STREAMS = "input-streams";
    public static final String TABLE_DEFINITIONS = "table-definitions";
    public static final String OUTPUT_STREAMS = "output-streams";
    public static final String OUTPUT_STREAM = "output-stream";
    public static final String EVENT_PROCESSOR_TAG = "event-processor";
    public static final String TRIGGER_TAG = "trigger";
    public static final String SIDDHI_BOLT = "SiddhiBolt";
    public static final String QUERIES = "queries";
    public static final String EVENT_RECEIVER = "event-receiver";
    public static final String EVENT_RECEIVER_SPOUT = "EventReceiverSpout";
    public static final String STREAMS = "streams";
    public static final String STREAM = "stream";
    public static final String DEFINE_STREAM = "define stream ";
    public static final String DEFINE_TRIGGER = "define trigger ";
    public static final String EVENT_PUBLISHER = "event-publisher";
    public static final String TRIGGER_DEFINITION = "trigger-definition";
    public static final String EVENT_PUBLISHER_BOLT = "EventPublisherBolt";
    public static final String TRIGGER_SPOUT = "TriggerSpout";
    public static final String PARALLEL = "parallel";
    public static final String RECEIVER_PARALLELISM = "receiverParallelism";
    public static final String PUBLISHER_PARALLELISM = "publisherParallelism";
    public static final String NAME = "name";
    public static final String PARTITION = "partition";
    public static final String DIST = "dist";
    public static final String EXEC_GROUP = "execGroup";
    public static final String ENFORCE_PARALLELISM = "enforceParallel";
    public static final String TRIGGER_AT_EVERY = " at every ";
    public static final String TRIGGER_AT = " at ";
    public static final String SECOND = " sec";

    // Annotations, Annotation Names and relevant tokens.
    public static final String ANNOTATION_PLAN = "Plan";
    public static final String ANNOTATION_IMPORT = "Import";
    public static final String ANNOTATION_EXPORT = "Export";

    public static final String ANNOTATION_NAME_NAME = "name";
    public static final String ANNOTATION_NAME_DESCRIPTION = "description";
    public static final String ANNOTATION_NAME_TRACE = "trace";
    public static final String ANNOTATION_NAME_STATISTICS = "statistics";
    public static final String ANNOTATION_INCLUDE_ARBITRARY = "arbitrary.data";

    public static final String ANNOTATION_TOKEN_AT = "@";
    public static final String ANNOTATION_TOKEN_COLON = ":";
    public static final String ANNOTATION_TOKEN_OPENING_BRACKET = "(";
    public static final String ANNOTATION_TOKEN_CLOSING_BRACKET = ")";

    public static final String DATABRIDGE_STREAM_REGEX = "[a-zA-Z0-9_\\-\\.]+";
    public static final String STREAM_VER_REGEX = "([0-9]*)\\.([0-9]*)\\.([0-9]*)";
    public static final String ARBITRARY_MAP = "arbitraryDataMap";

    // "Execution plan header" is the part above the Import/Export statements.

    // Following regex represents a line in an execution plan header.
    public static final String PLAN_HEADER_LINE_REGEX = "(^\\s*" + ANNOTATION_TOKEN_AT + ANNOTATION_PLAN +
            ANNOTATION_TOKEN_COLON + ".*)|(^\\s*--.*)|(^\\s*\\/\\*.*\\*\\/\\s*)|(^\\s*)";

    public static final String END_OF_PLAN_HEADER_COMMENT_REGEX = "^\\s*\\/\\* define streams and write query here" +
                                                                  " ... \\*\\/\\s*";

    public static final String SIDDHI_LINE_SEPARATER = "\n";
    public static final String SIDDHI_SINGLE_QUOTE = "'";

    public static final String METRIC_PREFIX = "WSO2_CEP";
    public static final String METRIC_INFIX_EXECUTION_PLANS = "ExecutionPlans";
    public static final String METRIC_INFIX_STREAMS = "Streams";
    public static final String METRIC_NAME_OUTPUT_EVENTS = "OutputEvents";
    public static final String METRIC_NAME_INPUT_EVENTS = "InputEvents";
    public static final String METRIC_AGGREGATE_ANNOTATION = "[+]";
    public static final String METRIC_DELIMITER = ".";
    public static final String TEMP_CARBON_APPS_DIRECTORY = "carbonapps";
}
