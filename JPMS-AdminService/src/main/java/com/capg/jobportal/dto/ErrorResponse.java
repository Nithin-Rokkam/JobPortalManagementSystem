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
<<<<<<< HEAD
 * CLASS: ApplicationStats
 * DESCRIPTION:
 * This DTO represents aggregated statistics of job applications
 * across the platform.
 *
 * It includes counts for:
 * - Total applications
 * - Applications in different stages (Applied, Under Review,
 *   Shortlisted, Rejected)
 *
 * PURPOSE:
 * Used for analytics, reporting, and admin dashboards to provide
 * insights into application trends and recruitment progress.
 * ================================================================
 */
=======
 * CLASS: ErrorResponse
 * DESCRIPTION:
 * DTO for error responses. Uses @Getter/@Setter since it has a
 * custom constructor that sets the timestamp automatically.
 * ================================================================
 */
@Getter
@Setter
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message) {
<<<<<<< HEAD
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
=======
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = LocalDateTime.now();
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
