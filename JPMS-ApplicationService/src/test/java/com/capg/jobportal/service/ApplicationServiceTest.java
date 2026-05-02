package com.capg.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
<<<<<<< HEAD
import static org.mockito.Mockito.*;

=======
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
<<<<<<< HEAD

import com.capg.jobportal.client.JobServiceClient;
import com.capg.jobportal.dao.ApplicationRepository;
import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import com.capg.jobportal.exception.ForbiddenException;
=======
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.client.JobServiceClient;
import com.capg.jobportal.dao.ApplicationRepository;
import com.capg.jobportal.dto.*;
import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import com.capg.jobportal.exception.*;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
import com.capg.jobportal.util.CloudinaryUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

<<<<<<< HEAD
    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private CloudinaryUtil cloudinaryUtil;
=======
    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobServiceClient jobServiceClient;
    @Mock private CloudinaryUtil cloudinaryUtil;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private AuthServiceClient authServiceClient;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

    @InjectMocks
    private ApplicationService applicationService;

    private Application testApplication;
<<<<<<< HEAD
=======
    private JobClientResponse testJobResponse;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

    @BeforeEach
    void setUp() {
        testApplication = new Application();
        testApplication.setId(1L);
        testApplication.setUserId(100L);
        testApplication.setJobId(200L);
        testApplication.setResumeUrl("https://cloudinary.com/resume.pdf");
        testApplication.setCoverLetter("I am interested in this role");
        testApplication.setStatus(ApplicationStatus.APPLIED);
<<<<<<< HEAD
=======

        testJobResponse = new JobClientResponse();
        testJobResponse.setId(200L);
        testJobResponse.setTitle("Java Dev");
        testJobResponse.setStatus("ACTIVE");
        testJobResponse.setPostedBy(50L);
        testJobResponse.setDeadline(LocalDate.of(2027, 12, 31));
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
        assertEquals(200L, result.get(0).getJobId());
=======
    }

    @Test
    void getMyApplications_emptyList() {
        when(applicationRepository.findByUserId(999L)).thenReturn(List.of());
        List<ApplicationResponse> result = applicationService.getMyApplications(999L);
        assertTrue(result.isEmpty());
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    }

    // ─── Get Application By ID ───────────────────────────────────────

    @Test
    void getApplicationById_success() {
        when(applicationRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(testApplication));

        ApplicationResponse result = applicationService.getApplicationById(1L, 100L);
<<<<<<< HEAD

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ApplicationStatus.APPLIED, result.getStatus());
    }

    @Test
    void getApplicationById_notFound_throwsException() {
=======
        assertEquals(1L, result.getId());
    }

    @Test
    void getApplicationById_notFound_throwsForbidden() {
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
        when(applicationRepository.findByIdAndUserId(99L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> applicationService.getApplicationById(99L, 100L));
    }

<<<<<<< HEAD
=======
    // ─── Apply For Job (upload new resume) ──────────────────────────

    @Test
    void applyForJob_withNewResume_success() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);
        when(cloudinaryUtil.uploadResume(mockFile)).thenReturn("https://newresume.pdf");
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        UserInfoResponse seekerInfo = new UserInfoResponse();
        seekerInfo.setName("John");
        seekerInfo.setEmail("john@test.com");
        when(authServiceClient.getUserInfo(100L)).thenReturn(seekerInfo);

        ApplicationResponse result = applicationService.applyForJob(
                200L, "Cover letter", false, null, mockFile, 100L);

        assertNotNull(result);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void applyForJob_withExistingResume_success() throws Exception {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        UserInfoResponse seekerInfo = new UserInfoResponse();
        seekerInfo.setName("John");
        seekerInfo.setEmail("john@test.com");
        when(authServiceClient.getUserInfo(100L)).thenReturn(seekerInfo);

        ApplicationResponse result = applicationService.applyForJob(
                200L, "Cover letter", true, "https://existing.pdf", null, 100L);

        assertNotNull(result);
        verify(cloudinaryUtil, never()).uploadResume(any());
    }

    @Test
    void applyForJob_existingResumeNoUrl_throwsException() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyForJob(200L, "cover", true, null, null, 100L));
    }

    @Test
    void applyForJob_existingResumeEmptyUrl_throwsException() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyForJob(200L, "cover", true, "", null, 100L));
    }

    @Test
    void applyForJob_noResumeFile_throwsException() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, null, 100L));
    }

    @Test
    void applyForJob_emptyResumeFile_throwsException() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, mockFile, 100L));
    }

    @Test
    void applyForJob_jobNotFound_throwsException() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, null, 100L));
    }

    @Test
    void applyForJob_jobDeleted_throwsException() {
        testJobResponse.setStatus("DELETED");
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, null, 100L));
    }

    @Test
    void applyForJob_jobClosed_throwsException() {
        testJobResponse.setStatus("CLOSED");
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, null, 100L));
    }

    @Test
    void applyForJob_deadlinePassed_throwsException() {
        testJobResponse.setDeadline(LocalDate.of(2020, 1, 1));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, null, 100L));
    }

    @Test
    void applyForJob_duplicateApplication_throwsException() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(true);

        assertThrows(DuplicateApplicationException.class,
                () -> applicationService.applyForJob(200L, "cover", false, null, null, 100L));
    }

    @Test
    void applyForJob_rabbitMQFails_stillSucceeds() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);
        when(cloudinaryUtil.uploadResume(mockFile)).thenReturn("resume.pdf");
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        when(authServiceClient.getUserInfo(100L)).thenThrow(new RuntimeException("Service down"));

        // Should not throw - event publishing failure is caught
        ApplicationResponse result = applicationService.applyForJob(
                200L, "cover", false, null, mockFile, 100L);
        assertNotNull(result);
    }

    @Test
    void applyForJob_nullDeadline_success() throws Exception {
        testJobResponse.setDeadline(null);
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.existsByUserIdAndJobId(100L, 200L)).thenReturn(false);
        when(cloudinaryUtil.uploadResume(mockFile)).thenReturn("resume.pdf");
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);
        when(authServiceClient.getUserInfo(100L)).thenReturn(new UserInfoResponse());

        ApplicationResponse result = applicationService.applyForJob(
                200L, "cover", false, null, mockFile, 100L);
        assertNotNull(result);
    }

    // ─── Get Applicants For Job ──────────────────────────────────────

    @Test
    void getApplicantsForJob_owner_success() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.findByJobId(200L))
                .thenReturn(Arrays.asList(testApplication));

        List<RecruiterApplicationResponse> result =
                applicationService.getApplicantsForJob(200L, 50L);

        assertEquals(1, result.size());
    }

    @Test
    void getApplicantsForJob_jobNotFound_throwsException() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.getApplicantsForJob(200L, 50L));
    }

    @Test
    void getApplicantsForJob_notOwner_throwsForbidden() {
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(ForbiddenException.class,
                () -> applicationService.getApplicantsForJob(200L, 999L));
    }

    // ─── Update Application Status ──────────────────────────────────

    @Test
    void updateApplicationStatus_validTransition() {
        testApplication.setStatus(ApplicationStatus.APPLIED);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.UNDER_REVIEW);
        request.setRecruiterNote("Reviewing now");

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        ApplicationResponse result = applicationService.updateApplicationStatus(1L, request, 50L);

        assertNotNull(result);
    }

    @Test
    void updateApplicationStatus_withoutNote() {
        testApplication.setStatus(ApplicationStatus.APPLIED);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.UNDER_REVIEW);
        request.setRecruiterNote(null);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        ApplicationResponse result = applicationService.updateApplicationStatus(1L, request, 50L);
        assertNotNull(result);
    }

    @Test
    void updateApplicationStatus_applicationNotFound_throwsException() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.updateApplicationStatus(99L, request, 50L));
    }

    @Test
    void updateApplicationStatus_jobNull_throwsForbidden() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.UNDER_REVIEW);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(null);

        assertThrows(ForbiddenException.class,
                () -> applicationService.updateApplicationStatus(1L, request, 50L));
    }

    @Test
    void updateApplicationStatus_notOwner_throwsForbidden() {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.UNDER_REVIEW);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(ForbiddenException.class,
                () -> applicationService.updateApplicationStatus(1L, request, 999L));
    }

    @Test
    void updateApplicationStatus_alreadyRejected_throwsException() {
        testApplication.setStatus(ApplicationStatus.REJECTED);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(InvalidStatusTransitionException.class,
                () -> applicationService.updateApplicationStatus(1L, request, 50L));
    }

    @Test
    void updateApplicationStatus_invalidTransition_throwsException() {
        testApplication.setStatus(ApplicationStatus.APPLIED);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.SHORTLISTED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);

        assertThrows(InvalidStatusTransitionException.class,
                () -> applicationService.updateApplicationStatus(1L, request, 50L));
    }

    @Test
    void updateApplicationStatus_underReviewToShortlisted() {
        testApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.SHORTLISTED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        ApplicationResponse result = applicationService.updateApplicationStatus(1L, request, 50L);
        assertNotNull(result);
    }

    @Test
    void updateApplicationStatus_underReviewToRejected() {
        testApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        ApplicationResponse result = applicationService.updateApplicationStatus(1L, request, 50L);
        assertNotNull(result);
    }

    @Test
    void updateApplicationStatus_shortlistedToRejected() {
        testApplication.setStatus(ApplicationStatus.SHORTLISTED);
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(jobServiceClient.getJobById(eq(200L), anyString(), anyString()))
                .thenReturn(testJobResponse);
        when(applicationRepository.save(any(Application.class))).thenReturn(testApplication);

        ApplicationResponse result = applicationService.updateApplicationStatus(1L, request, 50L);
        assertNotNull(result);
    }

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    // ─── Application Stats ───────────────────────────────────────────

    @Test
    void getApplicationStats_success() {
<<<<<<< HEAD
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
=======
        Application a1 = new Application(); a1.setStatus(ApplicationStatus.APPLIED);
        Application a2 = new Application(); a2.setStatus(ApplicationStatus.UNDER_REVIEW);
        Application a3 = new Application(); a3.setStatus(ApplicationStatus.SHORTLISTED);
        Application a4 = new Application(); a4.setStatus(ApplicationStatus.REJECTED);
        Application a5 = new Application(); a5.setStatus(ApplicationStatus.SELECTED);

        when(applicationRepository.findAll()).thenReturn(Arrays.asList(a1, a2, a3, a4, a5));

        ApplicationStats stats = applicationService.getApplicationStats();

        assertEquals(5, stats.getTotalApplications());
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
        assertEquals(1, stats.getAppliedCount());
        assertEquals(1, stats.getUnderReviewCount());
        assertEquals(1, stats.getShortlistedCount());
        assertEquals(1, stats.getRejectedCount());
    }

<<<<<<< HEAD
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
=======
    @Test
    void getApplicationStats_emptyList() {
        when(applicationRepository.findAll()).thenReturn(List.of());
        ApplicationStats stats = applicationService.getApplicationStats();
        assertEquals(0, stats.getTotalApplications());
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    }
}
