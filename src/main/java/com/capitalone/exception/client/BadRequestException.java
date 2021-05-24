package com.capitalone.exception.client;

import javax.ws.rs.core.Response;

/**
 * For issues pertaining to an illegal request.
 * Uses HTTP Response status code 400 BAD_REQUEST
 */
public class BadRequestException extends PropagateMessageToClientException {
    public BadRequestException(final String message) {
        super(Response.Status.BAD_REQUEST, message);
    }

    public BadRequestException(final String message, final Throwable cause) {
        super(Response.Status.BAD_REQUEST, message, cause);
    }
}
