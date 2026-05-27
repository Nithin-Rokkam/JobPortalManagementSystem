package com.capg.jobportal.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ErrorResponse
 * DESCRIPTION:
 * DTO for error responses. Uses @Getter/@Setter since it has a
 * custom constructor that sets the timestamp automatically.
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
}
