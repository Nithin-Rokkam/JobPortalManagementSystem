package com.capg.jobportal.test.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import com.capg.jobportal.controller.InternalApplicationController;
import com.capg.jobportal.dto.ApplicationStats;
import com.capg.jobportal.service.ApplicationService;

@WebMvcTest(
    controllers = InternalApplicationController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.capg.jobportal.security.SecurityConfig.class
    )
)
class InternalApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Test
    void getStats_returns200() throws Exception {
        ApplicationStats stats = new ApplicationStats();
        stats.setTotalApplications(100L);
        when(applicationService.getApplicationStats()).thenReturn(stats);

        mockMvc.perform(get("/api/internal/applications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(100));
    }
}
