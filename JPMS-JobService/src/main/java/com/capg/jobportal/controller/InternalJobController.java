package com.capg.jobportal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.service.JobService;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
public class InternalJobController {

    private final JobService jobService;

    public InternalJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs/all")
    public ResponseEntity<List<JobResponseDTO>> getAllJobsForAdmin() {
        List<JobResponseDTO> jobs = jobService.getAllJobsForAdmin();
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJobByAdmin(@PathVariable Long id) {
        jobService.deleteJobByAdmin(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}