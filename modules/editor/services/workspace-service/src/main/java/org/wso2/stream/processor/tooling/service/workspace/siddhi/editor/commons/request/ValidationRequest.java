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
package org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.request;

import java.util.List;

public class ValidationRequest {
    private String executionPlan;
    private List<String> missingStreams;
    private List<List<String>> missingInnerStreams;

    public String getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(String executionPlan) {
        this.executionPlan = executionPlan;
    }

    public List<String> getMissingStreams() {
        return missingStreams;
    }

    public void setMissingStreams(List<String> missingStreams) {
        this.missingStreams = missingStreams;
    }

    public List<List<String>> getMissingInnerStreams() {
        return missingInnerStreams;
    }

    public void setMissingInnerStreams(List<List<String>> missingInnerStreams) {
        this.missingInnerStreams = missingInnerStreams;
    }
}
