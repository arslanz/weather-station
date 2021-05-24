package com.capitalone.exception;

/**
 * For issues pertaining to operations which attempt to overwrite a measurement that already exists.
 */
public class EntityExistsException extends AppServerException {
    public EntityExistsException(final String message) {
        super(message);
    }

    public EntityExistsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
