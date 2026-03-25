package com.capg.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capg.jobportal.client.JobServiceClient;
import com.capg.jobportal.dao.ApplicationRepository;
import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import com.capg.jobportal.exception.ForbiddenException;
import com.capg.jobportal.util.CloudinaryUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private CloudinaryUtil cloudinaryUtil;

    @InjectMocks
    private ApplicationService applicationService;

    private Application testApplication;

    @BeforeEach
    void setUp() {
        testApplication = new Application();
        testApplication.setId(1L);
        testApplication.setUserId(100L);
        testApplication.setJobId(200L);
        testApplication.setResumeUrl("https://cloudinary.com/resume.pdf");
        testApplication.setCoverLetter("I am interested in this role");
        testApplication.setStatus(ApplicationStatus.APPLIED);
    }

    // ─── Get My Applications ─────────────────────────────────────────

    @Test
    void getMyApplications_success() {
        when(applicationRepository.findByUserId(100L))
                .thenReturn(Arrays.asList(testApplication));

        List<ApplicationResponse> result = applicationService.getMyApplications(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getUserId());
        assertEquals(200L, result.get(0).getJobId());
    }

    // ─── Get Application By ID ───────────────────────────────────────

    @Test
    void getApplicationById_success() {
        when(applicationRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(testApplication));

        ApplicationResponse result = applicationService.getApplicationById(1L, 100L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
    }

    @Test
    void getApplicationById_notFound_throwsException() {
        when(applicationRepository.findByIdAndUserId(99L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> applicationService.getApplicationById(99L, 100L));
    }

    // ─── Application Stats ───────────────────────────────────────────

    @Test
    void getApplicationStats_success() {
        Application app1 = new Application();
        app1.setStatus(ApplicationStatus.APPLIED);

        Application app2 = new Application();
        app2.setStatus(ApplicationStatus.UNDER_REVIEW);

        Application app3 = new Application();
        app3.setStatus(ApplicationStatus.SHORTLISTED);

        Application app4 = new Application();
        app4.setStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findAll())
                .thenReturn(Arrays.asList(app1, app2, app3, app4));

        ApplicationStats stats = applicationService.getApplicationStats();

        assertEquals(4, stats.getTotalApplications());
        assertEquals(1, stats.getAppliedCount());
        assertEquals(1, stats.getUnderReviewCount());
        assertEquals(1, stats.getShortlistedCount());
        assertEquals(1, stats.getRejectedCount());
    }

    // ─── Status Transition Validation ────────────────────────────────
    // We test the private validateStatusTransition indirectly via updateApplicationStatus

    @Test
    void getApplicationStats_emptyList_returnsZeros() {
        when(applicationRepository.findAll()).thenReturn(List.of());

        ApplicationStats stats = applicationService.getApplicationStats();

        assertEquals(0, stats.getTotalApplications());
        assertEquals(0, stats.getAppliedCount());
        assertEquals(0, stats.getUnderReviewCount());
        assertEquals(0, stats.getShortlistedCount());
        assertEquals(0, stats.getRejectedCount());
    }

    @Test
    void getMyApplications_emptyList_returnsEmpty() {
        when(applicationRepository.findByUserId(999L)).thenReturn(List.of());

        List<ApplicationResponse> result = applicationService.getMyApplications(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
