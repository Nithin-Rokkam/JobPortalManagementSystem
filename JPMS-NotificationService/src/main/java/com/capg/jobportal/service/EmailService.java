package com.capg.jobportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.capg.jobportal.event.JobAppliedEvent;
import com.capg.jobportal.event.JobPostedEvent;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: EmailService
 * DESCRIPTION:
 * This service class is responsible for handling all email-related
 * operations in the Job Portal application.
 *
 * It sends:
 * 1. Job alert emails to job seekers.
 * 2. Application notification emails to recruiters.
 * 3. Job posting confirmation emails to recruiters.
 *
 * NOTE:
 * This service uses JavaMailSender for sending emails and is
 * triggered by event-driven components like listeners.
 * ================================================================
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /* ================================================================
     * METHOD: sendJobAlert
     * DESCRIPTION:
     * Sends job alert email to job seekers when a new job is posted.
     * ================================================================ */
    public void sendJobAlert(String toEmail, JobPostedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("New Job Alert: " + event.getTitle() + " at " + event.getCompanyName());
        message.setText(buildEmailBody(event));
        mailSender.send(message);
    }

    
    /* ================================================================
     * METHOD: buildEmailBody
     * DESCRIPTION:
     * Constructs the email body for job alert notifications.
     * ================================================================ */
    private String buildEmailBody(JobPostedEvent event) {
        return "Hello Job Seeker,\n\n" +
               "A new job has been posted that might interest you!\n\n" +
               "Job Title    : " + event.getTitle() + "\n" +
               "Company      : " + event.getCompanyName() + "\n" +
               "Location     : " + event.getLocation() + "\n" +
               "Job Type     : " + event.getJobType() + "\n" +
               "Salary       : " + (event.getSalary() != null ? event.getSalary() : "Not specified") + "\n" +
               "Experience   : " + (event.getExperienceYears() != null ? event.getExperienceYears() + " years" : "Not specified") + "\n\n" +
               "Description  : " + event.getDescription() + "\n\n" +
               "Log in to the Job Portal to apply now!\n\n" +
               "Best regards,\nJob Portal Team";
    }

    
    /* ================================================================
     * METHOD: sendApplicationAlert
     * DESCRIPTION:
     * Sends an email to the recruiter when a job seeker applies
     * for a job.
     * ================================================================ */
    public void sendApplicationAlert(String recruiterEmail, JobAppliedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recruiterEmail);
        message.setSubject("New Application Received: " + event.getJobTitle());
        message.setText(buildApplicationEmailBody(event));
        mailSender.send(message);
    }

    
    /* ================================================================
     * METHOD: buildApplicationEmailBody
     * DESCRIPTION:
     * Constructs the email body for job application notifications.
     * ================================================================ */
    private String buildApplicationEmailBody(JobAppliedEvent event) {
        return "Hello Recruiter,\n\n" +
               "You have received a new application for your job posting!\n\n" +
               "Job Title    : " + event.getJobTitle() + "\n" +
               "Applicant    : " + event.getSeekerName() + "\n" +
               "Email        : " + event.getSeekerEmail() + "\n\n" +
               "Log in to the Job Portal to review the application.\n\n" +
               "Best regards,\nJob Portal Team";
    }

    
    /* ================================================================
     * METHOD: sendJobPostedConfirmation
     * DESCRIPTION:
     * Sends a confirmation email to the recruiter after successfully
     * posting a job.
     * ================================================================ */
    public void sendJobPostedConfirmation(String recruiterEmail, JobPostedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recruiterEmail);
        message.setSubject("Job Posted Successfully: " + event.getTitle());
        message.setText(
            "Hello Recruiter,\n\n" +
            "Your job posting has been published successfully!\n\n" +
            "Job Title    : " + event.getTitle() + "\n" +
            "Company      : " + event.getCompanyName() + "\n" +
            "Location     : " + event.getLocation() + "\n" +
            "Job Type     : " + event.getJobType() + "\n" +
            "Salary       : " + (event.getSalary() != null ? event.getSalary() : "Not specified") + "\n" +
            "Experience   : " + (event.getExperienceYears() != null ? event.getExperienceYears() + " years" : "Not specified") + "\n\n" +
            "Job seekers are being notified about your posting.\n\n" +
            "Best regards,\nJob Portal Team"
        );
        mailSender.send(message);
    }
}