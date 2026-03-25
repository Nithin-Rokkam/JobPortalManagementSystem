package com.capg.jobportal.controller;

import java.io.IOException;
import java.util.List;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.RecruiterApplicationResponse;
import com.capg.jobportal.dto.StatusUpdateRequest;
import com.capg.jobportal.service.ApplicationService;

import jakarta.validation.Valid;


/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: ApplicationController
 * DESCRIPTION:
 * This controller manages job application operations including
 * applying for jobs, retrieving applications, viewing applicants,
 * and updating application status. It supports both job seekers
 * and recruiters with role-based access control.
 * ================================================================
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    /*
     * Logger instance for tracking application-related API calls
     */
    private static final Logger logger = LogManager.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    

    /* ================================================================
     * METHOD: applyForJob
     * DESCRIPTION:
     * Allows a job seeker to apply for a job by submitting resume
     * and optional cover letter. Supports both new and existing resumes.
     * ================================================================ */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplicationResponse> applyForJob(
            @RequestParam("jobId") Long jobId,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "useExistingResume", defaultValue = "false") boolean useExistingResume,
            @RequestParam(value = "existingResumeUrl", required = false) String existingResumeUrl,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) throws IOException {

        logger.info("User [{}] applying for job [{}]", userId, jobId);

        if (!role.equals("JOB_SEEKER")) {
            logger.warn("Forbidden access: role '{}' tried to apply", role);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ApplicationResponse response = applicationService.applyForJob(
                jobId, coverLetter, useExistingResume, existingResumeUrl, resume, userId);

        logger.info("Application [{}] created for job [{}] by user [{}]",
                response.getId(), jobId, userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    
    /* ================================================================
     * METHOD: getMyApplications
     * DESCRIPTION:
     * Retrieves all applications submitted by the logged-in job seeker.
     * ================================================================ */
    @GetMapping("/my-applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Fetching applications for user [{}]", userId);

        if (!role.equals("JOB_SEEKER")) {
            logger.warn("Forbidden access: role '{}' tried to fetch applications", role);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<ApplicationResponse> response = applicationService.getMyApplications(userId);

        logger.info("Returned {} applications for user [{}]", response.size(), userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: getApplicationById
     * DESCRIPTION:
     * Retrieves details of a specific application for the job seeker.
     * ================================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("User [{}] fetching application [{}]", userId, id);

        if (!role.equals("JOB_SEEKER")) {
            logger.warn("Forbidden access: role '{}' tried to access application [{}]", role, id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ApplicationResponse response = applicationService.getApplicationById(id, userId);

        logger.info("Application [{}] fetched successfully", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: getApplicantsForJob
     * DESCRIPTION:
     * Allows recruiters to fetch all applicants for a specific job.
     * ================================================================ */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<RecruiterApplicationResponse>> getApplicantsForJob(
            @PathVariable Long jobId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Recruiter [{}] fetching applicants for job [{}]", userId, jobId);

        if (!role.equals("RECRUITER")) {
            logger.warn("Forbidden access: role '{}' tried to fetch applicants", role);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<RecruiterApplicationResponse> response =
                applicationService.getApplicantsForJob(jobId, userId);

        logger.info("Returned {} applicants for job [{}]", response.size(), jobId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: updateApplicationStatus
     * DESCRIPTION:
     * Allows recruiters to update the status of an application
     * (e.g., shortlisted, rejected, under review).
     * ================================================================ */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        logger.info("Recruiter [{}] updating application [{}] to status '{}'",
                userId, id, request.getNewStatus());

        if (!role.equals("RECRUITER")) {
            logger.warn("Forbidden access: role '{}' tried to update application [{}]", role, id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ApplicationResponse response =
                applicationService.updateApplicationStatus(id, request, userId);

        logger.info("Application [{}] status updated successfully", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}