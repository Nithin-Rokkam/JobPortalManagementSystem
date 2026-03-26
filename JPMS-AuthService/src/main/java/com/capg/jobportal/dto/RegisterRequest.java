package com.capg.jobportal.dto;

import com.capg.jobportal.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: RegisterRequest
 * DESCRIPTION:
 * This DTO is used to capture user details during the registration
 * process.
 *
 * It includes:
 * - Basic user information (name, email, password, phone)
 * - Role selection (e.g., JOB_SEEKER, RECRUITER)
 * - Validation annotations to enforce data integrity
 *
 * PURPOSE:
 * Ensures that only valid and complete user data is accepted
 * during account creation.
 * ================================================================
 */
public class RegisterRequest {
	
	@NotBlank(message = "Name is required")
    private String name;
 
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
 
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
 
    private String phone;
 
    @NotNull(message = "Role is required")
    private Role role;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
    
}
