package com.capg.jobportal.test.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.InternalAuthController;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.security.SecurityConfig;
import com.capg.jobportal.service.AuthService;

@WebMvcTest(
    controllers = InternalAuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
class InternalAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void getAllUsers_returnsOk() throws Exception {
        UserProfileResponse user = new UserProfileResponse();
        user.setId(1L);
        user.setName("Nithin");
        user.setEmail("nithin@gmail.com");
        user.setRole("JOB_SEEKER");
        when(authService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/internal/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Nithin"));
    }

    @Test
    void deleteUser_returnsOk() throws Exception {
        doNothing().when(authService).deleteUser(1L);

        mockMvc.perform(delete("/api/internal/users/1"))
                .andExpect(status().isOk());

        verify(authService).deleteUser(1L);
    }

    @Test
    void banUser_returnsOk() throws Exception {
        doNothing().when(authService).updateUserStatus(1L, "BANNED");

        mockMvc.perform(put("/api/internal/users/1/ban"))
                .andExpect(status().isOk());

        verify(authService).updateUserStatus(1L, "BANNED");
    }

    @Test
    void unbanUser_returnsOk() throws Exception {
        doNothing().when(authService).updateUserStatus(1L, "ACTIVE");

        mockMvc.perform(put("/api/internal/users/1/unban"))
                .andExpect(status().isOk());

        verify(authService).updateUserStatus(1L, "ACTIVE");
    }

    @Test
    void invalidateToken_returnsOk() throws Exception {
        doNothing().when(authService).invalidateTokenByUserId(1L);

        mockMvc.perform(put("/api/internal/users/1/invalidate-token"))
                .andExpect(status().isOk());

        verify(authService).invalidateTokenByUserId(1L);
    }

    @Test
    void getJobSeekerEmails_returnsOk() throws Exception {
        when(authService.getJobSeekerEmails()).thenReturn(List.of("seeker@test.com"));

        mockMvc.perform(get("/api/internal/users/job-seeker-emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("seeker@test.com"));
    }

    @Test
    void getUserInfo_returnsOk() throws Exception {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(1L);
        profile.setName("Nithin");
        profile.setEmail("nithin@gmail.com");
        profile.setRole("JOB_SEEKER");
        when(authService.getProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/internal/users/1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nithin"))
                .andExpect(jsonPath("$.email").value("nithin@gmail.com"));
    }
}
