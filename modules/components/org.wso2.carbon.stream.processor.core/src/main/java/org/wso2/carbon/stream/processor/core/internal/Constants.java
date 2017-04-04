/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.stream.processor.core.internal;

/**
 * Constants related to Stream Processor runtime
 */
public class Constants {

    // Name of the System property which contains the siddhiql file path to be executed
    public static final String SYSTEM_PROP_RUN_FILE = "file";

    public static final String SYSTEM_PROP_RUN_MODE = "run-mode";

    public static final String SYSTEM_PROP_BASE_DIR = "base-dir";

    public static final String SYSTEM_PROP_RUN_MODE_SERVER = "server";
    public static final String SYSTEM_PROP_RUN_MODE_RUN = "run";

    // Name of the main function
    public static final String MAIN_FUNCTION_NAME = "main";

    // Intermediate headers added to the stream processor message
    public static final String INTERMEDIATE_HEADERS = "INTERMEDIATE_HEADERS";

    /**
     * Runtime modes of Stream Processor engine
     */
    public enum RuntimeMode {
        // Run File Mode.
        RUN_FILE,
        // Run Stream Processor Server Mode.
        SERVER,
        // Represents ERROR Condition.
        ERROR
    }

}
