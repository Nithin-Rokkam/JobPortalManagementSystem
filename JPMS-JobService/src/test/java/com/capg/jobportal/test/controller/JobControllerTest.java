package com.capg.jobportal.test.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.JobController;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;
import com.capg.jobportal.service.JobService;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
// ... existing imports ...

@WebMvcTest(
    controllers = JobController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.capg.jobportal.security.SecurityConfig.class
    )
)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    void postJob_returns201() throws Exception {
        JobResponseDTO response = new JobResponseDTO();
        response.setId(1L);
        response.setTitle("Java Dev");
        when(jobService.postJob(any(), eq(10L), eq("RECRUITER"))).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "10")
                .header("X-User-Role", "RECRUITER")
                .content("{\"title\":\"Java Dev\",\"companyName\":\"Corp\",\"location\":\"NYC\",\"jobType\":\"FULL_TIME\",\"description\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Dev"));
    }

    @Test
    void getAllJobs_returns200() throws Exception {
        PagedResponse<JobResponseDTO> response = new PagedResponse<>(
                Collections.emptyList(), 0, 0, 0, true);
        when(jobService.getAllJobs(0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/jobs")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    void getJobById_returns200() throws Exception {
        JobResponseDTO response = new JobResponseDTO();
        response.setId(1L);
        response.setTitle("Java Dev");
        when(jobService.getJobById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/jobs/1")
                .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteJob_returns204() throws Exception {
        doNothing().when(jobService).deleteJob(1L, 10L, "RECRUITER");

        mockMvc.perform(delete("/api/jobs/1")
                .with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Id", "10")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMyJobs_returns200() throws Exception {
        PagedResponse<JobResponseDTO> response = new PagedResponse<>(
                Collections.emptyList(), 0, 0, 0, true);
        when(jobService.getMyJobs(10L, "RECRUITER", 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/jobs/my-jobs")
                .with(SecurityMockMvcRequestPostProcessors.anonymous())
                .header("X-User-Id", "10")
                .header("X-User-Role", "RECRUITER")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }
}
