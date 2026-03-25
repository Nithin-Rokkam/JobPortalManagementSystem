package com.capg.jobportal.dto;

public class UserResponse {

	private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String profilePictureUrl;
    private String resumeUrl;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
 
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
 
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
 
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
 
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
 
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
 
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
}
