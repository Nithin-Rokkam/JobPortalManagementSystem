package com.capg.jobportal.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capg.jobportal.dto.JobResponse;
import com.capg.jobportal.dto.PlatformReport;
import com.capg.jobportal.dto.UserResponse;
import com.capg.jobportal.exception.AccessDeniedException;
import com.capg.jobportal.model.AuditLog;
import com.capg.jobportal.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AdminService adminService;
	 
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ─── Helper: enforce ADMIN role ───────────────────────────────────
    private void assertAdmin(String role) {
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            throw new AccessDeniedException("Access denied. ADMIN role required.");
        }
    }

    // ─── User Management ─────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        List<UserResponse> users = adminService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    
 
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        adminService.deleteUser(id, adminId);
        return new ResponseEntity<>(Map.of("message", "User deleted successfully"), HttpStatus.OK);
    }
 
    
    @PutMapping("/users/{id}/ban")
    public ResponseEntity<Map<String, String>> banUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        adminService.banUser(id, adminId);
        return new ResponseEntity<>(Map.of("message", "User banned successfully"), HttpStatus.OK);
    }
 
    
    @PutMapping("/users/{id}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        adminService.unbanUser(id, adminId);
        return new ResponseEntity<>(Map.of("message", "User unbanned successfully"), HttpStatus.OK);
    }

    // ─── Job Management ──────────────────────────────────────────────

    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponse>> getAllJobs(
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        List<JobResponse> jobs = adminService.getAllJobs();
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }
    
 
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        adminService.deleteJob(id, adminId);
        return new ResponseEntity<>(Map.of("message", "Job deleted successfully"), HttpStatus.OK);
    }

    // ─── Reports ─────────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<PlatformReport> getReport(
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        PlatformReport report = adminService.getReport();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    // ─── Audit Logs ──────────────────────────────────────────────────

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @RequestHeader("X-User-Role") String role) {
    	assertAdmin(role);
        List<AuditLog> logs = adminService.getAuditLogs();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }
}