package com.capg.jobportal.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capg.jobportal.dto.JobRequestDTO;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;
import com.capg.jobportal.service.JobService;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    // POST /api/jobs — RECRUITER only
    @PostMapping
    public ResponseEntity<JobResponseDTO> postJob(
            @RequestBody JobRequestDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        JobResponseDTO response = jobService.postJob(dto, userId, userRole);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET /api/jobs?page=0&size=10 — Public — paginated
    @GetMapping
    public ResponseEntity<PagedResponse<JobResponseDTO>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<JobResponseDTO> response = jobService.getAllJobs(page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // GET /api/jobs/{id} — Public
    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {

        JobResponseDTO response = jobService.getJobById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // GET /api/jobs/search?title=java&location=delhi&page=0&size=10 — Public — paginated
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<JobResponseDTO> response = jobService.searchJobs(
                title, location, jobType, experienceYears, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // PUT /api/jobs/{id} — RECRUITER + owner only
    @PutMapping("/{id}")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @RequestBody JobRequestDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        JobResponseDTO response = jobService.updateJob(id, dto, userId, userRole);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // DELETE /api/jobs/{id} — RECRUITER + owner only — soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {

        jobService.deleteJob(id, userId, userRole);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // GET /api/jobs/my-jobs?page=0&size=10 — RECRUITER only — paginated
    @GetMapping("/my-jobs")
    public ResponseEntity<PagedResponse<JobResponseDTO>> getMyJobs(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<JobResponseDTO> response = jobService.getMyJobs(
                userId, userRole, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}

