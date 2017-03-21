package org.wso2.eventsimulator.core.exception;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized FileDeploymentException to customized HTTP responses
 */
@Component(
        name = "org.wso2.eventsimulator.core.exception.FileDeploymentMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class FileDeploymentMapper implements ExceptionMapper<FileDeploymentException> {
    @Override
    public Response toResponse(FileDeploymentException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(e.getMessage()).
                build();
    }
}
