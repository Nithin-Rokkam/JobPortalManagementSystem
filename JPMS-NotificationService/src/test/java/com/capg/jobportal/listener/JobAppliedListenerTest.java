package com.capg.jobportal.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.UserInfoResponse;
import com.capg.jobportal.event.JobAppliedEvent;
import com.capg.jobportal.service.EmailService;

@ExtendWith(MockitoExtension.class)
class JobAppliedListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private JobAppliedListener listener;

    @Test
    void handleJobApplied_sendsAlert() {
        JobAppliedEvent event = new JobAppliedEvent();
        event.setJobId(1L);
        event.setJobTitle("Java Dev");
        event.setSeekerId(100L);
        event.setSeekerName("John");
        event.setSeekerEmail("john@test.com");
        event.setRecruiterId(50L);

        UserInfoResponse recruiter = new UserInfoResponse();
        recruiter.setId(50L);
        recruiter.setName("Recruiter");
        recruiter.setEmail("recruiter@test.com");

        when(authServiceClient.getUserInfo(50L)).thenReturn(recruiter);

        listener.handleJobApplied(event);

        verify(emailService).sendApplicationAlert("recruiter@test.com", event);
    }

    @Test
    void handleJobApplied_failsGracefully() {
        JobAppliedEvent event = new JobAppliedEvent();
        event.setJobId(1L);
        event.setJobTitle("Java Dev");
        event.setRecruiterId(50L);

        when(authServiceClient.getUserInfo(50L)).thenThrow(new RuntimeException("Service down"));

        // Should not throw - catches exception internally
        listener.handleJobApplied(event);

        verify(emailService, never()).sendApplicationAlert(anyString(), any());
    }
}
