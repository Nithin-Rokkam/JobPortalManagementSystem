package com.capg.jobportal.enums;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ApplicationStatusTest {

    @Test
    void values() {
        ApplicationStatus[] statuses = ApplicationStatus.values();
        assertEquals(5, statuses.length);
    }

    @Test
    void valueOf() {
        assertEquals(ApplicationStatus.APPLIED, ApplicationStatus.valueOf("APPLIED"));
        assertEquals(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.valueOf("UNDER_REVIEW"));
        assertEquals(ApplicationStatus.SHORTLISTED, ApplicationStatus.valueOf("SHORTLISTED"));
        assertEquals(ApplicationStatus.SELECTED, ApplicationStatus.valueOf("SELECTED"));
        assertEquals(ApplicationStatus.REJECTED, ApplicationStatus.valueOf("REJECTED"));
    }
}
