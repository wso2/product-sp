package org.wso2.eventsimulator.core.eventGenerator.util.exceptions;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized FileAlreadyExistsException to customized HTTP responses
 */

@Component(
        name = "org.wso2.eventsimulator.core.eventGenerator.util.exceptions.FileAlreadyExistsMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class FileAlreadyExistsMapper implements ExceptionMapper<FileAlreadyExistsException> {

    @Override
    public Response toResponse(FileAlreadyExistsException e) {
        return Response.status(Response.Status.CONFLICT).
                entity(e.getMessage()).
                build();
    }
}
