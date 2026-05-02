package com.capg.jobportal.dto;

import java.time.LocalDateTime;
<<<<<<< HEAD

=======
import lombok.Getter;
import lombok.Setter;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ErrorResponse
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents a standardized error response returned
 * by the application when an exception occurs.
 *
 * It includes:
 * - HTTP status code
 * - Error type
 * - Detailed error message
 * - Timestamp of occurrence
 *
 * PURPOSE:
 * Ensures consistent and structured error handling across all APIs.
 * ================================================================
 */
public class ErrorResponse {
	
	private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
 
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
    
=======
 * Standardized error response DTO. Custom constructor sets the
 * timestamp automatically; Lombok generates getters/setters.
 * ================================================================
 */
@Getter
@Setter
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = LocalDateTime.now();
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
