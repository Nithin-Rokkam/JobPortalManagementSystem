package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.capg.jobportal.dto.JobResponse;

import java.util.List;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: AdminJobClient
 * DESCRIPTION:
 * This Feign client is used for communication between the Admin
 * Service and the Job Service.
 *
 * It provides functionalities such as:
 * - Fetching all job listings
 * - Deleting jobs by admin
 *
 * PURPOSE:
 * Allows the Admin Service to manage job-related operations
 * remotely via Job Service APIs in a microservices architecture.
 * ================================================================
 */
@FeignClient(name = "job-service")
public interface AdminJobClient {

    @GetMapping("/api/internal/jobs/all")
    List<JobResponse> getAllJobs();

    @DeleteMapping("/api/internal/jobs/{id}")
    void deleteJob(@PathVariable Long id);
}
