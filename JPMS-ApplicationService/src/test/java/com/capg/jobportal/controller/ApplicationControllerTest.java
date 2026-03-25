package com.capg.jobportal.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.RecruiterApplicationResponse;
import com.capg.jobportal.dto.StatusUpdateRequest;
import com.capg.jobportal.enums.ApplicationStatus;
import com.capg.jobportal.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = ApplicationController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. POST /api/applications
    @Test
    void applyForJob_asJobSeeker_returnsCreated() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        response.setStatus(ApplicationStatus.APPLIED);

        when(applicationService.applyForJob(anyLong(), anyString(), anyBoolean(), any(), any(), anyLong()))
                .thenReturn(response);

        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf", "application/pdf", "dummy content".getBytes());

        mockMvc.perform(multipart("/api/applications")
                .file(resume)
                .param("jobId", "100")
                .param("coverLetter", "Hello")
                .param("useExistingResume", "false")
                .header("X-User-Id", 10L)
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void applyForJob_asRecruiter_returnsForbidden() throws Exception {
        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf", "application/pdf", "dummy content".getBytes());

        mockMvc.perform(multipart("/api/applications")
                .file(resume)
                .param("jobId", "100")
                .header("X-User-Id", 20L)
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    // 2. GET /api/applications/my-applications
    @Test
    void getMyApplications_asJobSeeker_returnsOk() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(applicationService.getMyApplications(anyLong())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/applications/my-applications")
                .header("X-User-Id", 10L)
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // 3. GET /api/applications/{id}
    @Test
    void getApplicationById_asJobSeeker_returnsOk() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(5L);
        when(applicationService.getApplicationById(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/applications/5")
                .header("X-User-Id", 10L)
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    // 4. GET /api/applications/job/{jobId}
    @Test
    void getApplicantsForJob_asRecruiter_returnsOk() throws Exception {
        RecruiterApplicationResponse response = new RecruiterApplicationResponse();
        response.setId(7L);
        when(applicationService.getApplicantsForJob(anyLong(), anyLong())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/applications/job/100")
                .header("X-User-Id", 20L)
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    void getApplicantsForJob_asJobSeeker_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/applications/job/100")
                .header("X-User-Id", 10L)
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    // 5. PATCH /api/applications/{id}/status
    @Test
    void updateApplicationStatus_asRecruiter_returnsOk() throws Exception {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setNewStatus(ApplicationStatus.SHORTLISTED);

        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        response.setStatus(ApplicationStatus.SHORTLISTED);

        when(applicationService.updateApplicationStatus(anyLong(), any(), anyLong())).thenReturn(response);

        mockMvc.perform(patch("/api/applications/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Id", 20L)
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }
}
