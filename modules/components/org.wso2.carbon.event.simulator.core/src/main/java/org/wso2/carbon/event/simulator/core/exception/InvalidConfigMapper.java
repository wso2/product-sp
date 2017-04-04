package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized InvalidConfigException to customized HTTP responses
 */
@Component(
        name = "InvalidConfigMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class InvalidConfigMapper implements ExceptionMapper<InvalidConfigException> {

    @Override
    public Response toResponse(InvalidConfigException e) {
        return Response.status(Response.Status.BAD_REQUEST).
                entity(e.getMessage()).
                build();
    }
}
