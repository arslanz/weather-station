package com.capitalone.exception.client;

import com.capitalone.exception.AppServerException;
import lombok.Data;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The base class for exceptions that require their messages to be propagated to the client as a response.
 */
class PropagateMessageToClientException extends AppServerException {

    /**
     * Allows the exception message and response status to be propagated to the client as part of the HTTP response.
     * @param status the HTTP response status
     * @param message the message to pass to the client
     */
    PropagateMessageToClientException(final Response.Status status, final String message) {
        super(createResponse(status, message));
    }

    /**
     * Allows the exception message, response status and a throwable cayse to be propagated to the client as part
     * of the HTTP response.
     * @param status the HTTP response status
     * @param message the message to pass to the client
     * @param cause the throwable that caused this exception to be thrown
     */
    PropagateMessageToClientException(final Response.Status status, final String message, final Throwable cause) {
        super(createResponse(status, message), cause);
    }

    /**
     * Wraps the HTTP response status and string message into a Response object
     * @param status the HTTP response status
     * @param message the exception message
     * @return a Response object
     */
    private static Response createResponse(final Response.Status status, final String message) {
        return Response.status(status).entity(status.getStatusCode() + ": " +message).type(MediaType.TEXT_PLAIN).build();
    }
}
