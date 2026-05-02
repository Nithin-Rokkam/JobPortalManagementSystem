package com.capg.jobportal.test.enums;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;

class EnumTest {

    @Test
    void jobStatus_values() {
        JobStatus[] statuses = JobStatus.values();
        assertEquals(4, statuses.length);
        assertEquals(JobStatus.ACTIVE, JobStatus.valueOf("ACTIVE"));
        assertEquals(JobStatus.CLOSED, JobStatus.valueOf("CLOSED"));
        assertEquals(JobStatus.DRAFT, JobStatus.valueOf("DRAFT"));
        assertEquals(JobStatus.DELETED, JobStatus.valueOf("DELETED"));
    }

    @Test
    void jobType_values() {
        JobType[] types = JobType.values();
        assertEquals(4, types.length);
        assertEquals(JobType.FULL_TIME, JobType.valueOf("FULL_TIME"));
        assertEquals(JobType.PART_TIME, JobType.valueOf("PART_TIME"));
        assertEquals(JobType.REMOTE, JobType.valueOf("REMOTE"));
        assertEquals(JobType.CONTRACT, JobType.valueOf("CONTRACT"));
    }
}
