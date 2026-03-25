package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.capg.jobportal.dto.JobClientResponse;

@FeignClient(name = "job-service")
public interface JobServiceClient {

	@GetMapping("/api/jobs/{id}")
    JobClientResponse getJobById(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    );

}
