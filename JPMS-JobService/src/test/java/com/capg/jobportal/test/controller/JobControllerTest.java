package com.capg.jobportal.test.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.JobController;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.dto.PagedResponse;
import com.capg.jobportal.service.JobService;

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
        when(jobService.postJob(any(), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Java Dev\"}")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllJobs_returns200() throws Exception {
        PagedResponse<JobResponseDTO> paged = new PagedResponse<>(Collections.emptyList(), 0, 10, 0L, true);
        when(jobService.getAllJobs(anyInt(), anyInt())).thenReturn(paged);

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void getJobById_returns200() throws Exception {
        JobResponseDTO response = new JobResponseDTO();
        when(jobService.getJobById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk());
    }

    @Test
    void searchJobs_returns200() throws Exception {
        PagedResponse<JobResponseDTO> paged = new PagedResponse<>(Collections.emptyList(), 0, 10, 0L, true);
        when(jobService.searchJobs(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(paged);

        mockMvc.perform(get("/api/jobs/search?title=Java"))
                .andExpect(status().isOk());
    }

    @Test
    void updateJob_returns200() throws Exception {
        JobResponseDTO response = new JobResponseDTO();
        when(jobService.updateJob(anyLong(), any(), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/jobs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\"}")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJob_returns204() throws Exception {
        doNothing().when(jobService).deleteJob(anyLong(), anyLong(), anyString());

        mockMvc.perform(delete("/api/jobs/1")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMyJobs_returns200() throws Exception {
        PagedResponse<JobResponseDTO> paged = new PagedResponse<>(Collections.emptyList(), 0, 10, 0L, true);
        when(jobService.getMyJobs(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(paged);

        mockMvc.perform(get("/api/jobs/my-jobs")
                .header("X-User-Id", "100")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }
}
