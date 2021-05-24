package com.capitalone.exception;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * The base exception for all exceptions originating from within the server.
 * Specialised cases must extend this exception class.
 */
@Slf4j
public class AppServerException extends WebApplicationException {

    /**
     * This will not transmit the message back to client as part of the response.
     * Use this for internal servers that do not require propagation outside the application.
     * @param message the exception message
     */
    AppServerException(final String message) {
        super(message);
    }

    /**
     * This will not transmit the message back to client as part of the response.
     * Use this for internal servers that do not require propagation outside the application.
     * @param message the exception message
     * @param cause the Throwable that caused this exception to be thrown
     */
    AppServerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Allows a Response object to be passed to the super class, WebApplicationException
     * @param response the Response object to pass to the client
     */
    protected AppServerException(final Response response) {
        super(response);
        log(this.getClass().getName(), response.getEntity().toString());
    }

    /**
     * Allows a Response and Throwable object to be passed to the super class, WebApplicationException
     * @param response the Response object to pass to the client
     * @param cause the Throwable object to pass to the client
     */
    protected AppServerException(final Response response, final Throwable cause) {
        super(cause, response);
        log(this.getClass().getName(), response.getEntity().toString(), cause);
    }

    private void log(final String className, final String message, final Throwable cause) {
        log.error("{}: {}", className, message, cause);
    }

    private void log(final String className, final String message) {
        log.error("{}: {}", className, message);
    }
}
