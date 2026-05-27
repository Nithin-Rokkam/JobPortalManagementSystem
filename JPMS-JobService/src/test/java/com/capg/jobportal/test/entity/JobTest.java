package com.capg.jobportal.test.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.entity.Job;
import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;

class JobTest {

    @Test
    void gettersAndSetters() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Dev");
        job.setCompanyName("Corp");
        job.setLocation("NYC");
        job.setSalary(new BigDecimal("100000"));
        job.setExperienceYears(5);
        job.setJobType(JobType.FULL_TIME);
        job.setSkillsRequired("Java");
        job.setDescription("desc");
        job.setStatus(JobStatus.ACTIVE);
        job.setDeadline(LocalDate.of(2026, 12, 31));
        job.setPostedBy(10L);
        LocalDateTime now = LocalDateTime.now();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);

        assertEquals(1L, job.getId());
        assertEquals("Dev", job.getTitle());
        assertEquals("Corp", job.getCompanyName());
        assertEquals("NYC", job.getLocation());
        assertEquals(new BigDecimal("100000"), job.getSalary());
        assertEquals(5, job.getExperienceYears());
        assertEquals(JobType.FULL_TIME, job.getJobType());
        assertEquals("Java", job.getSkillsRequired());
        assertEquals("desc", job.getDescription());
        assertEquals(JobStatus.ACTIVE, job.getStatus());
        assertEquals(LocalDate.of(2026, 12, 31), job.getDeadline());
        assertEquals(10L, job.getPostedBy());
        assertEquals(now, job.getCreatedAt());
        assertEquals(now, job.getUpdatedAt());
    }

    @Test
    void onCreate_setsTimestampsAndDefaultStatus() throws Exception {
        Job job = new Job();
        Method onCreate = Job.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(job);

        assertNotNull(job.getCreatedAt());
        assertNotNull(job.getUpdatedAt());
        assertEquals(JobStatus.ACTIVE, job.getStatus());
    }

    @Test
    void onCreate_doesNotOverrideExistingStatus() throws Exception {
        Job job = new Job();
        job.setStatus(JobStatus.DRAFT);
        Method onCreate = Job.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(job);
        assertEquals(JobStatus.DRAFT, job.getStatus());
    }

    @Test
    void onUpdate_setsUpdatedAt() throws Exception {
        Job job = new Job();
        Method onUpdate = Job.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(job);
        assertNotNull(job.getUpdatedAt());
    }

    @Test
    void defaultStatus_isActive() {
        Job job = new Job();
        assertEquals(JobStatus.ACTIVE, job.getStatus());
    }
}
