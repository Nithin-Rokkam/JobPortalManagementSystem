package com.capg.jobportal.exception;

// Thrown when a seeker tries to apply to the same job twice
// Maps to HTTP 409 Conflict

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }

}
