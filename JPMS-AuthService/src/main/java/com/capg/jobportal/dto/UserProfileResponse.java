package com.capg.jobportal.dto;

import com.capg.jobportal.entity.User;

public class UserProfileResponse {
	
	private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String status;
    private String profilePictureUrl;
    private String resumeUrl;
    
    
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
    
}
