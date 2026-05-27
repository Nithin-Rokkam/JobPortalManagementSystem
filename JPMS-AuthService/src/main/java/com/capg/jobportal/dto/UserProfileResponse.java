package com.capg.jobportal.dto;

import com.capg.jobportal.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: UserProfileResponse
 * DESCRIPTION:
 * DTO for user profile data. Lombok generates all boilerplate.
 * Static factory method fromEntity() maps the JPA entity to this DTO.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String status;
    private String profilePictureUrl;
    private String resumeUrl;
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
}
