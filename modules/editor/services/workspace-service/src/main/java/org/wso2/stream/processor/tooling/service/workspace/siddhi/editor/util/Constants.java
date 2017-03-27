package org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.util;

import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants used by the Siddhi Source Editor backend
 */
public class Constants {
    static final String FUNCTION_EXECUTOR = "FunctionExecutor";
    static final String ATTRIBUTE_AGGREGATOR = "AttributeAggregator";
    static final String WINDOW_PROCESSOR = "WindowProcessor";
    static final String STREAM_FUNCTION_PROCESSOR = "StreamFunctionProcessor";
    static final String STREAM_PROCESSOR = "StreamProcessor";
    static final Map<String, Class<?>> SUPER_CLASS_MAP;
    static final Map<String, String> PACKAGE_NAME_MAP;

    static {
        // Populating the processor super class map
        SUPER_CLASS_MAP = new HashMap<>();
        SUPER_CLASS_MAP.put(FUNCTION_EXECUTOR, FunctionExecutor.class);
        SUPER_CLASS_MAP.put(ATTRIBUTE_AGGREGATOR, AttributeAggregator.class);
        SUPER_CLASS_MAP.put(WINDOW_PROCESSOR, WindowProcessor.class);
        SUPER_CLASS_MAP.put(STREAM_FUNCTION_PROCESSOR, StreamFunctionProcessor.class);
        SUPER_CLASS_MAP.put(STREAM_PROCESSOR, StreamProcessor.class);

        // Populating the package name map
        PACKAGE_NAME_MAP = new HashMap<>();
        PACKAGE_NAME_MAP.put(Constants.FUNCTION_EXECUTOR, "org.wso2.siddhi.core.executor.function");
        PACKAGE_NAME_MAP.put(Constants.ATTRIBUTE_AGGREGATOR,
                "org.wso2.siddhi.core.query.selector.attribute.aggregator");
        PACKAGE_NAME_MAP.put(Constants.WINDOW_PROCESSOR, "org.wso2.siddhi.core.query.processor.stream.window");
    }
}
