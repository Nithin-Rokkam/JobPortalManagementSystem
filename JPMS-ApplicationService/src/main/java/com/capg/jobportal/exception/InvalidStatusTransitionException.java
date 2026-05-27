package com.capg.jobportal.exception;

// Thrown when recruiter tries to move status in a wrong direction
// Example: trying to change REJECTED back to SHORTLISTED
// Maps to HTTP 400 Bad Request

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

}
