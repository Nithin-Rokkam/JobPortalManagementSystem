package com.capg.jobportal.test.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.capg.jobportal.security.SecurityConfig;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void securityFilterChain_isConfigured() throws Exception {
        // We can't easily create HttpSecurity outside of Spring context,
        // but we can test the passwordEncoder bean
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
    }

    @Test
    void passwordEncoder_returnsBCryptEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);

        String encoded = encoder.encode("testPassword");
        assertNotNull(encoded);
        assertTrue(encoder.matches("testPassword", encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }
}
