package com.capg.jobportal.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.UserInfoResponse;
import com.capg.jobportal.event.JobPostedEvent;
import com.capg.jobportal.service.EmailService;

@ExtendWith(MockitoExtension.class)
class JobPostedListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private JobPostedListener listener;

    @Test
    void handleJobPosted_withRecruiterAndSeekers() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobId(1L);
        event.setRecruiterId(50L);
        event.setTitle("Java Dev");
        event.setCompanyName("TechCorp");
        event.setLocation("NYC");
        event.setJobType("FULL_TIME");

        UserInfoResponse recruiter = new UserInfoResponse();
        recruiter.setEmail("recruiter@test.com");

        when(authServiceClient.getUserInfo(50L)).thenReturn(recruiter);
        when(authServiceClient.getJobSeekerEmails())
                .thenReturn(Arrays.asList("seeker1@test.com", "seeker2@test.com"));

        listener.handleJobPosted(event);

        verify(emailService).sendJobPostedConfirmation("recruiter@test.com", event);
        verify(emailService, times(2)).sendJobAlert(anyString(), eq(event));
    }

    @Test
    void handleJobPosted_nullRecruiterId_skipsConfirmation() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobId(1L);
        event.setRecruiterId(null);
        event.setTitle("Java Dev");

        when(authServiceClient.getJobSeekerEmails()).thenReturn(List.of("seeker@test.com"));

        listener.handleJobPosted(event);

        verify(emailService, never()).sendJobPostedConfirmation(anyString(), any());
        verify(emailService).sendJobAlert(eq("seeker@test.com"), eq(event));
    }

    @Test
    void handleJobPosted_emptyEmailList() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobId(1L);
        event.setRecruiterId(null);
        event.setTitle("Java Dev");

        when(authServiceClient.getJobSeekerEmails()).thenReturn(List.of());

        listener.handleJobPosted(event);

        verify(emailService, never()).sendJobAlert(anyString(), any());
    }

    @Test
    void handleJobPosted_nullEmailList() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobId(1L);
        event.setRecruiterId(null);
        event.setTitle("Java Dev");

        when(authServiceClient.getJobSeekerEmails()).thenReturn(null);

        listener.handleJobPosted(event);

        verify(emailService, never()).sendJobAlert(anyString(), any());
    }

    @Test
    void handleJobPosted_recruiterConfirmationFails_continuesWithSeekers() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobId(1L);
        event.setRecruiterId(50L);
        event.setTitle("Java Dev");

        when(authServiceClient.getUserInfo(50L)).thenThrow(new RuntimeException("Recruiter service down"));
        when(authServiceClient.getJobSeekerEmails()).thenReturn(List.of("seeker@test.com"));

        listener.handleJobPosted(event);

        verify(emailService, never()).sendJobPostedConfirmation(anyString(), any());
        verify(emailService).sendJobAlert(eq("seeker@test.com"), eq(event));
    }

    @Test
    void handleJobPosted_seekerEmailFails_continuesWithOthers() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobId(1L);
        event.setRecruiterId(null);
        event.setTitle("Java Dev");

        when(authServiceClient.getJobSeekerEmails())
                .thenReturn(Arrays.asList("fail@test.com", "success@test.com"));
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendJobAlert(eq("fail@test.com"), any());

        listener.handleJobPosted(event);

        verify(emailService, times(2)).sendJobAlert(anyString(), eq(event));
    }
}
