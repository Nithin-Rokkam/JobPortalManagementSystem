package com.capg.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.capg.jobportal.Exceptions.ForbiddenException;
import com.capg.jobportal.Exceptions.ResourceNotFoundException;
import com.capg.jobportal.dto.JobRequestDTO;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;
import com.capg.jobportal.entity.Job;
import com.capg.jobportal.enums.JobStatus;
import com.capg.jobportal.enums.JobType;
import com.capg.jobportal.repository.JobRepository;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job testJob;
    private JobRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        testJob = new Job();
        testJob.setId(1L);
        testJob.setTitle("Java Developer");
        testJob.setCompanyName("TechCorp");
        testJob.setLocation("Bangalore");
        testJob.setSalary(new BigDecimal("1500000"));
        testJob.setExperienceYears(3);
        testJob.setJobType(JobType.FULL_TIME);
        testJob.setSkillsRequired("Java, Spring Boot");
        testJob.setDescription("Java Developer role");
        testJob.setStatus(JobStatus.ACTIVE);
        testJob.setDeadline(LocalDate.of(2026, 12, 31));
        testJob.setPostedBy(10L);
        testJob.setCreatedAt(LocalDateTime.now());
        testJob.setUpdatedAt(LocalDateTime.now());

        testRequest = new JobRequestDTO();
        testRequest.setTitle("Java Developer");
        testRequest.setCompanyName("TechCorp");
        testRequest.setLocation("Bangalore");
        testRequest.setSalary(new BigDecimal("1500000"));
        testRequest.setExperienceYears(3);
        testRequest.setJobType("FULL_TIME");
        testRequest.setSkillsRequired("Java, Spring Boot");
        testRequest.setDescription("Java Developer role");
        testRequest.setDeadline(LocalDate.of(2026, 12, 31));
    }

    // ─── Post Job ────────────────────────────────────────────────────

    @Test
    void postJob_recruiter_success() {
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        JobResponseDTO result = jobService.postJob(testRequest, 10L, "RECRUITER");

        assertNotNull(result);
        assertEquals("Java Developer", result.getTitle());
        assertEquals("TechCorp", result.getCompanyName());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void postJob_nonRecruiter_throwsForbidden() {
        assertThrows(ForbiddenException.class,
                () -> jobService.postJob(testRequest, 10L, "JOB_SEEKER"));
        verify(jobRepository, never()).save(any(Job.class));
    }

    // ─── Get Job By ID ───────────────────────────────────────────────

    @Test
    void getJobById_success() {
        when(jobRepository.findByIdAndStatusNot(1L, JobStatus.DELETED))
                .thenReturn(Optional.of(testJob));

        JobResponseDTO result = jobService.getJobById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java Developer", result.getTitle());
    }

    @Test
    void getJobById_notFound_throwsException() {
        when(jobRepository.findByIdAndStatusNot(99L, JobStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobService.getJobById(99L));
    }

    // ─── Update Job ──────────────────────────────────────────────────

    @Test
    void updateJob_recruiterOwner_success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);

        JobResponseDTO result = jobService.updateJob(1L, testRequest, 10L, "RECRUITER");

        assertNotNull(result);
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void updateJob_nonOwner_throwsForbidden() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        assertThrows(ForbiddenException.class,
                () -> jobService.updateJob(1L, testRequest, 99L, "RECRUITER"));
    }

    // ─── Delete Job ──────────────────────────────────────────────────

    @Test
    void deleteJob_recruiterOwner_success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        jobService.deleteJob(1L, 10L, "RECRUITER");

        assertEquals(JobStatus.DELETED, testJob.getStatus());
        verify(jobRepository).save(testJob);
    }

    @Test
    void deleteJob_nonRecruiter_throwsForbidden() {
        assertThrows(ForbiddenException.class,
                () -> jobService.deleteJob(1L, 10L, "JOB_SEEKER"));
    }

    // ─── Get My Jobs ─────────────────────────────────────────────────

    @Test
    void getMyJobs_recruiter_success() {
        List<Job> jobList = new ArrayList<>();
        jobList.add(testJob);
        Page<Job> jobPage = new PageImpl<>(jobList);

        when(jobRepository.findByPostedByAndStatusNot(eq(10L), eq(JobStatus.DELETED), any(Pageable.class)))
                .thenReturn(jobPage);

        PagedResponse<JobResponseDTO> result = jobService.getMyJobs(10L, "RECRUITER", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Java Developer", result.getContent().get(0).getTitle());
    }

    @Test
    void getMyJobs_nonRecruiter_throwsForbidden() {
        assertThrows(ForbiddenException.class,
                () -> jobService.getMyJobs(10L, "JOB_SEEKER", 0, 10));
    }

    // ─── Admin Operations ────────────────────────────────────────────

    @Test
    void deleteJobByAdmin_success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(testJob));

        jobService.deleteJobByAdmin(1L);

        assertEquals(JobStatus.DELETED, testJob.getStatus());
        verify(jobRepository).save(testJob);
    }

    @Test
    void deleteJobByAdmin_notFound_throwsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobService.deleteJobByAdmin(99L));
    }
}
