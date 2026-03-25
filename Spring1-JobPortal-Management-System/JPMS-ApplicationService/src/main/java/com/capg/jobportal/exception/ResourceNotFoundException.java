package com.capg.jobportal.exception;

// Thrown when we cannot find something in the database
// Example: application with id 99 does not exist
// Maps to HTTP 404 Not Found

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
