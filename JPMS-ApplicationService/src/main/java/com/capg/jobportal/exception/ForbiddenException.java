package com.capg.jobportal.exception;

// Thrown when a user tries to do something they are not allowed to do
// Example: recruiter tries to update status of a job they don't own
// Maps to HTTP 403 Forbidden

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

}
