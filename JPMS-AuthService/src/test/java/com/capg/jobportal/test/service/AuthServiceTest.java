package com.capg.jobportal.test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

<<<<<<< HEAD
=======
import java.io.IOException;
import java.time.LocalDateTime;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
=======
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.multipart.MultipartFile;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

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
import com.capg.jobportal.service.AuthService;
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

<<<<<<< HEAD
    @InjectMocks
    private AuthService authService; // all Mock-dependencies are injected into this service
=======
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Nithin");
        testUser.setEmail("nithin@gmail.com");
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
        request.setName("Nithin");
        request.setEmail("nithin@gmail.com");
        request.setPassword("password123");
        request.setRole(Role.JOB_SEEKER);
        request.setPhone("1234567890");

<<<<<<< HEAD
        when(userRepository.existsByEmail("nithin@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
=======
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

        AuthResponse response = authService.register(request);

        assertNotNull(response);
<<<<<<< HEAD
        assertEquals("Registration successful. Please login.", response.getMessage());
        verify(userRepository).save(any(User.class));
=======
        assertTrue(response.getMessage().contains("OTP sent"));
        verify(userRepository).saveAndFlush(any(User.class));
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    }

    @Test
    void register_adminRole_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setRole(Role.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
<<<<<<< HEAD
        verify(userRepository, never()).save(any(User.class));
=======
        verify(userRepository, never()).saveAndFlush(any(User.class));
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("nithin@gmail.com");
        request.setRole(Role.JOB_SEEKER);

<<<<<<< HEAD
        when(userRepository.existsByEmail("nithin@gmail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
=======
        // Existing user is ACTIVE (already verified) — should reject
        testUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(java.util.Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).saveAndFlush(any(User.class));
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    }

    // ─── Login Tests ─────────────────────────────────────────────────

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nithin@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));
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
        request.setEmail("nithin@gmail.com");
        request.setPassword("password123");

        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
<<<<<<< HEAD
=======
    void login_pendingVerification_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nithin@gmail.com");
        request.setPassword("password123");

        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("EMAIL_NOT_VERIFIED", ex.getMessage());
    }

    @Test
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nithin@gmail.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));
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

<<<<<<< HEAD
=======
    @Test
    void refresh_bannedUser_throwsException() {
        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findByRefreshToken("existing-refresh-token")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.refresh("existing-refresh-token"));
    }

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    // ─── Logout Tests ────────────────────────────────────────────────

    @Test
    void logout_success() {
        when(userRepository.findByRefreshToken("existing-refresh-token")).thenReturn(Optional.of(testUser));

        authService.logout("existing-refresh-token");

        assertNull(testUser.getRefreshToken());
        verify(userRepository).save(testUser);
    }

<<<<<<< HEAD
=======
    @Test
    void logout_invalidToken_throwsException() {
        when(userRepository.findByRefreshToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.logout("invalid-token"));
    }

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    // ─── Profile Tests ───────────────────────────────────────────────

    @Test
    void getProfile_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileResponse response = authService.getProfile(1L);

        assertNotNull(response);
        assertEquals("Nithin", response.getName());
        assertEquals("nithin@gmail.com", response.getEmail());
    }

    @Test
    void getProfile_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getProfile(99L));
    }

<<<<<<< HEAD
=======
    // ─── Update Profile Picture ──────────────────────────────────────

    @Test
    void updateProfilePicture_success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cloudinaryUtil.uploadProfilePicture(mockFile)).thenReturn("https://cloudinary.com/pic.jpg");

        String url = authService.updateProfilePicture(1L, mockFile);

        assertEquals("https://cloudinary.com/pic.jpg", url);
        assertEquals("https://cloudinary.com/pic.jpg", testUser.getProfilePictureUrl());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfilePicture_userNotFound_throwsException() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.updateProfilePicture(99L, mockFile));
    }

    // ─── Update Profile Resume ───────────────────────────────────────

    @Test
    void updateProfileResume_success() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cloudinaryUtil.uploadResume(mockFile)).thenReturn("https://cloudinary.com/resume.pdf");

        String url = authService.updateProfileResume(1L, mockFile);

        assertEquals("https://cloudinary.com/resume.pdf", url);
        assertEquals("https://cloudinary.com/resume.pdf", testUser.getResumeUrl());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfileResume_userNotFound_throwsException() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.updateProfileResume(99L, mockFile));
    }

    // ─── Get All Users ───────────────────────────────────────────────

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        List<UserProfileResponse> result = authService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Nithin", result.get(0).getName());
    }

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
=======

    // ─── Update User Status ──────────────────────────────────────────

    @Test
    void updateUserStatus_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        authService.updateUserStatus(1L, "BANNED");

        assertEquals(UserStatus.BANNED, testUser.getStatus());
        assertNull(testUser.getRefreshToken());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserStatus_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.updateUserStatus(99L, "BANNED"));
    }

    // ─── Invalidate Token ────────────────────────────────────────────

    @Test
    void invalidateTokenByUserId_userFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        authService.invalidateTokenByUserId(1L);

        assertNull(testUser.getRefreshToken());
        verify(userRepository).save(testUser);
    }

    @Test
    void invalidateTokenByUserId_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        authService.invalidateTokenByUserId(99L);

        verify(userRepository, never()).save(any());
    }

    // ─── Get Job Seeker Emails ───────────────────────────────────────

    @Test
    void getJobSeekerEmails_success() {
        User activeSeeker = new User();
        activeSeeker.setEmail("seeker@test.com");
        activeSeeker.setRole(Role.JOB_SEEKER);
        activeSeeker.setStatus(UserStatus.ACTIVE);

        User bannedSeeker = new User();
        bannedSeeker.setEmail("banned@test.com");
        bannedSeeker.setRole(Role.JOB_SEEKER);
        bannedSeeker.setStatus(UserStatus.BANNED);

        when(userRepository.findByRole(Role.JOB_SEEKER))
                .thenReturn(Arrays.asList(activeSeeker, bannedSeeker));

        List<String> emails = authService.getJobSeekerEmails();

        assertEquals(1, emails.size());
        assertEquals("seeker@test.com", emails.get(0));
    }

    // ─── Forgot Password Tests ──────────────────────────────────────

    @Test
    void forgotPassword_success() {
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        authService.forgotPassword("nithin@gmail.com");

        assertNotNull(testUser.getResetPasswordOtp());
        assertEquals(6, testUser.getResetPasswordOtp().length());
        assertNotNull(testUser.getOtpExpiryTime());
        verify(userRepository).save(testUser);
        verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("password.reset"), (Object) any());
    }

    @Test
    void forgotPassword_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.forgotPassword("unknown@gmail.com"));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    // ─── Reset Password Tests ───────────────────────────────────────

    @Test
    void resetPassword_success() {
        testUser.setResetPasswordOtp("123456");
        testUser.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded_new_password");

        authService.resetPassword("nithin@gmail.com", "123456", "newPassword123");

        assertEquals("encoded_new_password", testUser.getPassword());
        assertNull(testUser.getResetPasswordOtp());
        assertNull(testUser.getOtpExpiryTime());
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_invalidOtp_throwsException() {
        testUser.setResetPasswordOtp("123456");
        testUser.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword("nithin@gmail.com", "999999", "newPassword123"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_expiredOtp_throwsException() {
        testUser.setResetPasswordOtp("123456");
        testUser.setOtpExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword("nithin@gmail.com", "123456", "newPassword123"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.resetPassword("unknown@gmail.com", "123456", "newPassword123"));
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
