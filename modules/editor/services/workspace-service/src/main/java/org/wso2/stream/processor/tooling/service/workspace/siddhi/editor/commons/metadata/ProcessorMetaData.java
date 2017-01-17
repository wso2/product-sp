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
package org.wso2.stream.processor.tooling.service.workspace.siddhi.editor.commons.metadata;

import java.util.List;

/**
 * For storing Processor and ExpressionExecutor related meta data
 * Used in JSON responses
 */
public class ProcessorMetaData {
    private String name;
    private String description;
    private List<ParameterMetaData> parameters;
    private ReturnTypeMetaData returnType;
    private List<AttributeMetaData> returnEvent;
    private String example;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ParameterMetaData> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterMetaData> parameters) {
        this.parameters = parameters;
    }

    public ReturnTypeMetaData getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnTypeMetaData returnType) {
        this.returnType = returnType;
    }

    public List<AttributeMetaData> getReturnEvent() {
        return returnEvent;
    }

    public void setReturnEvent(List<AttributeMetaData> returnEvent) {
        this.returnEvent = returnEvent;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
