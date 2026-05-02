package com.capg.jobportal.controller;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.LoginRequest;
import com.capg.jobportal.dto.RegisterRequest;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.service.AuthService;

import jakarta.validation.Valid;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: AuthController
 * DESCRIPTION:
 * This controller handles all authentication-related APIs including:
 * - User Registration
 * - Login & Token Generation
 * - Token Refresh & Logout
 * - Profile Management (Picture, Resume, Profile Fetch)
 *
 * NOTE:
 * All APIs are exposed via API Gateway.
 * User identity is passed through headers (X-User-Id) from Gateway.
 * ================================================================
 */

@Tag(name = "Auth APIs", description = "Authentication and User Profile APIs")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /*
     * Logger instance for tracking API activity
     */
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /* ================================================================
     * METHOD: register
     * DESCRIPTION:
     * Registers a new user in the system.
     * ================================================================ */
    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        logger.info("Register request for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        logger.info("User registered successfully: {}", request.getEmail());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: login
     * DESCRIPTION:
     * Authenticates user and generates JWT access & refresh tokens.
     * ================================================================ */
    @Operation(summary = "Login user and generate JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        logger.info("Login attempt for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        logger.info("Login successful for email: {}", request.getEmail());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: refresh
     * DESCRIPTION:
     * Generates a new access token using refresh token.
     * ================================================================ */
    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody Map<String, String> body) {

        logger.info("Token refresh requested");

        String refreshToken = body.get("refreshToken");
        AuthResponse response = authService.refresh(refreshToken);

        logger.info("Token refreshed successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: logout
     * DESCRIPTION:
     * Logs out the user by invalidating refresh token.
     * ================================================================ */
    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody Map<String, String> body) {

        logger.info("Logout request received");

        String refreshToken = body.get("refreshToken");
        authService.logout(refreshToken);

        logger.info("User logged out successfully");

        return new ResponseEntity<>(Map.of("message", "Logged out successfully"), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: uploadProfilePicture
     * DESCRIPTION:
     * Uploads user profile picture to Cloudinary.
     * ================================================================ */
    @Operation(summary = "Upload profile picture")
    @PutMapping("/profile/picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(

            @RequestPart("picture") MultipartFile picture,

            @Parameter(description = "User ID from Gateway", required = true)
            @RequestHeader("X-User-Id") Long userId
    ) throws IOException {

        logger.info("User [{}] uploading profile picture", userId);

        String url = authService.updateProfilePicture(userId, picture);

        logger.info("Profile picture updated for user [{}]", userId);

        return new ResponseEntity<>(Map.of("profilePictureUrl", url), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: uploadResume
     * DESCRIPTION:
     * Uploads user resume to Cloudinary.
     * ================================================================ */
    @Operation(summary = "Upload resume")
    @PutMapping("/profile/resume")
    public ResponseEntity<Map<String, String>> uploadResume(

            @RequestPart("resume") MultipartFile resume,

            @Parameter(description = "User ID from Gateway", required = true)
            @RequestHeader("X-User-Id") Long userId
    ) throws IOException {

        logger.info("User [{}] uploading resume", userId);

        String url = authService.updateProfileResume(userId, resume);

        logger.info("Resume updated for user [{}]", userId);

        return new ResponseEntity<>(Map.of("resumeUrl", url), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: getProfile
     * DESCRIPTION:
     * Fetches profile details of the logged-in user.
     * ================================================================ */
    @Operation(summary = "Get logged-in user profile")
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(

            @Parameter(description = "User ID from Gateway", required = true)
            @RequestHeader("X-User-Id") Long userId
    ) {

        logger.info("Fetching profile for user [{}]", userId);

        UserProfileResponse response = authService.getProfile(userId);

        logger.info("Profile fetched successfully for user [{}]", userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
<<<<<<< HEAD
=======

    /* ================================================================
     * METHOD: updateCompanyName
     * DESCRIPTION:
     * Allows a recruiter to update their company name.
     * ================================================================ */
    @Operation(summary = "Update recruiter company name")
    @PutMapping("/profile/company")
    public ResponseEntity<Map<String, String>> updateCompanyName(
            @RequestBody Map<String, String> body,
            @Parameter(description = "User ID from Gateway", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        String companyName = body.get("companyName");
        logger.info("User [{}] updating company name to '{}'", userId, companyName);
        authService.updateCompanyName(userId, companyName);
        logger.info("Company name updated for user [{}]", userId);
        return new ResponseEntity<>(Map.of("companyName", companyName != null ? companyName : ""), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: verifyRegistration
     * DESCRIPTION:
     * Validates the email verification OTP sent during registration.
     * Activates the user account on success.
     * ================================================================ */
    @Operation(summary = "Verify registration OTP")
    @PostMapping("/verify-registration")
    public ResponseEntity<Map<String, String>> verifyRegistration(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp   = body.get("otp");

        logger.info("Verify registration OTP for email: {}", email);

        authService.verifyRegistrationOtp(email, otp);

        logger.info("Registration verified for email: {}", email);

        return new ResponseEntity<>(Map.of("message", "Email verified successfully. You can now log in."), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: resendRegistrationOtp
     * DESCRIPTION:
     * Resends a new OTP to the user's email for account verification.
     * ================================================================ */
    @Operation(summary = "Resend registration OTP")
    @PostMapping("/resend-registration-otp")
    public ResponseEntity<Map<String, String>> resendRegistrationOtp(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        logger.info("Resend registration OTP for email: {}", email);

        authService.resendRegistrationOtp(email);

        logger.info("Registration OTP resent for email: {}", email);

        return new ResponseEntity<>(Map.of("message", "New OTP sent to your email"), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: forgotPassword
     * DESCRIPTION:
     * Accepts user email and triggers OTP generation.
     * The OTP is sent to the user's email via RabbitMQ/NotificationService.
     * ================================================================ */
    @Operation(summary = "Request password reset OTP")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        logger.info("Forgot password request for email: {}", email);

        authService.forgotPassword(email);

        logger.info("OTP sent successfully for email: {}", email);

        return new ResponseEntity<>(Map.of("message", "OTP sent to your email"), HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: resetPassword
     * DESCRIPTION:
     * Validates OTP and resets the user's password.
     * Requires email, otp, and newPassword in the request body.
     * ================================================================ */
    @Operation(summary = "Reset password using OTP")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        logger.info("Reset password attempt for email: {}", email);

        authService.resetPassword(email, otp, newPassword);

        logger.info("Password reset successful for email: {}", email);

        return new ResponseEntity<>(Map.of("message", "Password reset successfully"), HttpStatus.OK);
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}