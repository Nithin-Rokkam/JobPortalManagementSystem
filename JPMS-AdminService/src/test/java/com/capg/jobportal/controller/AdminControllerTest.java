package com.capg.jobportal.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.dto.PlatformReport;
import com.capg.jobportal.dto.UserResponse;
import com.capg.jobportal.model.AuditLog;
import com.capg.jobportal.service.AdminService;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
// ... existing imports ...

@WebMvcTest(
    controllers = AdminController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    // ─── User Endpoints ──────────────────────────────────────────────

    @Test
    void getAllUsers_admin_returns200() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setName("John");
        when(adminService.getAllUsers()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/api/admin/users")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"));
    }

    @Test
    void getAllUsers_nonAdmin_returnsForbidden() throws Exception {
        // The controller throws AccessDeniedException for non-ADMIN role
        // Expecting 403 Forbidden
        mockMvc.perform(get("/api/admin/users")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_admin_returns200() throws Exception {
        doNothing().when(adminService).deleteUser(1L, 99L);

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Id", "99")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void banUser_admin_returns200() throws Exception {
        doNothing().when(adminService).banUser(1L, 99L);

        mockMvc.perform(put("/api/admin/users/1/ban")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Id", "99")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User banned successfully"));
    }

    // ─── Job Endpoints ───────────────────────────────────────────────

    @Test
    void getAllJobs_admin_returns200() throws Exception {
        when(adminService.getAllJobs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/jobs")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    // ─── Report Endpoint ─────────────────────────────────────────────

    @Test
    void getReport_admin_returns200() throws Exception {
        PlatformReport report = new PlatformReport();
        report.setTotalUsers(5);
        report.setTotalJobs(10);
        when(adminService.getReport()).thenReturn(report);

        mockMvc.perform(get("/api/admin/reports")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(5))
                .andExpect(jsonPath("$.totalJobs").value(10));
    }

    // ─── Audit Logs Endpoint ─────────────────────────────────────────

    @Test
    void getAuditLogs_admin_returns200() throws Exception {
        AuditLog log = new AuditLog("BAN_USER", "admin:1", "Banned user 5");
        when(adminService.getAuditLogs()).thenReturn(Arrays.asList(log));

        mockMvc.perform(get("/api/admin/audit-logs")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("BAN_USER"));
    }
}
