package com.capg.jobportal.test.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import com.capg.jobportal.event.JobPostedEvent;

class JobPostedEventTest {

    @Test
    void defaultConstructor() {
        JobPostedEvent e = new JobPostedEvent();
        assertNull(e.getJobId());
    }

    @Test
    void parameterizedConstructor() {
        JobPostedEvent e = new JobPostedEvent(1L, 2L, "Dev", "Corp",
                "NYC", "FULL_TIME", new BigDecimal("100000"), 3, "desc");
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
    void setters() {
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
    }
}
