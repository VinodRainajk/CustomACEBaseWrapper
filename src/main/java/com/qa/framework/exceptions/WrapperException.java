package com.qa.framework.exceptions;

/**
 * Custom exception class for wrapper-related errors.
 */
public class WrapperException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public WrapperException(String message) {
        super(message);
    }
    
    public WrapperException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WrapperException(Throwable cause) {
        super(cause);
    }
}
