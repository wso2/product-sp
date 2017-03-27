package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized EventGenerationException to customized HTTP responses
 */
@Component(
        name = "EventGenerationMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class EventGenerationMapper implements ExceptionMapper<EventGenerationException> {
    @Override
    public Response toResponse(EventGenerationException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(e.getMessage()).
                build();
    }
}
