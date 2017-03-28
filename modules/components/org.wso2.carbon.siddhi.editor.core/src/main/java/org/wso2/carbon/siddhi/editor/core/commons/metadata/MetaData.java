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
package org.wso2.carbon.siddhi.editor.core.commons.metadata;

import java.util.LinkedList;
import java.util.List;

/**
 * For storing meta data for a extension namespace or in-built processors
 * Used in JSON responses
 */
public class MetaData {
    private List<ProcessorMetaData> functions;
    private List<ProcessorMetaData> streamProcessors;
    private List<ProcessorMetaData> windowProcessors;

    public MetaData() {
        functions = new LinkedList<>();
        streamProcessors = new LinkedList<>();
        windowProcessors = new LinkedList<>();
    }

    public List<ProcessorMetaData> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ProcessorMetaData> functions) {
        this.functions = functions;
    }

    public List<ProcessorMetaData> getStreamProcessors() {
        return streamProcessors;
    }

    public void setStreamProcessors(List<ProcessorMetaData> streamProcessors) {
        this.streamProcessors = streamProcessors;
    }

    public List<ProcessorMetaData> getWindowProcessors() {
        return windowProcessors;
    }

    public void setWindowProcessors(List<ProcessorMetaData> windowProcessors) {
        this.windowProcessors = windowProcessors;
    }
}
