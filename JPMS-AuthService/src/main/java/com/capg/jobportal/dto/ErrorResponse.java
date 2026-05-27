package com.capg.jobportal.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ErrorResponse
 * DESCRIPTION:
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
}
