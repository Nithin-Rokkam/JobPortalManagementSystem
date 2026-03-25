package com.capg.jobportal.exception;

public class UserAlreadyExistsException extends RuntimeException {
	
	public UserAlreadyExistsException(String message) {
        super(message);
    }
	
}
