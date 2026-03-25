package com.capg.jobportal.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.dto.RecruiterApplicationResponse;
import com.capg.jobportal.dto.StatusUpdateRequest;
import com.capg.jobportal.service.ApplicationService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

	private final ApplicationService applicationService;
	 
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
 
 
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplicationResponse> applyForJob(
            @RequestParam("jobId") Long jobId,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "useExistingResume", defaultValue = "false") boolean useExistingResume,
            @RequestParam(value = "existingResumeUrl", required = false) String existingResumeUrl,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) throws IOException {
 
        if (!role.equals("JOB_SEEKER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
 
        ApplicationResponse response = applicationService.applyForJob(
                jobId, coverLetter, useExistingResume, existingResumeUrl, resume, userId);
 
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
 
 
    @GetMapping("/my-applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
 
        if (!role.equals("JOB_SEEKER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
 
        List<ApplicationResponse> response = applicationService.getMyApplications(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
 
 
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
 
        if (!role.equals("JOB_SEEKER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
 
        ApplicationResponse response = applicationService.getApplicationById(id, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
 
 
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<RecruiterApplicationResponse>> getApplicantsForJob(
            @PathVariable Long jobId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
 
        if (!role.equals("RECRUITER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
 
        List<RecruiterApplicationResponse> response = applicationService.getApplicantsForJob(jobId, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
 
 
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
 
        if (!role.equals("RECRUITER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
 
        ApplicationResponse response = applicationService.updateApplicationStatus(id, request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
