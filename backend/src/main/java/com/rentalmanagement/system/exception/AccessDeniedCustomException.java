package com.rentalmanagement.system.exception;

/**
 * Thrown for business-rule access violations that aren't Spring Security
 * role failures - e.g. a tenant trying to act on a property they're not
 * assigned to. Kept distinct from Spring's AccessDeniedException so the
 * global handler can return a clear, consistent message either way.
 */
public class AccessDeniedCustomException extends RuntimeException {
    public AccessDeniedCustomException(String message) {
        super(message);
    }
}
