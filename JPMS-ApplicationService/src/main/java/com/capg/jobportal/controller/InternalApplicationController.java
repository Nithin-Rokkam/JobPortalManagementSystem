package com.capg.jobportal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.service.ApplicationService;

@RestController
@RequestMapping("/api/internal")
public class InternalApplicationController {

    private final ApplicationService applicationService;

    public InternalApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/applications/stats")
    public ResponseEntity<ApplicationStats> getStats() {
        ApplicationStats stats = applicationService.getApplicationStats();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}