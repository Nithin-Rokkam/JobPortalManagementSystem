package com.capg.jobportal.test.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.dto.ErrorResponse;
import com.capg.jobportal.dto.JobResponse;
import com.capg.jobportal.dto.PlatformReport;
import com.capg.jobportal.dto.UserResponse;

class DtoTest {

    // ─── ErrorResponse ──────────────────────────────────────────────
    @Test
    void errorResponse_constructorAndGetters() {
        ErrorResponse er = new ErrorResponse(400, "Bad Request", "test message");
        assertEquals(400, er.getStatus());
        assertEquals("Bad Request", er.getError());
        assertEquals("test message", er.getMessage());
        assertNotNull(er.getTimestamp());
    }

    @Test
    void errorResponse_setters() {
        ErrorResponse er = new ErrorResponse(400, "Bad Request", "msg");
        er.setStatus(500);
        er.setError("Internal");
        er.setMessage("new msg");
        LocalDateTime ts = LocalDateTime.now();
        er.setTimestamp(ts);
        assertEquals(500, er.getStatus());
        assertEquals("Internal", er.getError());
        assertEquals("new msg", er.getMessage());
        assertEquals(ts, er.getTimestamp());
    }

    // ─── ApplicationStats ───────────────────────────────────────────
    @Test
    void applicationStats_gettersSetters() {
        ApplicationStats stats = new ApplicationStats();
        stats.setTotalApplications(10);
        stats.setAppliedCount(4);
        stats.setUnderReviewCount(3);
        stats.setShortlistedCount(2);
        stats.setRejectedCount(1);

        assertEquals(10, stats.getTotalApplications());
        assertEquals(4, stats.getAppliedCount());
        assertEquals(3, stats.getUnderReviewCount());
        assertEquals(2, stats.getShortlistedCount());
        assertEquals(1, stats.getRejectedCount());
    }

    // ─── UserResponse ───────────────────────────────────────────────
    @Test
    void userResponse_gettersSetters() {
        UserResponse u = new UserResponse();
        u.setId(1L);
        u.setName("Test");
        u.setEmail("test@test.com");
        u.setPhone("123");
        u.setRole("ADMIN");
        u.setStatus("ACTIVE");
        u.setProfilePictureUrl("pic.jpg");
        u.setResumeUrl("resume.pdf");
        u.setCompanyName("Acme Corp");
        u.setSelectedByCompany("TechCorp");
        u.setCreatedAt("2026-01-01T00:00:00");

        assertEquals(1L, u.getId());
        assertEquals("Test", u.getName());
        assertEquals("test@test.com", u.getEmail());
        assertEquals("123", u.getPhone());
        assertEquals("ADMIN", u.getRole());
        assertEquals("ACTIVE", u.getStatus());
        assertEquals("pic.jpg", u.getProfilePictureUrl());
        assertEquals("resume.pdf", u.getResumeUrl());
        assertEquals("Acme Corp", u.getCompanyName());
        assertEquals("TechCorp", u.getSelectedByCompany());
        assertEquals("2026-01-01T00:00:00", u.getCreatedAt());
    }

    // ─── JobResponse ────────────────────────────────────────────────
    @Test
    void jobResponse_gettersSetters() {
        JobResponse j = new JobResponse();
        j.setId(1L);
        j.setTitle("Dev");
        j.setCompanyName("Corp");
        j.setLocation("NYC");
        j.setSalary(new BigDecimal("100000"));
        j.setExperienceYears(5);
        j.setJobType("FULL_TIME");
        j.setSkillsRequired("Java");
        j.setDescription("desc");
        j.setStatus("ACTIVE");
        LocalDate dl = LocalDate.of(2026, 12, 31);
        j.setDeadline(dl);
        j.setPostedBy(10L);
        LocalDateTime now = LocalDateTime.now();
        j.setCreatedAt(now);
        j.setUpdatedAt(now);

        assertEquals(1L, j.getId());
        assertEquals("Dev", j.getTitle());
        assertEquals("Corp", j.getCompanyName());
        assertEquals("NYC", j.getLocation());
        assertEquals(new BigDecimal("100000"), j.getSalary());
        assertEquals(5, j.getExperienceYears());
        assertEquals("FULL_TIME", j.getJobType());
        assertEquals("Java", j.getSkillsRequired());
        assertEquals("desc", j.getDescription());
        assertEquals("ACTIVE", j.getStatus());
        assertEquals(dl, j.getDeadline());
        assertEquals(10L, j.getPostedBy());
        assertEquals(now, j.getCreatedAt());
        assertEquals(now, j.getUpdatedAt());
    }

    // ─── PlatformReport ─────────────────────────────────────────────
    @Test
    void platformReport_defaultConstructor() {
        PlatformReport report = new PlatformReport();
        assertEquals(0, report.getTotalUsers());
        assertEquals(0, report.getTotalJobs());
        assertNull(report.getApplicationStats());
    }

    @Test
    void platformReport_parameterizedConstructor() {
        ApplicationStats stats = new ApplicationStats();
        PlatformReport report = new PlatformReport(5, 10, stats);
        assertEquals(5, report.getTotalUsers());
        assertEquals(10, report.getTotalJobs());
        assertEquals(stats, report.getApplicationStats());
    }

    @Test
    void platformReport_setters() {
        PlatformReport report = new PlatformReport();
        ApplicationStats stats = new ApplicationStats();
        List<UserResponse> users = new ArrayList<>();
        List<JobResponse> jobs = new ArrayList<>();

        report.setTotalUsers(3);
        report.setTotalJobs(7);
        report.setApplicationStats(stats);
        report.setUsers(users);
        report.setJobs(jobs);

        assertEquals(3, report.getTotalUsers());
        assertEquals(7, report.getTotalJobs());
        assertEquals(stats, report.getApplicationStats());
        assertEquals(users, report.getUsers());
        assertEquals(jobs, report.getJobs());
    }
}
