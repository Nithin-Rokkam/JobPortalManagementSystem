package com.capg.jobportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capg.jobportal.client.AdminAppClient;
import com.capg.jobportal.client.AdminJobClient;
import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.*;
import com.capg.jobportal.model.AuditLog;
import com.capg.jobportal.repository.AuditLogRepository;

import java.util.List;

@Service
public class AdminService {

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

    // ─── User Operations ─────────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return authServiceClient.getAllUsers();
    }
    
 
    public void deleteUser(Long id, Long adminId) {
        authServiceClient.deleteUser(id);
        AuditLog log = new AuditLog("DELETE_USER", "admin:" + adminId, "Deleted user ID: " + id);
        auditLogRepository.save(log);
    }
    
 
    public void banUser(Long id, Long adminId) {
        if (id.equals(adminId)) {
            throw new IllegalArgumentException("Admin cannot ban themselves");
        }
        authServiceClient.banUser(id);
        try {
            authServiceClient.invalidateToken(id);
        } catch (Exception e) {
            System.out.println("Could not invalidate token for user " + id + ": " + e.getMessage());
        }
        AuditLog log = new AuditLog("BAN_USER", "admin:" + adminId, "Banned user ID: " + id);
        auditLogRepository.save(log);
    }
    
 
    public void unbanUser(Long id, Long adminId) {
        authServiceClient.unbanUser(id);
        AuditLog log = new AuditLog("UNBAN_USER", "admin:" + adminId, "Unbanned user ID: " + id);
        auditLogRepository.save(log);
    }

    // ─── Job Operations ──────────────────────────────────────────────

    public List<JobResponse> getAllJobs() {
        return adminJobClient.getAllJobs();
    }
 
    
    public void deleteJob(Long id, Long adminId) {
        adminJobClient.deleteJob(id);
        AuditLog log = new AuditLog("DELETE_JOB", "admin:" + adminId, "Deleted job ID: " + id);
        auditLogRepository.save(log);
    }

    // ─── Platform Report ─────────────────────────────────────────────

    public PlatformReport getReport() {
        List<UserResponse> users = authServiceClient.getAllUsers();
        List<JobResponse> jobs = adminJobClient.getAllJobs();
        ApplicationStats stats = adminAppClient.getStats();
 
        PlatformReport report = new PlatformReport();
        report.setTotalUsers(users.size());
        report.setTotalJobs(jobs.size());
        report.setApplicationStats(stats);
        report.setUsers(users);
        report.setJobs(jobs);
 
        return report;
    }
    
    // ─── Audit Logs ──────────────────────────────────────────────────

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}
