package com.capg.jobportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: LoginRequest
 * DESCRIPTION:
 * This DTO is used to capture user credentials during the login
 * process.
 *
 * It includes validation constraints to ensure:
 * - Email is properly formatted
 * - Password meets minimum length requirements
 *
 * PURPOSE:
 * Acts as a request payload for authentication APIs, ensuring
 * valid input before processing login logic.
 * ================================================================
 */
public class LoginRequest {
	
	@NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
 
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
    
    
}
