package com.capg.jobportal.test.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
<<<<<<< HEAD
=======
import org.springframework.mock.web.MockMultipartFile;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.AuthController;
import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.security.SecurityConfig;
import com.capg.jobportal.service.AuthService;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = SecurityConfig.class
    )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void register_returnsOk() throws Exception {
        AuthResponse response = new AuthResponse("Registration successful. Please login.");
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Nithin\",\"email\":\"nithin@gmail.com\",\"password\":\"password123\",\"role\":\"JOB_SEEKER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful. Please login."));
    }

    @Test
    void login_returnsOk() throws Exception {
        AuthResponse response = new AuthResponse("access-token", "refresh-token", "JOB_SEEKER", 1L, "Nithin", "nithin@gmail.com");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nithin@gmail.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.role").value("JOB_SEEKER"));
    }

    @Test
    void refresh_returnsOk() throws Exception {
        AuthResponse response = new AuthResponse("new-access", "new-refresh", "JOB_SEEKER", 1L, "Nithin", "nithin@gmail.com");
        when(authService.refresh(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"old-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void logout_returnsOk() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"some-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
<<<<<<< HEAD
=======
    void uploadProfilePicture_returnsOk() throws Exception {
        MockMultipartFile picture = new MockMultipartFile(
                "picture", "photo.jpg", "image/jpeg", "fake-image-data".getBytes());
        when(authService.updateProfilePicture(eq(1L), any())).thenReturn("https://cloud.com/pic.jpg");

        mockMvc.perform(multipart("/api/auth/profile/picture")
                .file(picture)
                .header("X-User-Id", "1")
                .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl").value("https://cloud.com/pic.jpg"));
    }

    @Test
    void uploadResume_returnsOk() throws Exception {
        MockMultipartFile resume = new MockMultipartFile(
                "resume", "resume.pdf", "application/pdf", "fake-pdf-data".getBytes());
        when(authService.updateProfileResume(eq(1L), any())).thenReturn("https://cloud.com/resume.pdf");

        mockMvc.perform(multipart("/api/auth/profile/resume")
                .file(resume)
                .header("X-User-Id", "1")
                .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeUrl").value("https://cloud.com/resume.pdf"));
    }

    @Test
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    void getProfile_returnsOk() throws Exception {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(1L);
        profile.setName("Nithin");
        profile.setEmail("nithin@gmail.com");
        profile.setRole("JOB_SEEKER");
        when(authService.getProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/auth/profile")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nithin"))
                .andExpect(jsonPath("$.email").value("nithin@gmail.com"));
    }
<<<<<<< HEAD
=======

    @Test
    void forgotPassword_returnsOk() throws Exception {
        doNothing().when(authService).forgotPassword(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nithin@gmail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email"));
    }

    @Test
    void resetPassword_returnsOk() throws Exception {
        doNothing().when(authService).resetPassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nithin@gmail.com\",\"otp\":\"123456\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
