package com.capg.jobportal.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JobAppliedEventTest {

    @Test
    void defaultConstructor() {
        JobAppliedEvent e = new JobAppliedEvent();
        assertNull(e.getJobId());
    }

    @Test
    void gettersAndSetters() {
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
