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

package org.wso2.carbon.siddhi.editor.core.internal;

import org.osgi.framework.BundleContext;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * EditorDataHolder referenced through org.wso2.carbon.siddhi.debugger.core.internal.ServiceComponent
 */
public class EditorDataHolder {
    private static EditorDataHolder instance = new EditorDataHolder();
    private static SiddhiManager siddhiManager;
    private static BundleContext bundleContext;
    private static DebugProcessorService debugProcessorService;

    private EditorDataHolder() {

    }

    public static EditorDataHolder getInstance() {
        return instance;
    }

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void setSiddhiManager(SiddhiManager siddhiManager) {
        EditorDataHolder.siddhiManager = siddhiManager;
    }

    public static DebugProcessorService getDebugProcessorService() {
        return debugProcessorService;
    }

    public static void setDebugProcessorService(DebugProcessorService debugProcessorService) {
        EditorDataHolder.debugProcessorService = debugProcessorService;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        EditorDataHolder.bundleContext = bundleContext;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }
}
