package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.capg.jobportal.dto.JobClientResponse;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * INTERFACE: JobServiceClient
 * DESCRIPTION:
 * This Feign client is used for inter-service communication with
 * the Job Service in a microservices architecture.
 *
 * It allows the Application Service (or other services) to fetch
 * job-related data by making REST API calls to the Job Service.
 *
 * KEY FEATURES:
 * - Uses OpenFeign for declarative REST client implementation
 * - Communicates with "job-service" registered in Eureka
 * - Passes user context via headers (X-User-Id, X-User-Role)
 *
 * PURPOSE:
 * Enables seamless and type-safe communication between
 * microservices without manually writing HTTP client code.
 * ================================================================
 */
@FeignClient(name = "job-service")
public interface JobServiceClient {

	@GetMapping("/api/jobs/{id}")
    JobClientResponse getJobById(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    );

}
