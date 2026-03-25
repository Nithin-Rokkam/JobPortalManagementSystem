package com.capg.jobportal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.capg.jobportal.dto.ApplicationStats;

@FeignClient(name = "application-service")
public interface AdminAppClient {

    @GetMapping("/api/internal/applications/stats")
    ApplicationStats getStats();
}
