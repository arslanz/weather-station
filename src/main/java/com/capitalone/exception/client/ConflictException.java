package com.capitalone.exception.client;

import javax.ws.rs.core.Response;

/**
 * For issues pertaining to a measurement ID conflict.
 * Uses HTTP Response status code 409 CONFLICT
 */
public class ConflictException extends PropagateMessageToClientException {
    public ConflictException(final String message) {
        super(Response.Status.CONFLICT, message);
    }

    public ConflictException(final String message, final Throwable cause) {
        super(Response.Status.CONFLICT, message, cause);
    }
}
