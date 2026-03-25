package com.capg.jobportal.controller;

import java.util.List;
import java.util.Map;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.jobportal.dto.JobResponse;
import com.capg.jobportal.dto.PlatformReport;
import com.capg.jobportal.dto.UserResponse;
import com.capg.jobportal.exception.AccessDeniedException;
import com.capg.jobportal.model.AuditLog;
import com.capg.jobportal.service.AdminService;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: AdminController
 * DESCRIPTION:
 * This controller handles all admin-level operations including
 * user management, job management, platform reports, and audit logs.
 * All endpoints are restricted to ADMIN users only.
 * ================================================================
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /*
     * Logger object for tracking admin operations
     */
    private static final Logger logger = LogManager.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    
    /* ================================================================
     * METHOD: assertAdmin
     * DESCRIPTION:
     * Validates whether the incoming request has ADMIN role.
     * Throws AccessDeniedException if unauthorized.
     * ================================================================ */
    private void assertAdmin(String role) {
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            logger.warn("Access denied — role received: {}", role);
            throw new AccessDeniedException("Access denied. ADMIN role required.");
        }
    }

    
    /* ================================================================
     * METHOD: getAllUsers
     * DESCRIPTION:
     * Retrieves all users from the system. Accessible only by ADMIN.
     * ================================================================ */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestHeader("X-User-Role") String role) {

        logger.info("Fetching all users");
        assertAdmin(role);

        List<UserResponse> users = adminService.getAllUsers();
        logger.info("Total users fetched: {}", users.size());

        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    

    /* ================================================================
     * METHOD: deleteUser
     * DESCRIPTION:
     * Deletes a user by ID and records the action in audit logs.
     * ================================================================ */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Delete request for user ID: {} by admin ID: {}", id, adminId);
        assertAdmin(role);

        adminService.deleteUser(id, adminId);
        logger.info("User deleted successfully: {}", id);

        return new ResponseEntity<>(Map.of("message", "User deleted successfully"), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: banUser
     * DESCRIPTION:
     * Bans a user and prevents further access to the platform.
     * ================================================================ */
    @PutMapping("/users/{id}/ban")
    public ResponseEntity<Map<String, String>> banUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Ban request for user ID: {} by admin ID: {}", id, adminId);
        assertAdmin(role);

        adminService.banUser(id, adminId);
        logger.info("User banned successfully: {}", id);

        return new ResponseEntity<>(Map.of("message", "User banned successfully"), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: unbanUser
     * DESCRIPTION:
     * Restores access for a previously banned user.
     * ================================================================ */
    @PutMapping("/users/{id}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Unban request for user ID: {} by admin ID: {}", id, adminId);
        assertAdmin(role);

        adminService.unbanUser(id, adminId);
        logger.info("User unbanned successfully: {}", id);

        return new ResponseEntity<>(Map.of("message", "User unbanned successfully"), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: getAllJobs
     * DESCRIPTION:
     * Retrieves all job listings available in the system.
     * ================================================================ */
    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponse>> getAllJobs(
            @RequestHeader("X-User-Role") String role) {

        logger.info("Fetching all jobs");
        assertAdmin(role);

        List<JobResponse> jobs = adminService.getAllJobs();
        logger.info("Total jobs fetched: {}", jobs.size());

        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: deleteJob
     * DESCRIPTION:
     * Deletes a job by ID and records the action in audit logs.
     * ================================================================ */
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Delete job request for ID: {} by admin ID: {}", id, adminId);
        assertAdmin(role);

        adminService.deleteJob(id, adminId);
        logger.info("Job deleted successfully: {}", id);

        return new ResponseEntity<>(Map.of("message", "Job deleted successfully"), HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: getReport
     * DESCRIPTION:
     * Generates a platform-level report including users, jobs,
     * and application statistics.
     * ================================================================ */
    @GetMapping("/reports")
    public ResponseEntity<PlatformReport> getReport(
            @RequestHeader("X-User-Role") String role) {

        logger.info("Generating platform report");
        assertAdmin(role);

        PlatformReport report = adminService.getReport();
        logger.info("Report generated successfully");

        return new ResponseEntity<>(report, HttpStatus.OK);
    }
    

    /* ================================================================
     * METHOD: getAuditLogs
     * DESCRIPTION:
     * Retrieves all audit logs for admin monitoring and tracking.
     * ================================================================ */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestHeader("X-User-Role") String role) {

        logger.info("Fetching audit logs");
        assertAdmin(role);

        List<AuditLog> logs = adminService.getAuditLogs();
        logger.info("Total logs fetched: {}", logs.size());

        return new ResponseEntity<>(logs, HttpStatus.OK);
    }
}