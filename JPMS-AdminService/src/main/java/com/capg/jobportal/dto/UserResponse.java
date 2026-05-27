package com.capg.jobportal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: UserResponse
 * DESCRIPTION:
 * DTO for user information returned by AdminService.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String profilePictureUrl;
    private String resumeUrl;
    private String companyName;
    private String selectedByCompany;
    private String createdAt;
}
