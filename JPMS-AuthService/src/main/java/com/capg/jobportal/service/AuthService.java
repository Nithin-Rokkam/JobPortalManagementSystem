package com.capg.jobportal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
<<<<<<< HEAD
=======
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
=======
import com.capg.jobportal.event.PasswordResetEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.Random;
import java.time.LocalDateTime;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
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
<<<<<<< HEAD
=======
    private final RabbitTemplate rabbitTemplate;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
<<<<<<< HEAD
                       CloudinaryUtil cloudinaryUtil) {
=======
                       CloudinaryUtil cloudinaryUtil,
                       RabbitTemplate rabbitTemplate) {
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.cloudinaryUtil = cloudinaryUtil;
<<<<<<< HEAD
=======
        this.rabbitTemplate = rabbitTemplate;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    }
    

    /* ================================================================
     * METHOD: register
     * DESCRIPTION:
<<<<<<< HEAD
     * Registers a new user after validating role and checking for
     * duplicate email. Password is securely encoded before saving.
     * ================================================================ */
=======
     * Registers a new user with PENDING_VERIFICATION status.
     * If the email already exists but is PENDING_VERIFICATION,
     * updates the record with new details and resends a fresh OTP.
     * ================================================================ */
    @Transactional
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    public AuthResponse register(RegisterRequest request) {

        logger.info("Register request for email: {}", request.getEmail());

        if (request.getRole() == Role.ADMIN) {
            logger.warn("Admin registration blocked");
            throw new IllegalArgumentException("Admin registration is not allowed");
        }

<<<<<<< HEAD
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already in use");
        }

=======
        // Check if email already exists
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (existingUser != null) {
            // If account is already verified (ACTIVE/BANNED), reject
            if (existingUser.getStatus() != UserStatus.PENDING_VERIFICATION) {
                logger.warn("Email already exists and is verified: {}", request.getEmail());
                throw new UserAlreadyExistsException("Email already in use");
            }
            // Account exists but is unverified — update ALL details and generate fresh OTP
            logger.info("Re-registering unverified account for: {}", request.getEmail());
            String otp = String.format("%06d", new Random().nextInt(999999));
            existingUser.setName(request.getName());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setRole(request.getRole());
            existingUser.setPhone(request.getPhone());
            if (request.getRole() == Role.RECRUITER && request.getCompanyName() != null) {
                existingUser.setCompanyName(request.getCompanyName());
            }
            existingUser.setEmailVerificationOtp(otp);
            existingUser.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.saveAndFlush(existingUser);   // flush immediately so DB is updated before RabbitMQ fires

            // Send AFTER transaction commits to guarantee DB is consistent
            final String finalOtp = otp;
            final String finalName = existingUser.getName();
            final String finalEmail = existingUser.getEmail();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    PasswordResetEvent event = new PasswordResetEvent(finalEmail, finalName, finalOtp);
                    rabbitTemplate.convertAndSend("jobportal.exchange", "registration.otp", event);
                    logger.info("Registration OTP event sent after commit for: {}", finalEmail);
                }
            });

            logger.info("Updated unverified account for: {}", request.getEmail());
            return new AuthResponse("OTP sent to your email. Please verify to activate your account.");
        }

        // New registration
        String otp = String.format("%06d", new Random().nextInt(999999));

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
<<<<<<< HEAD

        userRepository.save(user);

        logger.info("User registered successfully: {}", request.getEmail());

        return new AuthResponse("Registration successful. Please login.");
=======
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        if (request.getRole() == Role.RECRUITER && request.getCompanyName() != null) {
            user.setCompanyName(request.getCompanyName());
        }
        user.setEmailVerificationOtp(otp);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.saveAndFlush(user);   // flush immediately

        // Send AFTER transaction commits
        final String finalOtp = otp;
        final String finalName = user.getName();
        final String finalEmail = user.getEmail();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                PasswordResetEvent event = new PasswordResetEvent(finalEmail, finalName, finalOtp);
                rabbitTemplate.convertAndSend("jobportal.exchange", "registration.otp", event);
                logger.info("Registration OTP event sent after commit for: {}", finalEmail);
            }
        });

        logger.info("User registered (pending verification): {}", request.getEmail());

        return new AuthResponse("OTP sent to your email. Please verify to activate your account.");
    }


    /* ================================================================
     * METHOD: verifyRegistrationOtp
     * DESCRIPTION:
     * Validates the email verification OTP. On success, activates
     * the user account (status → ACTIVE) and clears OTP fields.
     * ================================================================ */
    @Transactional
    public void verifyRegistrationOtp(String email, String otp) {
        String cleanEmail = email != null ? email.trim() : "";
        String cleanOtp   = otp   != null ? otp.trim()   : "";

        logger.info("Verifying registration OTP for email: {}", cleanEmail);

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalArgumentException("Account is already verified or not in pending state");
        }

        if (user.getEmailVerificationOtp() == null || !user.getEmailVerificationOtp().equals(cleanOtp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        if (user.getEmailVerificationExpiry() == null || user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationOtp(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        logger.info("Email verified and account activated for: {}", cleanEmail);
    }


    /* ================================================================
     * METHOD: resendRegistrationOtp
     * DESCRIPTION:
     * Generates a new OTP for a PENDING_VERIFICATION user and
     * re-sends it via RabbitMQ.
     * ================================================================ */
    @Transactional
    public void resendRegistrationOtp(String email) {
        String cleanEmail = email != null ? email.trim() : "";

        logger.info("Resend registration OTP for email: {}", cleanEmail);

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalArgumentException("Account is already verified");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setEmailVerificationOtp(otp);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.saveAndFlush(user);   // flush before RabbitMQ fires

        final String finalOtp = otp;
        final String finalName = user.getName();
        final String finalEmail = user.getEmail();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                PasswordResetEvent event = new PasswordResetEvent(finalEmail, finalName, finalOtp);
                rabbitTemplate.convertAndSend("jobportal.exchange", "registration.otp", event);
                logger.info("Resend registration OTP event sent after commit for: {}", finalEmail);
            }
        });

        logger.info("New registration OTP queued for: {}", cleanEmail);
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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

<<<<<<< HEAD
=======
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            logger.warn("Unverified user login attempt: {}", request.getEmail());
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED");
        }

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
    
    
<<<<<<< HEAD
=======
    /* ================================================================
     * METHOD: updateCompanyName
     * DESCRIPTION:
     * Allows a recruiter to update their company name in the DB.
     * ================================================================ */
    public void updateCompanyName(Long userId, String companyName) {
        logger.info("Updating company name for user: {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        user.setCompanyName(companyName != null ? companyName.trim() : null);
        userRepository.save(user);
        logger.info("Company name updated for user: {}", userId);
    }

    /* ================================================================
     * METHOD: updateSelectedByCompany
     * DESCRIPTION:
     * Called internally when a seeker's application status is set to
     * SELECTED. Stores the company name on the seeker's profile.
     * ================================================================ */
    public void updateSelectedByCompany(Long seekerId, String companyName) {
        logger.info("Updating selectedByCompany for seeker [{}] to '{}'", seekerId, companyName);
        User user = userRepository.findById(seekerId).orElse(null);
        if (user != null) {
            user.setSelectedByCompany(companyName);
            userRepository.save(user);
            logger.info("selectedByCompany updated for seeker [{}]", seekerId);
        }
    }

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    public List<String> getJobSeekerEmails() {
        return userRepository.findByRole(Role.JOB_SEEKER)
                .stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
<<<<<<< HEAD
=======

    /* ================================================================
     * METHOD: forgotPassword
     * DESCRIPTION:
     * Generates a 6-digit OTP, saves it to the user record with an
     * expiry time, and sends it via RabbitMQ to NotificationService.
     * ================================================================ */
    public void forgotPassword(String email) {
        logger.info("Forgot password request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetPasswordOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10)); // 10 min expiry
        userRepository.save(user);

        // Send Event to RabbitMQ
        PasswordResetEvent event = new PasswordResetEvent(user.getEmail(), user.getName(), otp);
        rabbitTemplate.convertAndSend("jobportal.exchange", "password.reset", event);

        logger.info("OTP sent to RabbitMQ for email: {}", email);
    }

    /* ================================================================
     * METHOD: resetPassword
     * DESCRIPTION:
     * Validates OTP and updates user's password if OTP is valid and
     * not expired.
     * ================================================================ */
    public void resetPassword(String email, String otp, String newPassword) {
        String cleanEmail = email != null ? email.trim() : "";
        String cleanOtp = otp != null ? otp.trim() : "";
        
        logger.info("Reset password attempt for email: {}", cleanEmail);

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + cleanEmail));

        if (user.getResetPasswordOtp() == null || !user.getResetPasswordOtp().equals(cleanOtp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);

        logger.info("Password reset successful for email: {}", cleanEmail);
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}