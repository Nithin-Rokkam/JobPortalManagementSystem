package com.capg.jobportal.controller;

import java.util.List;

/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: JobController
 * DESCRIPTION:
 * This controller handles all job-related operations including
 * posting jobs, fetching jobs, searching jobs, updating jobs,
 * deleting jobs, and retrieving recruiter-specific jobs.
 * ================================================================
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.jobportal.dto.JobRequestDTO;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;
import com.capg.jobportal.service.JobService;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    /*
     * Logger instance for tracking API activity
     */
    private static final Logger logger = LogManager.getLogger(JobController.class);

    /*
     * Constructor injection for better testability and clarity
     */
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }
    

    /* ================================================================
     * METHOD: postJob
     * DESCRIPTION:
     * Allows a recruiter to post a new job listing.
     * ================================================================ */
    @PostMapping
    public ResponseEntity<JobResponseDTO> postJob(
            @RequestBody JobRequestDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        logger.info("Recruiter [{}] posting job: {}", userId, dto.getTitle());

        JobResponseDTO response = jobService.postJob(dto, userId, userRole);

        logger.info("Job [{}] created successfully", response.getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    

    /* ================================================================
     * METHOD: getAllJobs
     * DESCRIPTION:
     * Fetches all jobs with pagination support for public users.
     * ================================================================ */
    @GetMapping
    public ResponseEntity<PagedResponse<JobResponseDTO>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Fetching jobs — page: {}, size: {}", page, size);

        PagedResponse<JobResponseDTO> response = jobService.getAllJobs(page, size);

        logger.info("Returned {} jobs", response.getContent().size());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: getJobById
     * DESCRIPTION:
     * Fetches job details based on job ID.
     * ================================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {

        logger.info("Fetching job with ID: {}", id);

        JobResponseDTO response = jobService.getJobById(id);

        logger.info("Job [{}] fetched successfully", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: searchJobs
     * DESCRIPTION:
     * Searches jobs based on filters like title, location, job type,
     * and experience with pagination support.
     * ================================================================ */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Searching jobs — title: {}, location: {}, type: {}, exp: {}",
                title, location, jobType, experienceYears);

        PagedResponse<JobResponseDTO> response =
                jobService.searchJobs(title, location, jobType, experienceYears, page, size);

        logger.info("Search returned {} results", response.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: updateJob
     * DESCRIPTION:
     * Allows a recruiter to update a job they own.
     * ================================================================ */
    @PutMapping("/{id}")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @RequestBody JobRequestDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        logger.info("Recruiter [{}] updating job [{}]", userId, id);

        JobResponseDTO response = jobService.updateJob(id, dto, userId, userRole);

        logger.info("Job [{}] updated successfully", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: deleteJob
     * DESCRIPTION:
     * Allows a recruiter to delete (soft delete) a job they own.
     * ================================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        logger.info("Recruiter [{}] deleting job [{}]", userId, id);

        jobService.deleteJob(id, userId, userRole);

        logger.info("Job [{}] deleted successfully", id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    
    /* ================================================================
     * METHOD: getMyJobs
     * DESCRIPTION:
     * Fetches jobs posted by a specific recruiter with pagination.
     * ================================================================ */
    @GetMapping("/my-jobs")
    public ResponseEntity<PagedResponse<JobResponseDTO>> getMyJobs(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Fetching jobs for recruiter [{}]", userId);

        PagedResponse<JobResponseDTO> response =
                jobService.getMyJobs(userId, userRole, page, size);

        logger.info("Returned {} jobs for recruiter [{}]",
                response.getContent().size(), userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}