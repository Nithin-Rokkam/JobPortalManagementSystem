package com.capg.jobportal.controller;

import java.util.List;

/*
 * ================================================================
 * AUTHOR: Kushagra Varshney
 * CLASS: InternalJobController
 * DESCRIPTION:
 * This controller provides internal APIs for job management operations
 * such as fetching all jobs and deleting jobs by admin.
 *
 * NOTE:
 * This controller is intended for internal microservice communication
 * (e.g., AdminService) and is NOT exposed to end users via API Gateway.
 * ================================================================
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.service.JobService;

@RestController
@RequestMapping("/api/internal")
public class InternalJobController {

    /*
     * Logger instance for tracking internal API operations
     */
    private static final Logger logger =
            LogManager.getLogger(InternalJobController.class);

    private final JobService jobService;

    public InternalJobController(JobService jobService) {
        this.jobService = jobService;
    }

    
    /* ================================================================
     * METHOD: getAllJobsForAdmin
     * DESCRIPTION:
     * Fetches all jobs available in the system for admin-level operations.
     * Used internally by AdminService.
     * ================================================================ */
    @GetMapping("/jobs/all")
    public ResponseEntity<List<JobResponseDTO>> getAllJobsForAdmin() {

        logger.info("Internal API → Admin fetching all jobs");

        List<JobResponseDTO> jobs = jobService.getAllJobsForAdmin();

        logger.info("Returned {} jobs", jobs.size());

        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    
    /* ================================================================
     * METHOD: deleteJobByAdmin
     * DESCRIPTION:
     * Deletes a job by its ID. This operation is restricted to admin
     * and used internally by AdminService.
     * ================================================================ */
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJobByAdmin(@PathVariable Long id) {

        logger.info("Internal API → Admin deleting job [{}]", id);

        jobService.deleteJobByAdmin(id);

        logger.info("Job [{}] deleted successfully", id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}