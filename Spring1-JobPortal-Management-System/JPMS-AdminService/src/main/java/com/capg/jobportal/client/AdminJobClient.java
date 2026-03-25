package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.capg.jobportal.dto.JobResponse;

import java.util.List;

@FeignClient(name = "job-service")
public interface AdminJobClient {

    @GetMapping("/api/internal/jobs/all")
    List<JobResponse> getAllJobs();

    @DeleteMapping("/api/internal/jobs/{id}")
    void deleteJob(@PathVariable Long id);
}
