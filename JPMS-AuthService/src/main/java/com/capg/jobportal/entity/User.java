package com.capg.jobportal.entity;

import java.time.LocalDateTime;

import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;

<<<<<<< HEAD
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


=======
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: User
 * DESCRIPTION:
<<<<<<< HEAD
 * This entity represents the "users" table in the database.
 * It stores all details related to users of the platform,
 * including both job seekers and recruiters.
 *
 * KEY FEATURES:
 * - Stores user credentials (email, password)
 * - Maintains user role (e.g., JOB_SEEKER, RECRUITER, ADMIN)
 * - Tracks account status using UserStatus enum
 * - Supports profile-related data (profile picture, resume)
 * - Handles authentication tokens (refreshToken)
 * - Includes audit fields (createdAt, updatedAt)
 *
 * LIFECYCLE METHODS:
 * - @PrePersist → Automatically sets createdAt and updatedAt
 *   when a new user record is created.
 * - @PreUpdate → Updates updatedAt whenever the user record is modified.
 *
 * PURPOSE:
 * Acts as the persistence model for managing user data,
 * authentication, and profile-related information using JPA/Hibernate.
 * ================================================================
 */
@Entity
@Table(name = "users")
public class User {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 100)
    private String name;
 
    @Column(nullable = false, unique = true, length = 150)
    private String email;
 
    @Column(nullable = false)
    private String password;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
 
    @Column(length = 20)
    private String phone;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
 
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
 
    @Column(name = "resume_url")
    private String resumeUrl;
 
    @Column(name = "refresh_token")
    private String refreshToken;
 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
=======
 * JPA entity for the "users" table. Lombok @Getter/@Setter generate
 * all accessors. @NoArgsConstructor provides the JPA-required
 * no-arg constructor. Lifecycle callbacks are kept manually.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "reset_password_otp")
    private String resetPasswordOtp;

    @Column(name = "otp_expiry_time")
    private LocalDateTime otpExpiryTime;

    @Column(name = "email_verification_otp")
    private String emailVerificationOtp;

    @Column(name = "email_verification_expiry")
    private LocalDateTime emailVerificationExpiry;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "selected_by_company", length = 200)
    private String selectedByCompany;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
<<<<<<< HEAD
 
=======

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
<<<<<<< HEAD
    
    public User() {
    	
    }

    
	public User(Long id, String name, String email, String password, Role role, String phone, UserStatus status,
			String profilePictureUrl, String resumeUrl, String refreshToken, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.phone = phone;
		this.status = status;
		this.profilePictureUrl = profilePictureUrl;
		this.resumeUrl = resumeUrl;
		this.refreshToken = refreshToken;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
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

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

    
=======

    /**
     * Kept for backward compatibility with existing tests.
     * Matches the original 12-arg constructor signature.
     */
    public User(Long id, String name, String email, String password,
                Role role, String phone, UserStatus status,
                String profilePictureUrl, String resumeUrl,
                String refreshToken, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id                 = id;
        this.name               = name;
        this.email              = email;
        this.password           = password;
        this.role               = role;
        this.phone              = phone;
        this.status             = status;
        this.profilePictureUrl  = profilePictureUrl;
        this.resumeUrl          = resumeUrl;
        this.refreshToken       = refreshToken;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
