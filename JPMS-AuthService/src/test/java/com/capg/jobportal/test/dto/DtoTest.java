package com.capg.jobportal.test.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.dto.AuthResponse;
import com.capg.jobportal.dto.ErrorResponse;
import com.capg.jobportal.dto.LoginRequest;
import com.capg.jobportal.dto.RegisterRequest;
import com.capg.jobportal.dto.UserInfoResponse;
import com.capg.jobportal.dto.UserProfileResponse;
import com.capg.jobportal.enums.Role;

class DtoTest {

    // ─── AuthResponse ───────────────────────────────────────────────
    @Test
    void authResponse_defaultConstructor() {
        AuthResponse r = new AuthResponse();
        assertNull(r.getMessage());
        assertNull(r.getAccessToken());
    }

    @Test
    void authResponse_messageConstructor() {
        AuthResponse r = new AuthResponse("Success");
        assertEquals("Success", r.getMessage());
    }

    @Test
    void authResponse_fullConstructor() {
        AuthResponse r = new AuthResponse("token", "refresh", "ADMIN", 1L, "Test", "test@test.com");
        assertEquals("token", r.getAccessToken());
        assertEquals("refresh", r.getRefreshToken());
        assertEquals("ADMIN", r.getRole());
        assertEquals(1L, r.getUserId());
        assertEquals("Test", r.getName());
        assertEquals("test@test.com", r.getEmail());
    }

    @Test
    void authResponse_setters() {
        AuthResponse r = new AuthResponse();
        r.setMessage("msg");
        r.setAccessToken("at");
        r.setRefreshToken("rt");
        r.setRole("role");
        r.setUserId(2L);
        r.setName("name");
        r.setEmail("email");

        assertEquals("msg", r.getMessage());
        assertEquals("at", r.getAccessToken());
        assertEquals("rt", r.getRefreshToken());
        assertEquals("role", r.getRole());
        assertEquals(2L, r.getUserId());
        assertEquals("name", r.getName());
        assertEquals("email", r.getEmail());
    }

    // ─── LoginRequest ───────────────────────────────────────────────
    @Test
    void loginRequest_gettersSetters() {
        LoginRequest lr = new LoginRequest();
        lr.setEmail("test@test.com");
        lr.setPassword("pass123");
        assertEquals("test@test.com", lr.getEmail());
        assertEquals("pass123", lr.getPassword());
    }

    // ─── RegisterRequest ────────────────────────────────────────────
    @Test
    void registerRequest_gettersSetters() {
        RegisterRequest rr = new RegisterRequest();
        rr.setName("Test");
        rr.setEmail("test@test.com");
        rr.setPassword("pass123");
        rr.setPhone("1234567890");
        rr.setRole(Role.RECRUITER);
        rr.setCompanyName("Acme Corp");

        assertEquals("Test", rr.getName());
        assertEquals("test@test.com", rr.getEmail());
        assertEquals("pass123", rr.getPassword());
        assertEquals("1234567890", rr.getPhone());
        assertEquals(Role.RECRUITER, rr.getRole());
        assertEquals("Acme Corp", rr.getCompanyName());
    }

    // ─── ErrorResponse ──────────────────────────────────────────────
    @Test
    void errorResponse_constructorAndSetters() {
        ErrorResponse er = new ErrorResponse(400, "Bad Request", "msg");
        assertEquals(400, er.getStatus());
        assertEquals("Bad Request", er.getError());
        assertEquals("msg", er.getMessage());
        assertNotNull(er.getTimestamp());

        LocalDateTime ts = LocalDateTime.now();
        er.setStatus(500);
        er.setError("Internal");
        er.setMessage("new");
        er.setTimestamp(ts);
        assertEquals(500, er.getStatus());
        assertEquals("Internal", er.getError());
        assertEquals("new", er.getMessage());
        assertEquals(ts, er.getTimestamp());
    }

    // ─── UserInfoResponse ───────────────────────────────────────────
    @Test
    void userInfoResponse_gettersSetters() {
        UserInfoResponse u = new UserInfoResponse();
        u.setId(1L);
        u.setName("Test");
        u.setEmail("test@test.com");

        assertEquals(1L, u.getId());
        assertEquals("Test", u.getName());
        assertEquals("test@test.com", u.getEmail());
    }

    // ─── UserProfileResponse ────────────────────────────────────────
    @Test
    void userProfileResponse_gettersSetters() {
        UserProfileResponse p = new UserProfileResponse();
        p.setId(1L);
        p.setName("Test");
        p.setEmail("test@test.com");
        p.setPhone("123");
        p.setRole("ADMIN");
        p.setStatus("ACTIVE");
        p.setProfilePictureUrl("pic.jpg");
        p.setResumeUrl("resume.pdf");
        p.setCompanyName("Acme Corp");
        p.setSelectedByCompany("TechCorp");
        p.setCreatedAt("2026-01-01T00:00:00");

        assertEquals(1L, p.getId());
        assertEquals("Test", p.getName());
        assertEquals("test@test.com", p.getEmail());
        assertEquals("123", p.getPhone());
        assertEquals("ADMIN", p.getRole());
        assertEquals("ACTIVE", p.getStatus());
        assertEquals("pic.jpg", p.getProfilePictureUrl());
        assertEquals("resume.pdf", p.getResumeUrl());
        assertEquals("Acme Corp", p.getCompanyName());
        assertEquals("TechCorp", p.getSelectedByCompany());
        assertEquals("2026-01-01T00:00:00", p.getCreatedAt());
    }
}
