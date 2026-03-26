package com.capg.jobportal.dto;

import java.time.LocalDateTime;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ErrorResponse
 * DESCRIPTION:
 * This DTO (Data Transfer Object) represents a standardized error
 * response returned by the application when an exception occurs.
 *
 * It encapsulates essential error details such as:
 * - HTTP status code
 * - Error type
 * - Error message
 * - Timestamp of the error
 *
 * PURPOSE:
 * Ensures consistent and structured error handling across all APIs,
 * making it easier for clients to understand and debug issues.
 * ================================================================
 */
public class ErrorResponse {
	
	private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
 
    
    public ErrorResponse() {
    	
    }
    
    
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public String getError() {
		return error;
	}


	public void setError(String error) {
		this.error = error;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public LocalDateTime getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
    
}