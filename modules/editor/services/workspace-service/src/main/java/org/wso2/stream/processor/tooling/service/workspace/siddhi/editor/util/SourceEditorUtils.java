/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.util;

import org.apache.log4j.Logger;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata.AttributeMetaData;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata.MetaData;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata.ParameterMetaData;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata.ProcessorMetaData;
import org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata.ReturnTypeMetaData;
import org.wso2.siddhi.annotation.AdditionalAttribute;
import org.wso2.siddhi.annotation.Description;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.Parameters;
import org.wso2.siddhi.annotation.Return;
import org.wso2.siddhi.annotation.ReturnEvent;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Utility class for getting the meta data for the in built and extension processors in siddhi
 */
public class SourceEditorUtils {
    static final Logger log = Logger.getLogger(SourceEditorUtils.class);

    private SourceEditorUtils() {

    }

    /**
     * Validate the execution plan string using the Siddhi Manager
     * Will return a valid executionPlanRuntime
     *
     * @param executionPlan Execution plan string
     * @return Valid execution plan runtime
     */
    public static ExecutionPlanRuntime validateExecutionPlan(String executionPlan) {
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = null;
        try {
            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
            executionPlanRuntime.start();
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
        return executionPlanRuntime;
    }

    /**
     * Get the definition of the inner streams in the partitions
     * Inner streams will be separated based on the partition
     *
     * @param executionPlanRuntime Execution plan runtime created after validating
     * @param partitionsWithMissingInnerStreams Required inner stream names separated based on partition it belongs to
     * @return The inner stream definitions separated base on the partition it belongs to
     */
    public static List<List<AbstractDefinition>> getInnerStreamDefinitions(ExecutionPlanRuntime executionPlanRuntime,
                                                                           List<List<String>> partitionsWithMissingInnerStreams) {
        List<List<AbstractDefinition>> innerStreamDefinitions = new ArrayList<>();

        // Transforming the element ID to partition inner streams map to element ID no to partition inner streams map
        Map<Integer, Map<String, AbstractDefinition>> innerStreamsMap = new ConcurrentHashMap<>();
        executionPlanRuntime.getPartitionedInnerStreamDefinitionMap().entrySet().parallelStream().forEach(
                entry -> innerStreamsMap.put(
                        Integer.valueOf(entry.getKey().split("-")[1]),
                        entry.getValue()
                )
        );

        // Creating an ordered list of partition inner streams based on partition element ID
        // This is important since the client sends the missing inner streams 2D list with partitions in the order they are in the execution plan
        List<Map<String, AbstractDefinition>> rankedPartitionsWithInnerStreams = new ArrayList<>();
        List<Integer> rankedPartitionElementIds = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, AbstractDefinition>> entry :
                innerStreamsMap.entrySet()) {
            int i = 0;
            for (; i < rankedPartitionsWithInnerStreams.size(); i++) {
                if (entry.getKey() < rankedPartitionElementIds.get(i)) {
                    break;
                }
            }
            rankedPartitionsWithInnerStreams.add(i, entry.getValue());
            rankedPartitionElementIds.add(i, entry.getKey());
        }

        // Extracting the requested stream definitions from based on the order in rankedPartitionsWithInnerStreams and partitionsWithMissingInnerStreams
        // The inner stream definitions 2D list fetched from the Siddhi Manager and the missing inner streams 2D list are now in the same order
        // Therefore the outer loops in both lists can be looped together
        for (int i = 0; i < partitionsWithMissingInnerStreams.size(); i++) {
            List<String> partitionWithMissingInnerStreams = partitionsWithMissingInnerStreams.get(i);
            Map<String, AbstractDefinition> partitionWithInnerStreams = rankedPartitionsWithInnerStreams.get(i);
            List<AbstractDefinition> innerStreamDefinition = new ArrayList<>();

            for (String missingInnerStream : partitionWithMissingInnerStreams) {
                AbstractDefinition streamDefinition = partitionWithInnerStreams.get(missingInnerStream);
                if (streamDefinition != null) {
                    innerStreamDefinition.add(streamDefinition);
                }
            }
            innerStreamDefinitions.add(innerStreamDefinition);
        }

        return innerStreamDefinitions;
    }

    /**
     * Get the definitions of the streams that are requested
     * used for fetching the definitions of streams that queries output into without defining them first
     *
     * @param executionPlanRuntime Execution plan runtime created after validating
     * @param missingStreams Required stream names
     * @return The stream definitions
     */
    public static List<AbstractDefinition> getStreamDefinitions(ExecutionPlanRuntime executionPlanRuntime,
                                                                List<String> missingStreams) {
        List<AbstractDefinition> streamDefinitions = new ArrayList<>();
        Map<String, AbstractDefinition> streamDefinitionMap = executionPlanRuntime.getStreamDefinitionMap();
        for (String stream : missingStreams) {
            AbstractDefinition streamDefinition = streamDefinitionMap.get(stream);
            if (streamDefinition != null) {
                streamDefinitions.add(streamDefinition);
            }
        }
        return streamDefinitions;
    }

    /**
     * Returns the in built processor meta data
     * Scans for all classes in all jars in the classpath
     *
     * @return In-built processor meta data
     */
    public static MetaData getInBuiltProcessorMetaData() {
        Map<String, Set<Class<?>>> processorClassMap = getClassesInClassPathFromPackages();
        return generateInBuiltMetaData(processorClassMap);
    }

    /**
     * Returns the extension processor meta data
     * Gets the meta data from the siddhi manager
     *
     * @return Extension processor meta data
     */
    public static Map<String, MetaData> getExtensionProcessorMetaData() {
        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, Class> extensionsMap = siddhiManager.getExtensions();
        return generateExtensionsMetaData(extensionsMap);
    }

    /**
     * Returns processor types to Classes map with classes in the packages in processor type to package name map
     *
     * @return Processor types to Classes map
     */
    private static Map<String, Set<Class<?>>> getClassesInClassPathFromPackages() {
        String[] classPathNames = System.getProperty("java.class.path").split(File.pathSeparator);
        Map<String, Set<Class<?>>> classSetMap = new HashMap<>();
        // Looping the jars
        for (String classPathName : classPathNames) {
            if (classPathName.endsWith(".jar")) {
                JarInputStream stream = null;
                try {
                    stream = new JarInputStream(new FileInputStream(classPathName));
                    JarEntry jarEntry = stream.getNextJarEntry();
                    // Looping the classes in jar to get classes in the specified package
                    while (jarEntry != null) {
                        /*
                         * Path separator for linux and windows machines needs to be replaces separately
                         * The path separator in the jar entries depends on the machine where the jar was built
                         */
                        String jarEntryName = jarEntry.getName().replace("/", ".");
                        jarEntryName = jarEntryName.replace("\\", ".");

                        try {
                            // Looping the set of packages
                            for (Map.Entry<String, String> entry : Constants.PACKAGE_NAME_MAP.entrySet()) {
                                if (jarEntryName.endsWith(".class") && jarEntryName.startsWith(entry.getValue())) {
                                    Set<Class<?>> classSet = classSetMap.get(entry.getKey());
                                    if (classSet == null) {
                                        classSet = new HashSet<>();
                                        classSetMap.put(entry.getKey(), classSet);
                                    }
                                    classSet.add(Class.forName(jarEntryName.substring(0, jarEntryName.length() - 6)));
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            log.debug("Failed to load class " + jarEntryName.substring(0, jarEntryName.length() - 6), e);
                        }
                        jarEntry = stream.getNextJarEntry();
                    }
                } catch (IOException e) {
                    log.debug("Failed to open the jar input stream for " + classPathName, e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            log.debug("Failed to close the jar input stream for " + classPathName, e);
                        }
                    }
                }
            }
        }
        return classSetMap;
    }

    /**
     * Generate a MetaData object using the class map provided for inbuilt processors
     *
     * @param classMap processor types to class map
     */
    private static MetaData generateInBuiltMetaData(Map<String, Set<Class<?>>> classMap) {
        MetaData metaData = new MetaData();

        // Generating the function meta data list containing function executors and attribute aggregators
        List<ProcessorMetaData> functionMetaData = new ArrayList<>();
        populateInBuiltProcessorMetaDataList(functionMetaData, classMap, Constants.FUNCTION_EXECUTOR);
        populateInBuiltProcessorMetaDataList(functionMetaData, classMap, Constants.ATTRIBUTE_AGGREGATOR);
        metaData.setFunctions(functionMetaData);

        // Generating the stream processor meta data list containing stream processor and stream function
        List<ProcessorMetaData> streamProcessorMetaData = new ArrayList<>();
        populateInBuiltProcessorMetaDataList(streamProcessorMetaData, classMap, Constants.STREAM_FUNCTION_PROCESSOR);
        populateInBuiltProcessorMetaDataList(streamProcessorMetaData, classMap, Constants.STREAM_PROCESSOR);
        metaData.setStreamProcessors(streamProcessorMetaData);

        // Generating the window processor meta data list
        List<ProcessorMetaData> windowProcessorMetaData = new ArrayList<>();
        populateInBuiltProcessorMetaDataList(windowProcessorMetaData, classMap, Constants.WINDOW_PROCESSOR);
        metaData.setWindowProcessors(windowProcessorMetaData);

        return metaData;
    }

    /**
     * populate the targetProcessorMetaDataList with the annotated data in the classes in the class map for the specified processor type
     *
     * @param targetProcessorMetaDataList List of processor meta data objects to populate
     * @param classMap   processor types to set of class map from which the metadata should be extracted
     * @param processorType               The type of the processor of which meta data needs to be extracted
     */
    private static void populateInBuiltProcessorMetaDataList(List<ProcessorMetaData> targetProcessorMetaDataList,
                                                             Map<String, Set<Class<?>>> classMap,
                                                             String processorType) {
        Set<Class<?>> classSet = classMap.get(processorType);
        if (classSet != null) {
            for (Class<?> processorClass : classSet) {
                ProcessorMetaData processorMetaData = generateProcessorMetaData(processorClass, processorType);
                if (processorMetaData != null) {
                    targetProcessorMetaDataList.add(processorMetaData);
                }
            }
        }
    }

    /**
     * Generate a MetaData object map using the class map provided for extension processors.
     * The return map's key is the namespace and the meta data object contains the different types of processors
     *
     * @param extensionsMap   Map from which the meta data needs to be extracted
     */
    private static Map<String, MetaData> generateExtensionsMetaData(Map<String, Class> extensionsMap) {
        Map<String, MetaData> metaDataMap = new HashMap<>();
        for (Map.Entry<String, Class> entry : extensionsMap.entrySet()) {
            String[] extensionWithNamespace = entry.getKey().split(":");
            MetaData metaData = metaDataMap.get(extensionWithNamespace[0]);
            if (metaData == null) {
                metaData = new MetaData();
                metaDataMap.put(extensionWithNamespace[0], metaData);
            }

            Class<?> extensionClass = entry.getValue();
            String processorType = null;
            List<ProcessorMetaData> processorMetaDataList = null;
            if (Constants.SUPER_CLASS_MAP.get(Constants.FUNCTION_EXECUTOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.FUNCTION_EXECUTOR;
                processorMetaDataList = metaData.getFunctions();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.ATTRIBUTE_AGGREGATOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.ATTRIBUTE_AGGREGATOR;
                processorMetaDataList = metaData.getFunctions();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.STREAM_FUNCTION_PROCESSOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.STREAM_FUNCTION_PROCESSOR;
                processorMetaDataList = metaData.getStreamProcessors();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.STREAM_PROCESSOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.STREAM_PROCESSOR;
                processorMetaDataList = metaData.getStreamProcessors();
            } else if (Constants.SUPER_CLASS_MAP.get(Constants.WINDOW_PROCESSOR)
                    .isAssignableFrom(extensionClass)) {
                processorType = Constants.WINDOW_PROCESSOR;
                processorMetaDataList = metaData.getWindowProcessors();
            }

            if (processorMetaDataList != null) {
                ProcessorMetaData processorMetaData =
                        generateProcessorMetaData(extensionClass, processorType, extensionWithNamespace[1]);

                if (processorMetaData != null) {
                    processorMetaDataList.add(processorMetaData);
                }
            } else {
                log.warn("Discarded extension " + extensionClass.getCanonicalName() +
                        " belonging to an unknown type ");
            }
        }
        return metaDataMap;
    }

    /**
     * Generate processor meta data from the annotated data in the class
     * This generates a processor name using the class name
     *
     * @param processorClass Class from which meta data should be extracted from
     * @param processorType  The processor type of the class
     * @return processor meta data
     */
    private static ProcessorMetaData generateProcessorMetaData(Class<?> processorClass,
                                                               String processorType) {
        String processorName = processorClass.getName();
        processorName = processorName.substring(processorName.lastIndexOf('.') + 1);    // Getting the class name
        processorName = processorName.replace(processorType, "");                       // Removing the super class postfix

        // Check if the processor class is a subclass of the super class and not the superclass itself
        // This check is important because the inbuilt processor scan retrieves the super classes as well
        if (!Constants.SUPER_CLASS_MAP.get(processorType).equals(processorClass)) {
            processorName = processorName.substring(0, 1).toLowerCase() + processorName.substring(1);
            return generateProcessorMetaData(processorClass, processorType, processorName);
        } else {
            return null;
        }
    }

    /**
     * Generate processor meta data from the annotated data in the class
     *
     * @param processorClass Class from which meta data should be extracted from
     * @param processorType  The processor type of the class
     * @param processorName  The name of the processor
     * @return processor meta data
     */
    private static ProcessorMetaData generateProcessorMetaData(Class<?> processorClass, String processorType,
                                                               String processorName) {
        ProcessorMetaData processorMetaData = null;

        Description descriptionAnnotation = processorClass.getAnnotation(Description.class);
        Parameters parametersAnnotation = processorClass.getAnnotation(Parameters.class);   // When multiple parameters are present
        Parameter parameterAnnotation = processorClass.getAnnotation(Parameter.class);      // When only single parameter is present
        Return returnAnnotation = processorClass.getAnnotation(Return.class);
        ReturnEvent returnEventAnnotation = processorClass.getAnnotation(ReturnEvent.class);
        Example exampleAnnotation = processorClass.getAnnotation(Example.class);

        if (descriptionAnnotation != null || parametersAnnotation != null || parameterAnnotation != null ||
                returnAnnotation != null || exampleAnnotation != null || returnEventAnnotation != null) {
            processorMetaData = new ProcessorMetaData();
            processorMetaData.setName(processorName);

            // Adding Description annotation data
            if (descriptionAnnotation != null) {
                processorMetaData.setDescription(descriptionAnnotation.value());
            }

            // Adding Parameter annotation data
            if (parametersAnnotation != null) {
                // When multiple parameters are present
                List<ParameterMetaData> parameterMetaDataList = new ArrayList<>();
                for (Parameter parameter : parametersAnnotation.value()) {
                    ParameterMetaData parameterMetaData = new ParameterMetaData();
                    parameterMetaData.setName(parameter.name());
                    parameterMetaData.setType(Arrays.asList(parameter.type()));
                    parameterMetaData.setOptional(parameter.optional());
                    parameterMetaData.setDescription(parameter.description());
                    parameterMetaDataList.add(parameterMetaData);
                }
                processorMetaData.setParameters(parameterMetaDataList);
            } else if (parameterAnnotation != null) {
                // When only a single parameter is present
                ParameterMetaData parameterMetaData = new ParameterMetaData();
                parameterMetaData.setName(parameterAnnotation.name());
                parameterMetaData.setType(Arrays.asList(parameterAnnotation.type()));
                parameterMetaData.setOptional(parameterAnnotation.optional());

                List<ParameterMetaData> parameterMetaDataList = new ArrayList<>();
                parameterMetaDataList.add(parameterMetaData);
                processorMetaData.setParameters(parameterMetaDataList);
            }

            // Adding Return annotation data
            ReturnTypeMetaData returnTypeMetaData = null;
            if (Constants.STREAM_FUNCTION_PROCESSOR.equals(processorType) ||
                    Constants.STREAM_PROCESSOR.equals(processorType) ||
                    Constants.WINDOW_PROCESSOR.equals(processorType)) {
                /*
                 * Setting the return type to event for stream functions, stream processors and windows
                 * The return type does not refer to the additional attributes added by the stream processor
                 * (@ReturnEvent is used for indicating the additional attributes added by the stream processor)
                 */
                returnTypeMetaData = new ReturnTypeMetaData();
            } else if (returnAnnotation != null) {
                returnTypeMetaData = new ReturnTypeMetaData();
                returnTypeMetaData.setType(Arrays.asList(returnAnnotation.type()));
                returnTypeMetaData.setDescription(returnAnnotation.description());
            }
            if (returnTypeMetaData != null) {
                processorMetaData.setReturnType(returnTypeMetaData);
            }

            // Adding ReturnEvent annotation data
            // Adding return event additional attributes only if the processor type is stream processor
            if (Constants.WINDOW_PROCESSOR.equals(processorType) ||
                    Constants.STREAM_PROCESSOR.equals(processorType) ||
                    Constants.STREAM_FUNCTION_PROCESSOR.equals(processorType)) {
                List<AttributeMetaData> attributeMetaDataList = new ArrayList<>();
                if (returnEventAnnotation != null) {
                    for (AdditionalAttribute additionalAttribute : returnEventAnnotation.value()) {
                        AttributeMetaData attributeMetaData = new AttributeMetaData();
                        attributeMetaData.setName(additionalAttribute.name());
                        attributeMetaData.setType(Arrays.asList(additionalAttribute.type()));
                        attributeMetaData.setDescription(additionalAttribute.description());
                        attributeMetaDataList.add(attributeMetaData);
                    }
                }
                processorMetaData.setReturnEvent(attributeMetaDataList);
            }

            // Adding Example annotation data
            if (exampleAnnotation != null) {
                processorMetaData.setExample(exampleAnnotation.value());
            }
        }
        return processorMetaData;
    }
}
