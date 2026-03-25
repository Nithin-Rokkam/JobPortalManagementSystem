package com.capg.jobportal.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.client.JobServiceClient;
import com.capg.jobportal.dao.ApplicationRepository;
import com.capg.jobportal.dto.*;
import com.capg.jobportal.entity.Application;
import com.capg.jobportal.enums.ApplicationStatus;
import com.capg.jobportal.exception.*;
import com.capg.jobportal.util.CloudinaryUtil;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: ApplicationService
 * DESCRIPTION:
 * This service handles all business logic related to job applications
 * including applying for jobs, retrieving applications, managing
 * application status, validating transitions, and generating
 * application statistics.
 * ================================================================
 */
@Service
public class ApplicationService {

    /*
     * Logger instance for tracking business operations
     */
    private static final Logger logger = LogManager.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final JobServiceClient jobServiceClient;
    private final CloudinaryUtil cloudinaryUtil;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobServiceClient jobServiceClient,
                              CloudinaryUtil cloudinaryUtil) {
        this.applicationRepository = applicationRepository;
        this.jobServiceClient = jobServiceClient;
        this.cloudinaryUtil = cloudinaryUtil;
    }
    

    /* ================================================================
     * METHOD: applyForJob
     * DESCRIPTION:
     * Allows a job seeker to apply for a job after validating job status,
     * deadline, and duplicate applications. Handles resume upload logic.
     * ================================================================ */
    public ApplicationResponse applyForJob(Long jobId, String coverLetter,
                                           boolean useExistingResume, String existingResumeUrl,
                                           MultipartFile resumeFile, Long seekerId) throws IOException {

        logger.info("User [{}] applying for job [{}]", seekerId, jobId);

        JobClientResponse job =
                jobServiceClient.getJobById(jobId, String.valueOf(seekerId), "JOB_SEEKER");

        if (job == null) {
            logger.warn("Job [{}] not found", jobId);
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }

        if ("DELETED".equals(job.getStatus()) || "CLOSED".equals(job.getStatus())) {
            logger.warn("Job [{}] is {} — cannot apply", jobId, job.getStatus());
            throw new ResourceNotFoundException("This job is no longer accepting applications");
        }

        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            logger.warn("Deadline passed for job [{}]: {}", jobId, job.getDeadline());
            throw new IllegalArgumentException("Application deadline has passed");
        }

        if (applicationRepository.existsByUserIdAndJobId(seekerId, jobId)) {
            logger.warn("Duplicate application — user [{}] already applied for job [{}]", seekerId, jobId);
            throw new DuplicateApplicationException("Already applied");
        }

        String resumeUrl;

        if (useExistingResume) {
            if (existingResumeUrl == null || existingResumeUrl.isEmpty()) {
                logger.warn("User [{}] selected existing resume but no URL provided", seekerId);
                throw new IllegalArgumentException("No saved resume found");
            }
            resumeUrl = existingResumeUrl;
        } else {
            if (resumeFile == null || resumeFile.isEmpty()) {
                logger.warn("User [{}] did not upload resume", seekerId);
                throw new IllegalArgumentException("Resume required");
            }
            resumeUrl = cloudinaryUtil.uploadResume(resumeFile);
            logger.debug("Resume uploaded for user [{}]: {}", seekerId, resumeUrl);
        }

        Application application = new Application();
        application.setUserId(seekerId);
        application.setJobId(jobId);
        application.setResumeUrl(resumeUrl);
        application.setCoverLetter(coverLetter);
        application.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationRepository.save(application);

        logger.info("Application [{}] created successfully", saved.getId());

        return ApplicationResponse.fromEntity(saved);
    }

    
    /* ================================================================
     * METHOD: getMyApplications
     * DESCRIPTION:
     * Retrieves all applications submitted by a specific job seeker.
     * ================================================================ */
    public List<ApplicationResponse> getMyApplications(Long seekerId) {

        logger.debug("Fetching applications for user [{}]", seekerId);

        List<Application> list = applicationRepository.findByUserId(seekerId);

        List<ApplicationResponse> response = new ArrayList<>();
        for (Application app : list) {
            response.add(ApplicationResponse.fromEntity(app));
        }

        logger.info("Returned {} applications", response.size());

        return response;
    }

    
    /* ================================================================
     * METHOD: getApplicationById
     * DESCRIPTION:
     * Retrieves a specific application ensuring it belongs to the user.
     * ================================================================ */
    public ApplicationResponse getApplicationById(Long id, Long seekerId) {

        logger.debug("Fetching application [{}] for user [{}]", id, seekerId);

        Optional<Application> optional =
                applicationRepository.findByIdAndUserId(id, seekerId);

        if (optional.isEmpty()) {
            logger.warn("Application [{}] not accessible", id);
            throw new ForbiddenException("Not allowed");
        }

        return ApplicationResponse.fromEntity(optional.get());
    }

    
    /* ================================================================
     * METHOD: getApplicantsForJob
     * DESCRIPTION:
     * Retrieves all applicants for a job ensuring recruiter ownership.
     * ================================================================ */
    public List<RecruiterApplicationResponse> getApplicantsForJob(Long jobId, Long recruiterId) {

        logger.debug("Fetching applicants for job [{}]", jobId);

        JobClientResponse job =
                jobServiceClient.getJobById(jobId, String.valueOf(recruiterId), "RECRUITER");

        if (job == null) {
            throw new ResourceNotFoundException("Job not found");
        }

        if (!job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("Not allowed");
        }

        List<Application> list = applicationRepository.findByJobId(jobId);

        List<RecruiterApplicationResponse> response = new ArrayList<>();
        for (Application app : list) {
            response.add(RecruiterApplicationResponse.fromEntity(app));
        }

        return response;
    }
    

    /* ================================================================
     * METHOD: updateApplicationStatus
     * DESCRIPTION:
     * Updates the status of an application with proper validation
     * and recruiter authorization.
     * ================================================================ */
    public ApplicationResponse updateApplicationStatus(Long id,
                                                       StatusUpdateRequest request,
                                                       Long recruiterId) {

        logger.info("Updating application [{}] status", id);

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        JobClientResponse job = jobServiceClient.getJobById(
                app.getJobId(), String.valueOf(recruiterId), "RECRUITER");

        if (job == null || !job.getPostedBy().equals(recruiterId)) {
            throw new ForbiddenException("Not allowed");
        }

        validateStatusTransition(app.getStatus(), request.getNewStatus());

        app.setStatus(request.getNewStatus());

        if (request.getRecruiterNote() != null) {
            app.setRecruiterNote(request.getRecruiterNote());
        }

        Application updated = applicationRepository.save(app);

        return ApplicationResponse.fromEntity(updated);
    }

    
    /* ================================================================
     * METHOD: validateStatusTransition
     * DESCRIPTION:
     * Ensures only valid transitions between application statuses.
     * ================================================================ */
    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus next) {

        if (current == ApplicationStatus.REJECTED) {
            throw new InvalidStatusTransitionException("Already rejected");
        }

        boolean valid =
                (current == ApplicationStatus.APPLIED && next == ApplicationStatus.UNDER_REVIEW) ||
                (current == ApplicationStatus.UNDER_REVIEW &&
                        (next == ApplicationStatus.SHORTLISTED || next == ApplicationStatus.REJECTED)) ||
                (current == ApplicationStatus.SHORTLISTED && next == ApplicationStatus.REJECTED);

        if (!valid) {
            throw new InvalidStatusTransitionException("Invalid transition");
        }
    }

    
    /* ================================================================
     * METHOD: getApplicationStats
     * DESCRIPTION:
     * Calculates application statistics including total and
     * status-wise counts.
     * ================================================================ */
    public ApplicationStats getApplicationStats() {

        List<Application> all = applicationRepository.findAll();

        long applied = 0, review = 0, shortlisted = 0, rejected = 0;

        for (Application app : all) {
            switch (app.getStatus()) {
                case APPLIED -> applied++;
                case UNDER_REVIEW -> review++;
                case SHORTLISTED -> shortlisted++;
                case REJECTED -> rejected++;
            }
        }

        ApplicationStats stats = new ApplicationStats();
        stats.setTotalApplications(all.size());
        stats.setAppliedCount(applied);
        stats.setUnderReviewCount(review);
        stats.setShortlistedCount(shortlisted);
        stats.setRejectedCount(rejected);

        logger.info("Application stats calculated");

        return stats;
    }
}