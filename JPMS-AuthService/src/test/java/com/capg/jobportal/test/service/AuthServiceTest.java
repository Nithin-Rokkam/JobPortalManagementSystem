package com.capg.jobportal.test.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

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

        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Intercept TransactionSynchronizationManager and immediately invoke afterCommit()
        // so the lambda body (RabbitMQ publish) is executed and covered by JaCoCo
        try (MockedStatic<TransactionSynchronizationManager> txMock =
                mockStatic(TransactionSynchronizationManager.class)) {
            txMock.when(() -> TransactionSynchronizationManager
                    .registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        TransactionSynchronization sync = inv.getArgument(0);
                        sync.afterCommit();
                        return null;
                    });

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            assertTrue(response.getMessage().contains("OTP sent"));
            verify(userRepository).saveAndFlush(any(User.class));
            verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("registration.otp"), (Object) any(Object.class));
        }
    }

    @Test
    void register_adminRole_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setRole(Role.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("nithin@gmail.com");
        request.setRole(Role.JOB_SEEKER);

        // Existing user is ACTIVE (already verified) — should reject
        testUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(java.util.Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).saveAndFlush(any(User.class));
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

    @Test
    void refresh_bannedUser_throwsException() {
        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findByRefreshToken("existing-refresh-token")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> authService.refresh("existing-refresh-token"));
    }

    // ─── Logout Tests ────────────────────────────────────────────────

    @Test
    void logout_success() {
        when(userRepository.findByRefreshToken("existing-refresh-token")).thenReturn(Optional.of(testUser));

        authService.logout("existing-refresh-token");

        assertNull(testUser.getRefreshToken());
        verify(userRepository).save(testUser);
    }

    @Test
    void logout_invalidToken_throwsException() {
        when(userRepository.findByRefreshToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.logout("invalid-token"));
    }

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
        verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("password.reset"), (Object) any(Object.class));
    }

    @Test
    void forgotPassword_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.forgotPassword("unknown@gmail.com"));
        verify(rabbitTemplate, never()).convertAndSend(eq("jobportal.exchange"), eq("password.reset"), (Object) any(Object.class));
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

    // ─── Verify Registration OTP ─────────────────────────────────────

    @Test
    void verifyRegistrationOtp_success() {
        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        testUser.setEmailVerificationOtp("482931");
        testUser.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        authService.verifyRegistrationOtp("nithin@gmail.com", "482931");

        assertEquals(UserStatus.ACTIVE, testUser.getStatus());
        assertNull(testUser.getEmailVerificationOtp());
        assertNull(testUser.getEmailVerificationExpiry());
        verify(userRepository).save(testUser);
    }

    @Test
    void verifyRegistrationOtp_bypassCode_success() {
        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        testUser.setEmailVerificationOtp("482931");
        testUser.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        // Correct OTP should activate the account
        authService.verifyRegistrationOtp("nithin@gmail.com", "482931");

        assertEquals(UserStatus.ACTIVE, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    void verifyRegistrationOtp_alreadyActive_throwsException() {
        testUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> authService.verifyRegistrationOtp("nithin@gmail.com", "123456"));
    }

    @Test
    void verifyRegistrationOtp_wrongOtp_throwsException() {
        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        testUser.setEmailVerificationOtp("482931");
        testUser.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> authService.verifyRegistrationOtp("nithin@gmail.com", "000001"));
    }

    @Test
    void verifyRegistrationOtp_expiredOtp_throwsException() {
        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        testUser.setEmailVerificationOtp("482931");
        testUser.setEmailVerificationExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> authService.verifyRegistrationOtp("nithin@gmail.com", "482931"));
    }

    @Test
    void verifyRegistrationOtp_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.verifyRegistrationOtp("unknown@gmail.com", "123456"));
    }

    // ─── Resend Registration OTP ─────────────────────────────────────

    @Test
    void resendRegistrationOtp_success() {
        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        try (MockedStatic<TransactionSynchronizationManager> txMock =
                mockStatic(TransactionSynchronizationManager.class)) {
            txMock.when(() -> TransactionSynchronizationManager
                    .registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        TransactionSynchronization sync = inv.getArgument(0);
                        sync.afterCommit();
                        return null;
                    });

            authService.resendRegistrationOtp("nithin@gmail.com");

            assertNotNull(testUser.getEmailVerificationOtp());
            assertEquals(6, testUser.getEmailVerificationOtp().length());
            verify(userRepository).saveAndFlush(testUser);
            verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("registration.otp"), (Object) any(Object.class));
        }
    }

    @Test
    void resendRegistrationOtp_alreadyVerified_throwsException() {
        testUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class,
                () -> authService.resendRegistrationOtp("nithin@gmail.com"));
    }

    @Test
    void resendRegistrationOtp_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.resendRegistrationOtp("unknown@gmail.com"));
    }

    // ─── Register — Re-registration of unverified account ───────────

    @Test
    void register_reRegistration_pendingVerification_updatesAndResends() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Nithin Updated");
        request.setEmail("nithin@gmail.com");
        request.setPassword("newpassword123");
        request.setRole(Role.JOB_SEEKER);

        testUser.setStatus(UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmail("nithin@gmail.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new_encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        try (MockedStatic<TransactionSynchronizationManager> txMock =
                mockStatic(TransactionSynchronizationManager.class)) {
            txMock.when(() -> TransactionSynchronizationManager
                    .registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        TransactionSynchronization sync = inv.getArgument(0);
                        sync.afterCommit();
                        return null;
                    });

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            assertTrue(response.getMessage().contains("OTP sent"));
            verify(userRepository).saveAndFlush(testUser);
            verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("registration.otp"), (Object) any(Object.class));
        }
    }

    @Test
    void register_recruiterWithCompanyName_success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Recruiter");
        request.setEmail("recruiter@gmail.com");
        request.setPassword("password123");
        request.setRole(Role.RECRUITER);
        request.setCompanyName("Acme Ltd");

        when(userRepository.findByEmail("recruiter@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        try (MockedStatic<TransactionSynchronizationManager> txMock =
                mockStatic(TransactionSynchronizationManager.class)) {
            txMock.when(() -> TransactionSynchronizationManager
                    .registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        TransactionSynchronization sync = inv.getArgument(0);
                        sync.afterCommit();
                        return null;
                    });

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            assertTrue(response.getMessage().contains("OTP sent"));
            verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("registration.otp"), (Object) any(Object.class));
        }
    }

    @Test
    void register_reRegistration_recruiterWithCompanyName() {
        // Covers line 106: existingUser.setCompanyName() in re-registration path
        RegisterRequest request = new RegisterRequest();
        request.setName("Recruiter Updated");
        request.setEmail("recruiter@gmail.com");
        request.setPassword("password123");
        request.setRole(Role.RECRUITER);
        request.setCompanyName("New Company Ltd");

        User existingRecruiter = new User();
        existingRecruiter.setId(2L);
        existingRecruiter.setEmail("recruiter@gmail.com");
        existingRecruiter.setStatus(UserStatus.PENDING_VERIFICATION);
        existingRecruiter.setRole(Role.RECRUITER);

        when(userRepository.findByEmail("recruiter@gmail.com")).thenReturn(Optional.of(existingRecruiter));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(existingRecruiter);

        try (MockedStatic<TransactionSynchronizationManager> txMock =
                mockStatic(TransactionSynchronizationManager.class)) {
            txMock.when(() -> TransactionSynchronizationManager
                    .registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(inv -> {
                        TransactionSynchronization sync = inv.getArgument(0);
                        sync.afterCommit();
                        return null;
                    });

            AuthResponse response = authService.register(request);

            assertNotNull(response);
            assertEquals("New Company Ltd", existingRecruiter.getCompanyName());
            verify(rabbitTemplate).convertAndSend(eq("jobportal.exchange"), eq("registration.otp"), (Object) any(Object.class));
        }
    }

    // ─── Update Company Name ─────────────────────────────────────────

    @Test
    void updateCompanyName_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        authService.updateCompanyName(1L, "New Company");

        assertEquals("New Company", testUser.getCompanyName());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCompanyName_nullName_setsNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        authService.updateCompanyName(1L, null);

        assertNull(testUser.getCompanyName());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateCompanyName_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.updateCompanyName(99L, "Company"));
    }

    // ─── Update Selected By Company ──────────────────────────────────

    @Test
    void updateSelectedByCompany_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        authService.updateSelectedByCompany(1L, "Acme Ltd");

        assertEquals("Acme Ltd", testUser.getSelectedByCompany());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateSelectedByCompany_userNotFound_doesNotThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Should not throw — method silently skips if user not found
        assertDoesNotThrow(() -> authService.updateSelectedByCompany(99L, "Acme Ltd"));
        verify(userRepository, never()).save(any());
    }
}
