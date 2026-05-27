package com.capg.jobportal.test.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.AdminController;
import com.capg.jobportal.dto.PlatformReport;
import com.capg.jobportal.service.AdminService;

@WebMvcTest(
    controllers = AdminController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.capg.jobportal.security.SecurityConfig.class
    )
)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    void getAllUsers_returns200() throws Exception {
        when(adminService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/users").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_returns200() throws Exception {
        doNothing().when(adminService).deleteUser(anyLong(), anyLong());
        mockMvc.perform(delete("/api/admin/users/1").header("X-User-Role", "ADMIN").header("X-User-Id", "999"))
                .andExpect(status().isOk());
    }

    @Test
    void banUser_returns200() throws Exception {
        doNothing().when(adminService).banUser(anyLong(), anyLong());
        mockMvc.perform(put("/api/admin/users/1/ban").header("X-User-Role", "ADMIN").header("X-User-Id", "999"))
                .andExpect(status().isOk());
    }

    @Test
    void unbanUser_returns200() throws Exception {
        doNothing().when(adminService).unbanUser(anyLong(), anyLong());
        mockMvc.perform(put("/api/admin/users/1/unban").header("X-User-Role", "ADMIN").header("X-User-Id", "999"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllJobs_returns200() throws Exception {
        when(adminService.getAllJobs()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/jobs").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJob_returns200() throws Exception {
        doNothing().when(adminService).deleteJob(anyLong(), anyLong());
        mockMvc.perform(delete("/api/admin/jobs/1").header("X-User-Role", "ADMIN").header("X-User-Id", "999"))
                .andExpect(status().isOk());
    }

    @Test
    void getReport_returns200() throws Exception {
        PlatformReport report = new PlatformReport();
        when(adminService.getReport()).thenReturn(report);
        mockMvc.perform(get("/api/admin/reports").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLogs_returns200() throws Exception {
        when(adminService.getAuditLogs()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/audit-logs").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void assertAdmin_nullRole_throwsException() {
        AdminController controller = new AdminController(adminService);
        org.junit.jupiter.api.Assertions.assertThrows(
            com.capg.jobportal.exception.AccessDeniedException.class, 
            () -> controller.getAllUsers(null)
        );
    }

    @Test
    void assertAdmin_userRole_throwsException() {
        AdminController controller = new AdminController(adminService);
        org.junit.jupiter.api.Assertions.assertThrows(
            com.capg.jobportal.exception.AccessDeniedException.class, 
            () -> controller.getAllUsers("USER")
        );
    }

    @Test
    void getAllUsers_nonAdminRole_throwsAccessDenied() throws Exception {
        // When X-User-Role is not ADMIN, assertAdmin throws AccessDeniedException
        // which GlobalExceptionHandler maps to 403
        when(adminService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/users").header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }
}
