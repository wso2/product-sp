package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized ValidationFailedException to customized HTTP responses
 */

@Component(
        name = "org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ValidationFailedMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class ValidationFailedMapper implements ExceptionMapper<ValidationFailedException> {

    @Override
    public Response toResponse(ValidationFailedException e) {
        return Response.status(Response.Status.BAD_REQUEST).
                entity(e.getMessage()).
                build();
    }
}
