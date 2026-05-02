package com.capg.jobportal.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class EventTest {

    @Test
    void jobPostedEvent_defaultConstructor() {
        JobPostedEvent e = new JobPostedEvent();
        assertNull(e.getJobId());
        assertNull(e.getRecruiterId());
        assertNull(e.getTitle());
    }

    @Test
    void jobPostedEvent_gettersSetters() {
        JobPostedEvent e = new JobPostedEvent();
        e.setJobId(1L);
        e.setRecruiterId(2L);
        e.setTitle("Dev");
        e.setCompanyName("Corp");
        e.setLocation("NYC");
        e.setJobType("FULL_TIME");
        e.setSalary(new BigDecimal("100000"));
        e.setExperienceYears(3);
        e.setDescription("desc");

        assertEquals(1L, e.getJobId());
        assertEquals(2L, e.getRecruiterId());
        assertEquals("Dev", e.getTitle());
        assertEquals("Corp", e.getCompanyName());
        assertEquals("NYC", e.getLocation());
        assertEquals("FULL_TIME", e.getJobType());
        assertEquals(new BigDecimal("100000"), e.getSalary());
        assertEquals(3, e.getExperienceYears());
        assertEquals("desc", e.getDescription());
    }

    @Test
    void jobAppliedEvent_defaultConstructor() {
        JobAppliedEvent e = new JobAppliedEvent();
        assertNull(e.getJobId());
    }

    @Test
    void jobAppliedEvent_gettersSetters() {
        JobAppliedEvent e = new JobAppliedEvent();
        e.setJobId(1L);
        e.setJobTitle("Dev");
        e.setSeekerId(2L);
        e.setSeekerName("John");
        e.setSeekerEmail("john@test.com");
        e.setRecruiterId(3L);

        assertEquals(1L, e.getJobId());
        assertEquals("Dev", e.getJobTitle());
        assertEquals(2L, e.getSeekerId());
        assertEquals("John", e.getSeekerName());
        assertEquals("john@test.com", e.getSeekerEmail());
        assertEquals(3L, e.getRecruiterId());
    }
}
