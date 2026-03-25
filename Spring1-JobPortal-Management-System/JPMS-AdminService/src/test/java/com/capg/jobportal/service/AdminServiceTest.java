package com.capg.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capg.jobportal.client.AdminAppClient;
import com.capg.jobportal.client.AdminJobClient;
import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.dto.JobResponse;
import com.capg.jobportal.dto.PlatformReport;
import com.capg.jobportal.dto.UserResponse;
import com.capg.jobportal.model.AuditLog;
import com.capg.jobportal.repository.AuditLogRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private AdminJobClient adminJobClient;

    @Mock
    private AdminAppClient adminAppClient;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AdminService adminService;

    // ─── User Operations ─────────────────────────────────────────────

    @Test
    void getAllUsers_success() {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setName("John");
        when(authServiceClient.getAllUsers()).thenReturn(Arrays.asList(user));

        List<UserResponse> result = adminService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
        verify(authServiceClient).getAllUsers();
    }

    @Test
    void deleteUser_success() {
        doNothing().when(authServiceClient).deleteUser(1L);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        adminService.deleteUser(1L, 99L);

        verify(authServiceClient).deleteUser(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void banUser_success() {
        doNothing().when(authServiceClient).banUser(1L);
        doNothing().when(authServiceClient).invalidateToken(1L);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        adminService.banUser(1L, 99L);

        verify(authServiceClient).banUser(1L);
        verify(authServiceClient).invalidateToken(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void banUser_self_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> adminService.banUser(99L, 99L));
        verify(authServiceClient, never()).banUser(anyLong());
    }

    @Test
    void unbanUser_success() {
        doNothing().when(authServiceClient).unbanUser(1L);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        adminService.unbanUser(1L, 99L);

        verify(authServiceClient).unbanUser(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // ─── Job Operations ──────────────────────────────────────────────

    @Test
    void getAllJobs_success() {
        JobResponse job = new JobResponse();
        job.setId(1L);
        job.setTitle("Java Dev");
        when(adminJobClient.getAllJobs()).thenReturn(Arrays.asList(job));

        List<JobResponse> result = adminService.getAllJobs();

        assertEquals(1, result.size());
        assertEquals("Java Dev", result.get(0).getTitle());
    }

    @Test
    void deleteJob_success() {
        doNothing().when(adminJobClient).deleteJob(1L);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        adminService.deleteJob(1L, 99L);

        verify(adminJobClient).deleteJob(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // ─── Reports ─────────────────────────────────────────────────────

    @Test
    void getReport_success() {
        UserResponse user = new UserResponse();
        user.setId(1L);
        JobResponse job = new JobResponse();
        job.setId(1L);
        ApplicationStats stats = new ApplicationStats();
        stats.setTotalApplications(10);

        when(authServiceClient.getAllUsers()).thenReturn(Arrays.asList(user));
        when(adminJobClient.getAllJobs()).thenReturn(Arrays.asList(job));
        when(adminAppClient.getStats()).thenReturn(stats);

        PlatformReport report = adminService.getReport();

        assertEquals(1, report.getTotalUsers());
        assertEquals(1, report.getTotalJobs());
        assertEquals(10, report.getApplicationStats().getTotalApplications());
    }

    // ─── Audit Logs ──────────────────────────────────────────────────

    @Test
    void getAuditLogs_success() {
        AuditLog log = new AuditLog("BAN_USER", "admin:1", "Banned user 5");
        when(auditLogRepository.findAll()).thenReturn(Arrays.asList(log));

        List<AuditLog> result = adminService.getAuditLogs();

        assertEquals(1, result.size());
        assertEquals("BAN_USER", result.get(0).getAction());
    }
}
