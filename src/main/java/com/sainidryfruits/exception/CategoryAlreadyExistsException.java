package com.sainidryfruits.exception;

/**
 * Exception thrown when trying to create a category that already exists
 */
public class CategoryAlreadyExistsException extends RuntimeException {
    
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
    
    public CategoryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
