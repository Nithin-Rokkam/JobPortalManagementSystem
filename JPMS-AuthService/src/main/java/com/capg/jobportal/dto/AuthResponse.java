package com.capg.jobportal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: AuthResponse
 * DESCRIPTION:
 * DTO for authentication responses. Lombok generates getters/setters.
 * Custom constructors are kept for the two existing call sites.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class AuthResponse {

    private String message;
    private String accessToken;
    private String refreshToken;
    private String role;
    private Long userId;
    private String name;
    private String email;

    /** Used for simple message-only responses (e.g. registration) */
    public AuthResponse(String message) {
        this.message = message;
    }

    /** Used for full login/refresh responses */
    public AuthResponse(String accessToken, String refreshToken, String role,
                        Long userId, String name, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}
