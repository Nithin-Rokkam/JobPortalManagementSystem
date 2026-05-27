package com.capg.jobportal.entity;

import java.time.LocalDateTime;

import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: User
 * DESCRIPTION:
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
}
