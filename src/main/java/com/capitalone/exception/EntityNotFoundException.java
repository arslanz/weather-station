package com.capitalone.exception;

/**
 * For issues pertaining to operations which attempt to retrieve a measurement that cannot be found.
 */
public class EntityNotFoundException extends AppServerException {
    public EntityNotFoundException(final String message) {
        super(message);
    }

    public EntityNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
