package org.wso2.carbon.analytics.jobmanager.restapi;

import org.wso2.carbon.analytics.jobmanager.restapi.*;
import org.wso2.carbon.analytics.jobmanager.restapi.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.analytics.jobmanager.restapi.dto.ErrorDTO;
import org.wso2.carbon.analytics.jobmanager.restapi.dto.ExecutionPlanDTO;
import org.wso2.carbon.analytics.jobmanager.restapi.dto.ExecutionPlanListDTO;
import org.wso2.carbon.analytics.jobmanager.restapi.dto.WorkerDTO;
import org.wso2.carbon.analytics.jobmanager.restapi.dto.WorkerListDTO;

import java.util.List;
import org.wso2.carbon.analytics.jobmanager.restapi.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

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
