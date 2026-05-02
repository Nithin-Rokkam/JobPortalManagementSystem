package com.capg.jobportal.dto;

import com.capg.jobportal.entity.User;
<<<<<<< HEAD

=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: UserProfileResponse
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents user profile details returned to the client.
 *
 * It includes:
 * - Basic user information (id, name, email, role)
 * - Contact details (phone)
 * - Account status
 * - Profile resources (profile picture, resume URL)
 *
 * KEY FEATURE:
 * - Includes a static factory method (fromEntity) to map User
 *   entity data into a response DTO.
 *
 * PURPOSE:
 * Provides a clean and secure representation of user data without
 * exposing internal entity structure.
 * ================================================================
 */
public class UserProfileResponse {
	
	private Long id;
=======
 * DTO for user profile data. Lombok generates all boilerplate.
 * Static factory method fromEntity() maps the JPA entity to this DTO.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    private String name;
    private String email;
    private String role;
    private String phone;
    private String status;
    private String profilePictureUrl;
    private String resumeUrl;
<<<<<<< HEAD
    
    
    public UserProfileResponse() {
    	
    }
    

    public static UserProfileResponse fromEntity(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.id = user.getId();
        response.name = user.getName();
        response.email = user.getEmail();
        response.role = user.getRole().name();
        response.phone = user.getPhone();
        response.status = user.getStatus().name();
        response.profilePictureUrl = user.getProfilePictureUrl();
        response.resumeUrl = user.getResumeUrl();
        return response;
    }


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
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


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}


	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}


	public String getResumeUrl() {
		return resumeUrl;
	}


	public void setResumeUrl(String resumeUrl) {
		this.resumeUrl = resumeUrl;
	}
    
=======
    private String companyName;
    private String selectedByCompany;
    private String createdAt;

    public static UserProfileResponse fromEntity(User user) {
        UserProfileResponse r = new UserProfileResponse();
        r.id                = user.getId();
        r.name              = user.getName();
        r.email             = user.getEmail();
        r.role              = user.getRole().name();
        r.phone             = user.getPhone();
        r.status            = user.getStatus().name();
        r.profilePictureUrl = user.getProfilePictureUrl();
        r.resumeUrl         = user.getResumeUrl();
        r.companyName       = user.getCompanyName();
        r.selectedByCompany = user.getSelectedByCompany();
        r.createdAt         = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        return r;
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
