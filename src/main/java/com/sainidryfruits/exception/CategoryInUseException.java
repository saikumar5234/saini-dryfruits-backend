package com.sainidryfruits.exception;

/**
 * Exception thrown when trying to delete a category that is in use by products
 */
public class CategoryInUseException extends RuntimeException {
    
    public CategoryInUseException(String message) {
        super(message);
    }
    
    public CategoryInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}