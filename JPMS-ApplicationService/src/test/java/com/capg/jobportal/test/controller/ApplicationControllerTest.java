package com.capg.jobportal.test.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.ApplicationController;
import com.capg.jobportal.dto.ApplicationResponse;
import com.capg.jobportal.dto.RecruiterApplicationResponse;
import com.capg.jobportal.dto.StatusUpdateRequest;
import com.capg.jobportal.service.ApplicationService;

@WebMvcTest(
    controllers = ApplicationController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.capg.jobportal.security.SecurityConfig.class
    )
)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Test
    void applyForJob_returns201() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(applicationService.applyForJob(anyLong(), any(), anyBoolean(), any(), any(), anyLong())).thenReturn(response);

        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf", "application/pdf", "dummy".getBytes());

        mockMvc.perform(multipart("/api/applications")
                .file(resume)
                .param("jobId", "1")
                .header("X-User-Id", "100")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isCreated());
    }

    @Test
    void getMyApplications_returns200() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(applicationService.getMyApplications(anyLong())).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/applications/my-applications")
                .header("X-User-Id", "100")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    void getApplicationById_returns200() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(applicationService.getApplicationById(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/applications/1")
                .header("X-User-Id", "100")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    void getApplicantsForJob_returns200() throws Exception {
        RecruiterApplicationResponse response = new RecruiterApplicationResponse();
        response.setId(1L);
        when(applicationService.getApplicantsForJob(anyLong(), anyLong())).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/applications/job/1")
                .header("X-User-Id", "200")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void updateApplicationStatus_returns200() throws Exception {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(1L);
        when(applicationService.updateApplicationStatus(anyLong(), any(), anyLong())).thenReturn(response);

        com.capg.jobportal.dto.StatusUpdateRequest req = new com.capg.jobportal.dto.StatusUpdateRequest();
        req.setNewStatus(com.capg.jobportal.enums.ApplicationStatus.SHORTLISTED);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        mockMvc.perform(patch("/api/applications/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .header("X-User-Id", "200")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void applyForJob_returns403() throws Exception {
        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf", "application/pdf", "dummy".getBytes());
        mockMvc.perform(multipart("/api/applications")
                .file(resume)
                .param("jobId", "1")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyApplications_returns403() throws Exception {
        mockMvc.perform(get("/api/applications/my-applications")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getApplicationById_returns403() throws Exception {
        mockMvc.perform(get("/api/applications/1")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getApplicantsForJob_returns403() throws Exception {
        mockMvc.perform(get("/api/applications/job/1")
                .header("X-User-Id", "200")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateApplicationStatus_returns403() throws Exception {
        com.capg.jobportal.dto.StatusUpdateRequest req = new com.capg.jobportal.dto.StatusUpdateRequest();
        req.setNewStatus(com.capg.jobportal.enums.ApplicationStatus.SHORTLISTED);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        mockMvc.perform(patch("/api/applications/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .header("X-User-Id", "200")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }
}
