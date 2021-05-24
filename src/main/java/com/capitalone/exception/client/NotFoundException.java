package com.capitalone.exception.client;

import javax.ws.rs.core.Response;

/**
 * For issues pertaining to a measurement not being found.
 * Uses HTTP Response status code 404 NOT_FOUND
 */

public class NotFoundException extends PropagateMessageToClientException {
    public NotFoundException(final String message) {
        super(Response.Status.NOT_FOUND, message);
    }

    public NotFoundException(final String message, final Throwable cause) {
        super(Response.Status.NOT_FOUND, message, cause);
    }
}
