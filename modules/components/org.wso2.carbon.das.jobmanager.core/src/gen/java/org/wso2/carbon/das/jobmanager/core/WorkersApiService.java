package org.wso2.carbon.das.jobmanager.core;

import org.wso2.carbon.das.jobmanager.core.dto.WorkerDTO;

import org.wso2.carbon.das.jobmanager.core.dto.ExecutionPlanDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-14T10:36:41.439+05:30")
public abstract class WorkersApiService {
    public abstract Response workersGet(String accept
 ) throws NotFoundException;
    public abstract Response workersIdDelete(String id
 ) throws NotFoundException;
    public abstract Response workersIdExecutionplansGet(String id
 ,String accept
 ) throws NotFoundException;
    public abstract Response workersIdExecutionplansPost(String id
 ,ExecutionPlanDTO body
 ,String contentType
 ) throws NotFoundException;
    public abstract Response workersIdGet(String id
 ,String accept
 ) throws NotFoundException;
    public abstract Response workersPost(WorkerDTO body
 ,String contentType
 ) throws NotFoundException;
}
