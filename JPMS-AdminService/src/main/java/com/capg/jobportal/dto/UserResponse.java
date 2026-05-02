package com.capg.jobportal.dto;

<<<<<<< HEAD

=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: UserResponse
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents user information returned to the client
 * or other services.
 *
 * It includes:
 * - Basic user details (id, name, email, phone)
 * - Role and account status
 * - Profile-related data (profile picture, resume)
 *
 * PURPOSE:
 * Provides a safe and structured representation of user data
 * without exposing sensitive fields like password.
 * ================================================================
 */
public class UserResponse {

	private Long id;
=======
 * DTO for user information returned by AdminService.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class UserResponse {

    private Long id;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String profilePictureUrl;
    private String resumeUrl;
<<<<<<< HEAD
 
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
=======
    private String companyName;
    private String selectedByCompany;
    private String createdAt;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
