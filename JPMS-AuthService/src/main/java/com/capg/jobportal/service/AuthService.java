package com.capg.jobportal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.dao.UserRepository;
import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.LoginRequest;
import com.capg.jobportal.dto.RegisterRequest;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.entity.User;
import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;
import com.capg.jobportal.exception.ResourceNotFoundException;
import com.capg.jobportal.exception.UserAlreadyExistsException;
import com.capg.jobportal.security.JwtUtil;
import com.capg.jobportal.util.CloudinaryUtil;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: AuthService
 * DESCRIPTION:
 * This service handles all authentication-related operations such as
 * user registration, login, token management, profile updates, and
 * internal admin operations like user deletion and status updates.
 * ================================================================
 */
@Service
public class AuthService {

    /*
     * Logger instance for tracking application flow
     */
    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CloudinaryUtil cloudinaryUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       CloudinaryUtil cloudinaryUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.cloudinaryUtil = cloudinaryUtil;
    }
    

    /* ================================================================
     * METHOD: register
     * DESCRIPTION:
     * Registers a new user after validating role and checking for
     * duplicate email. Password is securely encoded before saving.
     * ================================================================ */
    public AuthResponse register(RegisterRequest request) {

        logger.info("Register request for email: {}", request.getEmail());

        if (request.getRole() == Role.ADMIN) {
            logger.warn("Admin registration blocked");
            throw new IllegalArgumentException("Admin registration is not allowed");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());

        userRepository.save(user);

        logger.info("User registered successfully: {}", request.getEmail());

        return new AuthResponse("Registration successful. Please login.");
    }
    

    /* ================================================================
     * METHOD: login
     * DESCRIPTION:
     * Authenticates user credentials and generates access and refresh
     * tokens if credentials are valid and account is active.
     * ================================================================ */
    public AuthResponse login(LoginRequest request) {
    	logger.info("CHECK LOGGER FORMAT");

        logger.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            logger.warn("User not found: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            logger.warn("Banned user login attempt: {}", request.getEmail());
            throw new IllegalArgumentException("Account suspended");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken();

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        logger.info("Login successful for user ID: {}", user.getId());

        return new AuthResponse(accessToken, refreshToken, user.getRole().name(),
                user.getId(), user.getName(), user.getEmail());
    }
    

    /* ================================================================
     * METHOD: refresh
     * DESCRIPTION:
     * Generates new access and refresh tokens using a valid refresh token.
     * ================================================================ */
    public AuthResponse refresh(String refreshToken) {

        logger.debug("Refreshing token");

        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);

        if (user == null) {
            logger.warn("Invalid refresh token");
            throw new ResourceNotFoundException("Invalid or expired refresh token");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            logger.warn("Banned user token refresh attempt: {}", user.getId());
            throw new IllegalArgumentException("Account suspended");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken();

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        logger.info("Token refreshed for user ID: {}", user.getId());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getRole().name(),
                user.getId(), user.getName(), user.getEmail());
    }
    

    /* ================================================================
     * METHOD: logout
     * DESCRIPTION:
     * Logs out user by clearing refresh token from database.
     * ================================================================ */
    public void logout(String refreshToken) {

        logger.debug("Logout request");

        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);

        if (user == null) {
            logger.warn("Invalid logout token");
            throw new ResourceNotFoundException("Invalid refresh token");
        }

        user.setRefreshToken(null);
        userRepository.save(user);

        logger.info("User logged out: {}", user.getId());
    }

    
    /* ================================================================
     * METHOD: updateProfilePicture
     * DESCRIPTION:
     * Uploads user profile picture to cloud storage and updates DB.
     * ================================================================ */
    public String updateProfilePicture(Long userId, MultipartFile picture) throws IOException {

        logger.info("Updating profile picture for user: {}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        String url = cloudinaryUtil.uploadProfilePicture(picture);
        user.setProfilePictureUrl(url);
        userRepository.save(user);

        logger.info("Profile picture updated for user: {}", userId);

        return url;
    }
    

    /* ================================================================
     * METHOD: updateProfileResume
     * DESCRIPTION:
     * Uploads user resume to cloud storage and updates DB.
     * ================================================================ */
    public String updateProfileResume(Long userId, MultipartFile resume) throws IOException {

        logger.info("Updating resume for user: {}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        String url = cloudinaryUtil.uploadResume(resume);
        user.setResumeUrl(url);
        userRepository.save(user);

        logger.info("Resume updated for user: {}", userId);

        return url;
    }
    

    /* ================================================================
     * METHOD: getProfile
     * DESCRIPTION:
     * Fetches user profile details by user ID.
     * ================================================================ */
    public UserProfileResponse getProfile(Long userId) {

        logger.debug("Fetching profile for user: {}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        return UserProfileResponse.fromEntity(user);
    }
    

    /* ================================================================
     * METHOD: getAllUsers
     * DESCRIPTION:
     * Retrieves all users for admin/internal use.
     * ================================================================ */
    public List<UserProfileResponse> getAllUsers() {

        logger.debug("Fetching all users");

        List<User> users = userRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();

        for (User user : users) {
            result.add(UserProfileResponse.fromEntity(user));
        }

        logger.info("Total users fetched: {}", result.size());

        return result;
    }
    

    /* ================================================================
     * METHOD: deleteUser
     * DESCRIPTION:
     * Deletes a user by ID (used by admin).
     * ================================================================ */
    public void deleteUser(Long userId) {

        logger.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        userRepository.delete(user);

        logger.info("User deleted: {}", userId);
    }

    
    /* ================================================================
     * METHOD: updateUserStatus
     * DESCRIPTION:
     * Updates user status (BAN / UNBAN) and invalidates token.
     * ================================================================ */
    public void updateUserStatus(Long userId, String status) {

        logger.info("Updating status for user {} to {}", userId, status);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User not found: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        user.setStatus(UserStatus.valueOf(status));
        user.setRefreshToken(null);

        userRepository.save(user);

        logger.info("User status updated successfully");
    }

    
    /* ================================================================
     * METHOD: invalidateTokenByUserId
     * DESCRIPTION:
     * Clears refresh token of user (used after ban).
     * ================================================================ */
    public void invalidateTokenByUserId(Long userId) {

        logger.info("Invalidating token for user: {}", userId);

        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setRefreshToken(null);
            userRepository.save(user);
            logger.info("Token invalidated");
        } else {
            logger.warn("User not found for token invalidation");
        }
    }
}