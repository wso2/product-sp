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
package org.wso2.carbon.siddhi.editor.core.commons.response;

import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.List;

/**
 * Response wrapper for Validation Success
 */
public class ValidationSuccessResponse extends GeneralResponse {
    private List<AbstractDefinition> streams;
    private List<List<AbstractDefinition>> innerStreams;

    public ValidationSuccessResponse(Status status) {
        super(status, null);
    }

    public List<AbstractDefinition> getStreams() {
        return streams;
    }

    public void setStreams(List<AbstractDefinition> streams) {
        this.streams = streams;
    }

    public List<List<AbstractDefinition>> getInnerStreams() {
        return innerStreams;
    }

    public void setInnerStreams(List<List<AbstractDefinition>> innerStreams) {
        this.innerStreams = innerStreams;
    }
}
