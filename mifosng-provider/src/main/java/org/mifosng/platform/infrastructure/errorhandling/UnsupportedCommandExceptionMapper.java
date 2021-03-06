package org.mifosng.platform.infrastructure.errorhandling;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.platform.api.data.ApiGlobalErrorResponse;
import org.mifosng.platform.api.data.ApiParameterError;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link UnsupportedCommandException} thrown by platform into a HTTP API friendly format.
 */
@Provider
@Component
@Scope("singleton")
public class UnsupportedCommandExceptionMapper implements ExceptionMapper<UnsupportedCommandException> {

	@Override
	public Response toResponse(final UnsupportedCommandException exception) {

		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();	
		
		StringBuilder validationErrorCode = new StringBuilder("error.msg.command.unsupported");
		StringBuilder defaultEnglishMessage = new StringBuilder("The command ").append(exception.getUnsupportedCommandName()).append(" is not supported.");
		ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), exception.getUnsupportedCommandName(), exception.getUnsupportedCommandName());
		
		errors.add(error);	
		
		ApiGlobalErrorResponse invalidParameterError = ApiGlobalErrorResponse.badClientRequest(
				"validation.msg.validation.errors.exist",
				"Validation errors exist.", errors);
		
		return Response.status(Status.BAD_REQUEST).entity(invalidParameterError).type(MediaType.APPLICATION_JSON).build();
	}
}