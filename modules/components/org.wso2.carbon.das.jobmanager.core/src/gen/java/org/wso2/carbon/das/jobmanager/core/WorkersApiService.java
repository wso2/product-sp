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
package org.wso2.carbon.das.jobmanager.core;

import org.wso2.carbon.das.jobmanager.core.dto.ExecutionPlanDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-03-14T10:36:41.439+05:30")
public abstract class WorkersApiService {
    public abstract Response workersGet(String accept
    ) throws NotFoundException;

    public abstract Response workersIdDelete(String id
    ) throws NotFoundException;

    public abstract Response workersIdExecutionplansGet(String id
            , String accept
    ) throws NotFoundException;

    public abstract Response workersIdExecutionplansPost(String id
            , ExecutionPlanDTO body
            , String contentType
    ) throws NotFoundException;

    public abstract Response workersIdGet(String id
            , String accept
    ) throws NotFoundException;

    public abstract Response workersPost(WorkerDTO body
            , String contentType
    ) throws NotFoundException;
}
