package com.capg.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.capg.jobportal.dao.UserRepository;
import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.LoginRequest;
import com.capg.jobportal.dto.RegisterRequest;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.entity.User;
import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;
import com.capg.jobportal.exception.ResourceNotFoundException;
import com.capg.jobportal.exception.UserAlreadyExistsException;
import com.capg.jobportal.security.JwtUtil;
import com.capg.jobportal.util.CloudinaryUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CloudinaryUtil cloudinaryUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("encoded_password");
        testUser.setRole(Role.JOB_SEEKER);
        testUser.setPhone("1234567890");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRefreshToken("existing-refresh-token");
    }

    // ─── Register Tests ──────────────────────────────────────────────

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRole(Role.JOB_SEEKER);
        request.setPhone("1234567890");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("Registration successful. Please login.", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_adminRole_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setRole(Role.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");
        request.setRole(Role.JOB_SEEKER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // ─── Login Tests ─────────────────────────────────────────────────

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateAccessToken(1L, "JOB_SEEKER")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken()).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("JOB_SEEKER", response.getRole());
        assertEquals(1L, response.getUserId());
        verify(userRepository).save(testUser);
    }

    @Test
    void login_invalidEmail_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_bannedUser_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    // ─── Refresh Tests ───────────────────────────────────────────────

    @Test
    void refresh_success() {
        when(userRepository.findByRefreshToken("existing-refresh-token")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(1L, "JOB_SEEKER")).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh("existing-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(userRepository).save(testUser);
    }

    @Test
    void refresh_invalidToken_throwsException() {
        when(userRepository.findByRefreshToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.refresh("invalid-token"));
    }

    // ─── Logout Tests ────────────────────────────────────────────────

    @Test
    void logout_success() {
        when(userRepository.findByRefreshToken("existing-refresh-token")).thenReturn(Optional.of(testUser));

        authService.logout("existing-refresh-token");

        assertNull(testUser.getRefreshToken());
        verify(userRepository).save(testUser);
    }

    // ─── Profile Tests ───────────────────────────────────────────────

    @Test
    void getProfile_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileResponse response = authService.getProfile(1L);

        assertNotNull(response);
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getProfile(99L));
    }

    // ─── Delete / Status Tests ───────────────────────────────────────

    @Test
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        authService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.deleteUser(99L));
    }
}
