package com.capg.jobportal.controller;

import java.util.List;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.service.AuthService;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: InternalAuthController
 * DESCRIPTION:
 * This controller provides internal APIs for user management
 * operations such as fetching users, deleting users, banning,
 * unbanning, and invalidating tokens.
 *
 * NOTE:
 * This controller is strictly for internal microservice communication
 * (e.g., AdminService) and is NOT exposed to end users via API Gateway.
 * ================================================================
 */
@RestController
@RequestMapping("/api/internal")
public class InternalAuthController {

    /*
     * Logger instance for tracking internal API calls
     */
    private static final Logger logger =
            LogManager.getLogger(InternalAuthController.class);

    private final AuthService authService;

    public InternalAuthController(AuthService authService) {
        this.authService = authService;
    }

    /* ================================================================
     * METHOD: getAllUsers
     * DESCRIPTION:
     * Fetches all users from the system. Used internally by AdminService.
     * ================================================================ */
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {

        logger.info("Internal API → Fetching all users");

        List<UserProfileResponse> users = authService.getAllUsers();

        logger.info("Returned {} users", users.size());

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: deleteUser
     * DESCRIPTION:
     * Deletes a user by ID. Used internally by AdminService.
     * ================================================================ */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        logger.info("Internal API → Deleting user [{}]", id);

        authService.deleteUser(id);

        logger.info("User [{}] deleted successfully", id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: banUser
     * DESCRIPTION:
     * Updates user status to BANNED and prevents further access.
     * ================================================================ */
    @PutMapping("/users/{id}/ban")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {

        logger.info("Internal API → Banning user [{}]", id);

        authService.updateUserStatus(id, "BANNED");

        logger.info("User [{}] banned successfully", id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: unbanUser
     * DESCRIPTION:
     * Updates user status to ACTIVE allowing access again.
     * ================================================================ */
    @PutMapping("/users/{id}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id) {

        logger.info("Internal API → Unbanning user [{}]", id);

        authService.updateUserStatus(id, "ACTIVE");

        logger.info("User [{}] unbanned successfully", id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /* ================================================================
     * METHOD: invalidateToken
     * DESCRIPTION:
     * Invalidates user session by clearing refresh token.
     * Used when admin bans a user.
     * ================================================================ */
    @PutMapping("/users/{id}/invalidate-token")
    public ResponseEntity<Void> invalidateToken(@PathVariable Long id) {

        logger.info("Internal API → Invalidating token for user [{}]", id);

        authService.invalidateTokenByUserId(id);

        logger.info("Token invalidated for user [{}]", id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}