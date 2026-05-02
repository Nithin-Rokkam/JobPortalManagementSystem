package com.capg.jobportal.test.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.capg.jobportal.security.JwtUtil;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set required @Value fields via reflection
        ReflectionTestUtils.setField(jwtUtil, "secret", "ThisIsAVeryLongSecretKeyForHS256AlgorithmThatMustBeAtLeast32BytesLong");
        ReflectionTestUtils.setField(jwtUtil, "accessExpiryMs", 3600000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiryMs", 604800000L);
    }

    @Test
    void generateAccessToken_returnsNonNullToken() {
        String token = jwtUtil.generateAccessToken(1L, "JOB_SEEKER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_returnsNonNullToken() {
        String token = jwtUtil.generateRefreshToken();
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUserId_fromValidToken() {
        String token = jwtUtil.generateAccessToken(42L, "RECRUITER");
        String userId = jwtUtil.extractUserId(token);
        assertEquals("42", userId);
    }

    @Test
    void extractRole_fromValidToken() {
        String token = jwtUtil.generateAccessToken(1L, "ADMIN");
        String role = jwtUtil.extractRole(token);
        assertEquals("ADMIN", role);
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateAccessToken(1L, "JOB_SEEKER");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // Set very short expiry to create an expired token
        ReflectionTestUtils.setField(jwtUtil, "accessExpiryMs", 1L);
        String token = jwtUtil.generateAccessToken(1L, "JOB_SEEKER");
        // Token should be expired almost immediately
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        assertFalse(jwtUtil.isTokenValid(token));
        // Restore normal expiry
        ReflectionTestUtils.setField(jwtUtil, "accessExpiryMs", 3600000L);
    }

    @Test
    void extractAllClaims_validToken() {
        String token = jwtUtil.generateAccessToken(5L, "RECRUITER");
        var claims = jwtUtil.extractAllClaims(token);
        assertEquals("5", claims.getSubject());
        assertEquals("RECRUITER", claims.get("role", String.class));
    }
}
