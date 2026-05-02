package com.capg.jobportal.dto;

<<<<<<< HEAD
=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: AuthResponse
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents the response returned after successful
 * authentication (login/register/refresh).
 *
 * It contains:
 * - JWT access token and refresh token
 * - User details (id, name, email, role)
 * - Optional message for status communication
 *
 * PURPOSE:
 * Provides a structured response for authentication-related APIs,
 * enabling secure communication between client and server.
 * ================================================================
 */
public class AuthResponse {
	
	private String message;
	private String accessToken;
=======
 * DTO for authentication responses. Lombok generates getters/setters.
 * Custom constructors are kept for the two existing call sites.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class AuthResponse {

    private String message;
    private String accessToken;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    private String refreshToken;
    private String role;
    private Long userId;
    private String name;
    private String email;
<<<<<<< HEAD
 
  
    public AuthResponse() {
    	
    }
    
    
    public AuthResponse(String message) {
        this.message = message;
    }
    
    
    public AuthResponse(String accessToken, String refreshToken, String role, Long userId, String name, String email) {
=======

    /** Used for simple message-only responses (e.g. registration) */
    public AuthResponse(String message) {
        this.message = message;
    }

    /** Used for full login/refresh responses */
    public AuthResponse(String accessToken, String refreshToken, String role,
                        Long userId, String name, String email) {
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
<<<<<<< HEAD
    
    

	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
=======
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
