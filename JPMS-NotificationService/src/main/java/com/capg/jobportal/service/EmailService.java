package com.capg.jobportal.service;

import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
=======
import org.springframework.beans.factory.annotation.Value;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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

<<<<<<< HEAD
=======
    @Value("${spring.mail.username}")
    private String senderEmail;

>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
    /* ================================================================
     * METHOD: sendJobAlert
     * DESCRIPTION:
     * Sends job alert email to job seekers when a new job is posted.
     * ================================================================ */
    public void sendJobAlert(String toEmail, JobPostedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
<<<<<<< HEAD
=======
        message.setFrom(senderEmail);
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
=======
        message.setFrom(senderEmail);
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
=======
        message.setFrom(senderEmail);
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
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
<<<<<<< HEAD
=======

    public void sendOtpEmail(String toEmail, String name, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("Joblix - Password Reset OTP");
        message.setText("Hello " + name + ",\n\n" +
                "You have requested to reset your password. Use the following OTP to complete the process:\n\n" +
                "OTP: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\nJoblix Team");
        mailSender.send(message);
    }

    /* ================================================================
     * METHOD: sendRegistrationOtpEmail
     * DESCRIPTION:
     * Sends a 6-digit OTP to the user's email for account verification
     * during the registration process.
     * ================================================================ */
    public void sendRegistrationOtpEmail(String toEmail, String name, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("Joblix - Verify Your Email Address");
        message.setText("Hello " + name + ",\n\n" +
                "Welcome to Joblix! To complete your registration, please verify your email address using the OTP below:\n\n" +
                "OTP: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not create an account on Joblix, please ignore this email.\n\n" +
                "Best regards,\nJoblix Team");
        mailSender.send(message);
    }

    /* ================================================================
     * METHOD: sendShortlistedEmail
     * DESCRIPTION:
     * Sends a congratulatory email to the applicant when they are
     * shortlisted for a job.
     * ================================================================ */
    public void sendShortlistedEmail(com.capg.jobportal.event.ApplicationStatusChangedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(event.getSeekerEmail());
        message.setSubject("Congratulations! You've been Shortlisted for " + event.getJobTitle());
        
        String body = "Hello " + event.getSeekerName() + ",\n\n" +
                      "Great news! Your profile has been shortlisted for the next round of the selection process for the position of '" + event.getJobTitle() + "'.\n\n" +
                      "Congratulations! Your profile got shortlisted for the test/interview round. Our team will contact you shortly with the schedule and further details.\n\n" +
                      "Keep an eye on your dashboard for any further updates.\n\n" +
                      "Best of luck!\n\n" +
                      "Best regards,\n" +
                      "Job Portal Team";
        
        message.setText(body);
        mailSender.send(message);
    }

    /* ================================================================
     * METHOD: sendRejectedEmail
     * DESCRIPTION:
     * Sends a formal and encouraging rejection email to the applicant.
     * ================================================================ */
    public void sendRejectedEmail(com.capg.jobportal.event.ApplicationStatusChangedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(event.getSeekerEmail());
        message.setSubject("Update regarding your application for " + event.getJobTitle());
        
        String body = "Hello " + event.getSeekerName() + ",\n\n" +
                      "Thank you for your interest in the position of '" + event.getJobTitle() + "' and for the time you invested in the application process.\n\n" +
                      "We regret to inform you that we will not be moving forward with your application at this time as your current skill set does not exactly match our requirements for this specific role.\n\n" +
                      "Please do not be discouraged. We were impressed with your background, and we encourage you to keep working hard and refining your skills. New opportunities arise frequently, and we would welcome you to apply for other roles that match your expertise in the future.\n\n" +
                      "We wish you the very best in your job search and professional journey.\n\n" +
                      "Best regards,\n" +
                      "Job Portal Team";
        
        message.setText(body);
        mailSender.send(message);
    }

    /* ================================================================
     * METHOD: sendSelectedEmail
     * DESCRIPTION:
     * Sends a final selection email to the applicant.
     * ================================================================ */
    public void sendSelectedEmail(com.capg.jobportal.event.ApplicationStatusChangedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(event.getSeekerEmail());
        message.setSubject("Selection Confirmation: " + event.getJobTitle());
        
        String body = "Hello " + event.getSeekerName() + ",\n\n" +
                      "We are absolutely thrilled to inform you that you have been SELECTED for the position of '" + event.getJobTitle() + "'!\n\n" +
                      "Our HR team was highly impressed with your profile and performance throughout the selection process. We believe you will be a fantastic addition to the team.\n\n" +
                      "What's next?\n" +
                      "Our HR department will contact you shortly regarding the offer letter, compensation details, and the onboarding process.\n\n" +
                      "Congratulations once again on this achievement! We look forward to having you on board.\n\n" +
                      "Best regards,\n" +
                      "Job Portal Team";
        
        message.setText(body);
        mailSender.send(message);
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}