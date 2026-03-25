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


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: AuthController
 * DESCRIPTION:
 * This controller handles all authentication-related APIs including
 * user registration, login, token refresh, logout, and profile
 * management such as uploading profile picture, resume, and fetching
 * user profile details.
 * ================================================================
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /*
     * Logger instance for tracking API calls and debugging
     */
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    

    /* ================================================================
     * METHOD: register
     * DESCRIPTION:
     * Registers a new user by validating input data and delegating
     * the request to the AuthService.
     * ================================================================ */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        logger.info("Register request for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        logger.info("User registered successfully: {}", request.getEmail());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    

    /* ================================================================
     * METHOD: login
     * DESCRIPTION:
     * Authenticates user credentials and returns access and refresh
     * tokens upon successful authentication.
     * ================================================================ */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        logger.info("Login attempt for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        logger.info("Login successful for email: {}", request.getEmail());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: refresh
     * DESCRIPTION:
     * Generates a new access token using a valid refresh token
     * provided by the user.
     * ================================================================ */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {

        logger.info("Token refresh requested");

        String refreshToken = body.get("refreshToken");
        AuthResponse response = authService.refresh(refreshToken);

        logger.info("Token refreshed successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: logout
     * DESCRIPTION:
     * Logs out the user by invalidating the refresh token stored
     * in the database.
     * ================================================================ */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body) {

        logger.info("Logout request received");

        String refreshToken = body.get("refreshToken");
        authService.logout(refreshToken);

        logger.info("User logged out successfully");

        return new ResponseEntity<>(Map.of("message", "Logged out successfully"), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: uploadProfilePicture
     * DESCRIPTION:
     * Uploads the user's profile picture to cloud storage and updates
     * the profile with the new image URL.
     * ================================================================ */
    @PutMapping("/profile/picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestPart("picture") MultipartFile picture,
            @RequestHeader("X-User-Id") Long userId) throws IOException {

        logger.info("User [{}] uploading profile picture", userId);

        String url = authService.updateProfilePicture(userId, picture);

        logger.info("Profile picture updated for user [{}]", userId);

        return new ResponseEntity<>(Map.of("profilePictureUrl", url), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: uploadResume
     * DESCRIPTION:
     * Uploads the user's resume file to cloud storage and updates
     * the user profile with the resume URL.
     * ================================================================ */
    @PutMapping("/profile/resume")
    public ResponseEntity<Map<String, String>> uploadResume(
            @RequestPart("resume") MultipartFile resume,
            @RequestHeader("X-User-Id") Long userId) throws IOException {

        logger.info("User [{}] uploading resume", userId);

        String url = authService.updateProfileResume(userId, resume);

        logger.info("Resume updated for user [{}]", userId);

        return new ResponseEntity<>(Map.of("resumeUrl", url), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: getProfile
     * DESCRIPTION:
     * Fetches the profile details of the authenticated user based
     * on the provided user ID.
     * ================================================================ */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader("X-User-Id") Long userId) {

        logger.info("Fetching profile for user [{}]", userId);

        UserProfileResponse response = authService.getProfile(userId);

        logger.info("Profile fetched successfully for user [{}]", userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}