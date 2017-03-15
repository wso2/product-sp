package org.wso2.carbon.das.jobmanager.core;

import org.wso2.carbon.das.jobmanager.core.factories.WorkersApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.das.jobmanager.core.dto.ExecutionPlanDTO;
import org.wso2.carbon.das.jobmanager.core.dto.ExecutionPlanListDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerDTO;
import org.wso2.carbon.das.jobmanager.core.dto.WorkerListDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "WorkersApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/das/jobmanager/v1/workers")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the workers API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-14T10:36:41.439+05:30")
public class WorkersApi implements Microservice  {
   private final WorkersApiService delegate = WorkersApiServiceFactory.getWorkersApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieving Workers ", notes = "Get a list of registered workers. ", response = WorkerListDTO.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of registered workers is returned. ", response = WorkerListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = WorkerListDTO.class) })
    public Response workersGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
)
    throws NotFoundException {
        return delegate.workersGet(accept);
    }
    @DELETE
    @Path("/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Deregister a Worker ", response = void.class, tags={ "Delete", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = void.class) })
    public Response workersIdDelete(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ",required=true) @PathParam("id") String id
)
    throws NotFoundException {
        return delegate.workersIdDelete(id);
    }
    @GET
    @Path("/{id}/executionplans")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of execution plans deployed in a Worker. ", response = ExecutionPlanListDTO.class, tags={ "Retrieve Execution Plans", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Execution Plan list is returned. ", response = ExecutionPlanListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Worker does not exist. ", response = ExecutionPlanListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ExecutionPlanListDTO.class) })
    public Response workersIdExecutionplansGet(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ",required=true) @PathParam("id") String id
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
)
    throws NotFoundException {
        return delegate.workersIdExecutionplansGet(id,accept);
    }
    @POST
    @Path("/{id}/executionplans")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add a new execution plan to a Worker. ", response = ExecutionPlanDTO.class, tags={ "Register", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Registered. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = ExecutionPlanDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ExecutionPlanDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict. Worker already exists. ", response = ExecutionPlanDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = ExecutionPlanDTO.class) })
    public Response workersIdExecutionplansPost(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ",required=true) @PathParam("id") String id
,@ApiParam(value = "Execution Plan object that is to be created. " ,required=true) ExecutionPlanDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
)
    throws NotFoundException {
        return delegate.workersIdExecutionplansPost(id,body,contentType);
    }
    @GET
    @Path("/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get details of a Worker ", response = WorkerDTO.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested Worker is returned ", response = WorkerDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Worker does not exist. ", response = WorkerDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = WorkerDTO.class) })
    public Response workersIdGet(@ApiParam(value = "**ID** consisting of the **UUID** of the Worker. ",required=true) @PathParam("id") String id
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
)
    throws NotFoundException {
        return delegate.workersIdGet(id,accept);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Register a new Worker. ", response = WorkerDTO.class, tags={ "Register", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Registered. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = WorkerDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = WorkerDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict. Worker already exists. ", response = WorkerDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = WorkerDTO.class) })
    public Response workersPost(@ApiParam(value = "Worker object that is to be created. " ,required=true) WorkerDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
)
    throws NotFoundException {
        return delegate.workersPost(body,contentType);
    }
}
