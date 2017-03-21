package org.wso2.eventsimulator.core.exception;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized InsufficientAttributesException to customized HTTP responses
 */

@Component(
        name = "org.wso2.eventsimulator.core.exception.InsufficientAttributesMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class InsufficientAttributesMapper implements ExceptionMapper<InsufficientAttributesException> {
    @Override
    public Response toResponse(InsufficientAttributesException e) {
        return Response.status(Response.Status.BAD_REQUEST).
                entity(e.getMessage()).
                build();
    }
}
