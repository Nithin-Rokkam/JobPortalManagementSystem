package com.capg.jobportal.service;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Service;

import com.capg.jobportal.client.AdminAppClient;
import com.capg.jobportal.client.AdminJobClient;
import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.*;
import com.capg.jobportal.model.AuditLog;
import com.capg.jobportal.repository.AuditLogRepository;

import java.util.List;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: AdminService
 * DESCRIPTION:
 * This service handles all admin-related business logic including
 * user management, job management, platform reporting, and audit
 * logging. It interacts with multiple microservices via clients
 * and ensures proper tracking of admin actions.
 * ================================================================
 */
@Service
public class AdminService {

    /*
     * Logger for tracking admin operations and debugging
     */
    private static final Logger logger = LogManager.getLogger(AdminService.class);

    private final AuthServiceClient authServiceClient;
    private final AdminJobClient adminJobClient;
    private final AdminAppClient adminAppClient;
    private final AuditLogRepository auditLogRepository;

    public AdminService(AuthServiceClient authServiceClient,
                        AdminJobClient adminJobClient,
                        AdminAppClient adminAppClient,
                        AuditLogRepository auditLogRepository) {
        this.authServiceClient = authServiceClient;
        this.adminJobClient = adminJobClient;
        this.adminAppClient = adminAppClient;
        this.auditLogRepository = auditLogRepository;
    }

    
    /* ================================================================
     * METHOD: getAllUsers
     * DESCRIPTION:
     * Retrieves all users from AuthService for admin monitoring.
     * ================================================================ */
    public List<UserResponse> getAllUsers() {

        logger.debug("Fetching users from AuthService");

        List<UserResponse> users = authServiceClient.getAllUsers();

        logger.info("Total users fetched: {}", users.size());

        return users;
    }

    
    /* ================================================================
     * METHOD: deleteUser
     * DESCRIPTION:
     * Deletes a user by ID and records the action in audit logs.
     * ================================================================ */
    public void deleteUser(Long id, Long adminId) {

        logger.info("Admin [{}] deleting user [{}]", adminId, id);

        authServiceClient.deleteUser(id);

        AuditLog log = new AuditLog("DELETE_USER", "admin:" + adminId, "Deleted user ID: " + id);
        auditLogRepository.save(log);

        logger.info("User [{}] deleted and audit log saved", id);
    }

    
    /* ================================================================
     * METHOD: banUser
     * DESCRIPTION:
     * Bans a user, invalidates their session, and records the action.
     * ================================================================ */
    public void banUser(Long id, Long adminId) {

        logger.info("Admin [{}] banning user [{}]", adminId, id);

        // Prevent admin from banning themselves
        if (id.equals(adminId)) {
            logger.warn("Admin [{}] tried to ban themselves", adminId);
            throw new IllegalArgumentException("Admin cannot ban themselves");
        }

        authServiceClient.banUser(id);

        /*
         * Attempt to invalidate user token.
         * If it fails, log warning but continue execution.
         */
        try {
            authServiceClient.invalidateToken(id);
            logger.debug("Token invalidated for user [{}]", id);
        } catch (Exception e) {
            logger.warn("Token invalidation failed for user [{}]: {}", id, e.getMessage());
        }

        AuditLog log = new AuditLog("BAN_USER", "admin:" + adminId, "Banned user ID: " + id);
        auditLogRepository.save(log);

        logger.info("User [{}] banned and audit log saved", id);
    }

    
    /* ================================================================
     * METHOD: unbanUser
     * DESCRIPTION:
     * Restores access for a banned user and logs the action.
     * ================================================================ */
    public void unbanUser(Long id, Long adminId) {

        logger.info("Admin [{}] unbanning user [{}]", adminId, id);

        authServiceClient.unbanUser(id);

        AuditLog log = new AuditLog("UNBAN_USER", "admin:" + adminId, "Unbanned user ID: " + id);
        auditLogRepository.save(log);

        logger.info("User [{}] unbanned and audit log saved", id);
    }

    
    /* ================================================================
     * METHOD: getAllJobs
     * DESCRIPTION:
     * Retrieves all jobs from JobService for admin-level access.
     * ================================================================ */
    public List<JobResponse> getAllJobs() {

        logger.debug("Fetching jobs from JobService");

        List<JobResponse> jobs = adminJobClient.getAllJobs();

        logger.info("Total jobs fetched: {}", jobs.size());

        return jobs;
    }

    
    /* ================================================================
     * METHOD: deleteJob
     * DESCRIPTION:
     * Deletes a job by ID and records the action in audit logs.
     * ================================================================ */
    public void deleteJob(Long id, Long adminId) {

        logger.info("Admin [{}] deleting job [{}]", adminId, id);

        adminJobClient.deleteJob(id);

        AuditLog log = new AuditLog("DELETE_JOB", "admin:" + adminId, "Deleted job ID: " + id);
        auditLogRepository.save(log);

        logger.info("Job [{}] deleted and audit log saved", id);
    }
    

    /* ================================================================
     * METHOD: getReport
     * DESCRIPTION:
     * Generates a comprehensive platform report including users,
     * jobs, and application statistics.
     * ================================================================ */
    public PlatformReport getReport() {

        logger.info("Generating platform report");

        List<UserResponse> users = authServiceClient.getAllUsers();
        logger.debug("Users fetched: {}", users.size());

        List<JobResponse> jobs = adminJobClient.getAllJobs();
        logger.debug("Jobs fetched: {}", jobs.size());

        ApplicationStats stats = adminAppClient.getStats();
        logger.debug("Application stats fetched");

        PlatformReport report = new PlatformReport();
        report.setTotalUsers(users.size());
        report.setTotalJobs(jobs.size());
        report.setApplicationStats(stats);
        report.setUsers(users);
        report.setJobs(jobs);

        logger.info("Report ready: users={}, jobs={}", users.size(), jobs.size());

        return report;
    }

    
    /* ================================================================
     * METHOD: getAuditLogs
     * DESCRIPTION:
     * Retrieves all audit logs for admin monitoring and tracking.
     * ================================================================ */
    public List<AuditLog> getAuditLogs() {

        logger.debug("Fetching audit logs");

        List<AuditLog> logs = auditLogRepository.findAll();

        logger.info("Total logs fetched: {}", logs.size());

        return logs;
    }
}