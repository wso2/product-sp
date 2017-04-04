package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized SimulatorInitializationException to customized HTTP responses
 */
@Component(
        name = "SimulatorInitializationMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class SimulatorInitializationMapper implements ExceptionMapper<SimulatorInitializationException> {

    @Override
    public Response toResponse(SimulatorInitializationException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(e.getMessage()).
                build();
    }
}

