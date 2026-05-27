package com.capg.jobportal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;

class DtoTest {

    // ─── ApplicationResponse ────────────────────────────────────────
    @Test
    void applicationResponse_fromEntity() {
        Application app = new Application();
        app.setId(1L);
        app.setUserId(100L);
        app.setJobId(200L);
        app.setResumeUrl("resume.pdf");
        app.setCoverLetter("cover");
        app.setStatus(ApplicationStatus.APPLIED);

        ApplicationResponse r = ApplicationResponse.fromEntity(app);
        assertEquals(1L, r.getId());
        assertEquals(100L, r.getUserId());
        assertEquals(200L, r.getJobId());
        assertEquals("resume.pdf", r.getResumeUrl());
        assertEquals("cover", r.getCoverLetter());
        assertEquals(ApplicationStatus.APPLIED, r.getStatus());
    }

    @Test
    void applicationResponse_setters() {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(1L);
        r.setUserId(2L);
        r.setJobId(3L);
        r.setResumeUrl("url");
        r.setCoverLetter("cl");
        r.setStatus(ApplicationStatus.SHORTLISTED);
        LocalDateTime now = LocalDateTime.now();
        r.setAppliedAt(now);
        r.setUpdatedAt(now);

        assertEquals(1L, r.getId());
        assertEquals(2L, r.getUserId());
        assertEquals(3L, r.getJobId());
        assertEquals("url", r.getResumeUrl());
        assertEquals("cl", r.getCoverLetter());
        assertEquals(ApplicationStatus.SHORTLISTED, r.getStatus());
        assertEquals(now, r.getAppliedAt());
        assertEquals(now, r.getUpdatedAt());
    }

    // ─── RecruiterApplicationResponse ───────────────────────────────
    @Test
    void recruiterApplicationResponse_fromEntity() {
        Application app = new Application();
        app.setId(1L);
        app.setUserId(100L);
        app.setJobId(200L);
        app.setResumeUrl("resume.pdf");
        app.setCoverLetter("cover");
        app.setStatus(ApplicationStatus.UNDER_REVIEW);
        app.setRecruiterNote("Good candidate");

        RecruiterApplicationResponse r = RecruiterApplicationResponse.fromEntity(app);
        assertEquals(1L, r.getId());
        assertEquals(100L, r.getUserId());
        assertEquals("Good candidate", r.getRecruiterNote());
    }

    @Test
    void recruiterApplicationResponse_setters() {
        RecruiterApplicationResponse r = new RecruiterApplicationResponse();
        r.setId(1L);
        r.setUserId(2L);
        r.setJobId(3L);
        r.setResumeUrl("url");
        r.setCoverLetter("cl");
        r.setStatus(ApplicationStatus.REJECTED);
        r.setRecruiterNote("note");
        LocalDateTime now = LocalDateTime.now();
        r.setAppliedAt(now);
        r.setUpdatedAt(now);

        assertEquals(1L, r.getId());
        assertEquals("note", r.getRecruiterNote());
        assertEquals(now, r.getAppliedAt());
        assertEquals(now, r.getUpdatedAt());
    }

    // ─── StatusUpdateRequest ────────────────────────────────────────
    @Test
    void statusUpdateRequest_gettersSetters() {
        StatusUpdateRequest s = new StatusUpdateRequest();
        s.setNewStatus(ApplicationStatus.SHORTLISTED);
        s.setRecruiterNote("Shortlisted");

        assertEquals(ApplicationStatus.SHORTLISTED, s.getNewStatus());
        assertEquals("Shortlisted", s.getRecruiterNote());
    }

    // ─── JobClientResponse ──────────────────────────────────────────
    @Test
    void jobClientResponse_gettersSetters() {
        JobClientResponse j = new JobClientResponse();
        j.setId(1L);
        j.setTitle("Dev");
        j.setStatus("ACTIVE");
        j.setPostedBy(10L);
        LocalDate dl = LocalDate.of(2026, 12, 31);
        j.setDeadline(dl);

        assertEquals(1L, j.getId());
        assertEquals("Dev", j.getTitle());
        assertEquals("ACTIVE", j.getStatus());
        assertEquals(10L, j.getPostedBy());
        assertEquals(dl, j.getDeadline());
    }

    // ─── ApplicationStats ───────────────────────────────────────────
    @Test
    void applicationStats_gettersSetters() {
        ApplicationStats s = new ApplicationStats();
        s.setTotalApplications(10);
        s.setAppliedCount(4);
        s.setUnderReviewCount(3);
        s.setShortlistedCount(2);
        s.setRejectedCount(1);

        assertEquals(10, s.getTotalApplications());
        assertEquals(4, s.getAppliedCount());
        assertEquals(3, s.getUnderReviewCount());
        assertEquals(2, s.getShortlistedCount());
        assertEquals(1, s.getRejectedCount());
    }

    // ─── ErrorResponse ──────────────────────────────────────────────
    @Test
    void errorResponse_constructorAndSetters() {
        ErrorResponse er = new ErrorResponse(400, "Bad Request", "msg");
        assertEquals(400, er.getStatus());
        assertNotNull(er.getTimestamp());

        er.setStatus(500);
        er.setError("Internal");
        er.setMessage("new");
        LocalDateTime ts = LocalDateTime.now();
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
}
