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

import org.wso2.carbon.siddhi.editor.core.commons.metadata.MetaData;

import java.util.Map;

/**
 * Response wrapper for Meta Data
 */
public class MetaDataResponse extends GeneralResponse {
    private MetaData inBuilt;
    private Map<String, MetaData> extensions;

    public MetaDataResponse(Status status) {
        super(status, null);
    }

    public MetaData getInBuilt() {
        return inBuilt;
    }

    public void setInBuilt(MetaData inBuilt) {
        this.inBuilt = inBuilt;
    }

    public Map<String, MetaData> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, MetaData> extensions) {
        this.extensions = extensions;
    }
}
