package com.capg.jobportal.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.capg.jobportal.event.JobAppliedEvent;
import com.capg.jobportal.event.JobPostedEvent;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendJobAlert_success() {
        JobPostedEvent event = new JobPostedEvent();
        event.setTitle("Java Dev");
        event.setCompanyName("TechCorp");
        event.setLocation("NYC");
        event.setJobType("FULL_TIME");
        event.setSalary(new BigDecimal("100000"));
        event.setExperienceYears(3);
        event.setDescription("Java Dev role");

        emailService.sendJobAlert("seeker@test.com", event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendJobAlert_withNullSalaryAndExperience() {
        JobPostedEvent event = new JobPostedEvent();
        event.setTitle("Java Dev");
        event.setCompanyName("TechCorp");
        event.setLocation("NYC");
        event.setJobType("FULL_TIME");
        event.setSalary(null);
        event.setExperienceYears(null);
        event.setDescription("desc");

        emailService.sendJobAlert("seeker@test.com", event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendApplicationAlert_success() {
        JobAppliedEvent event = new JobAppliedEvent();
        event.setJobTitle("Java Dev");
        event.setSeekerName("John");
        event.setSeekerEmail("john@test.com");

        emailService.sendApplicationAlert("recruiter@test.com", event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendJobPostedConfirmation_success() {
        JobPostedEvent event = new JobPostedEvent();
        event.setTitle("Java Dev");
        event.setCompanyName("TechCorp");
        event.setLocation("NYC");
        event.setJobType("FULL_TIME");
        event.setSalary(new BigDecimal("100000"));
        event.setExperienceYears(3);

        emailService.sendJobPostedConfirmation("recruiter@test.com", event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendJobPostedConfirmation_withNullSalaryAndExperience() {
        JobPostedEvent event = new JobPostedEvent();
        event.setTitle("Java Dev");
        event.setCompanyName("TechCorp");
        event.setLocation("NYC");
        event.setJobType("FULL_TIME");
        event.setSalary(null);
        event.setExperienceYears(null);

        emailService.sendJobPostedConfirmation("recruiter@test.com", event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
