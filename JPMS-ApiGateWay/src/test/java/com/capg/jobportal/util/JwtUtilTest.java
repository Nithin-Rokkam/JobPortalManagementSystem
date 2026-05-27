package com.capg.jobportal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "ThisIsAVeryLongSecretKeyForHS256AlgorithmThatMustBeAtLeast32BytesLong";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    private String generateToken(Long userId, String role, long expiryMs) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractAllClaims_validToken() {
        String token = generateToken(1L, "ADMIN", 3600000L);
        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals("1", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = generateToken(1L, "JOB_SEEKER", 3600000L);
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String token = generateToken(1L, "JOB_SEEKER", -1000L);
        assertFalse(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }

    @Test
    void extractUserId() {
        String token = generateToken(42L, "RECRUITER", 3600000L);
        assertEquals("42", jwtUtil.extractUserId(token));
    }

    @Test
    void extractRole() {
        String token = generateToken(1L, "ADMIN", 3600000L);
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }
}
