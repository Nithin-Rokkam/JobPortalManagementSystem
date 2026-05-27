package com.capg.jobportal.test.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.InternalJobController;
import com.capg.jobportal.dto.JobResponseDTO;
import com.capg.jobportal.service.JobService;

@WebMvcTest(
    controllers = InternalJobController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.capg.jobportal.security.SecurityConfig.class
    )
)
class InternalJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    void getAllJobsForAdmin_returns200() throws Exception {
        JobResponseDTO job = new JobResponseDTO();
        job.setId(1L);
        when(jobService.getAllJobsForAdmin()).thenReturn(Collections.singletonList(job));

        mockMvc.perform(get("/api/internal/jobs/all"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJobByAdmin_returns200() throws Exception {
        doNothing().when(jobService).deleteJobByAdmin(anyLong());

        mockMvc.perform(delete("/api/internal/jobs/1"))
                .andExpect(status().isOk());
    }
}
