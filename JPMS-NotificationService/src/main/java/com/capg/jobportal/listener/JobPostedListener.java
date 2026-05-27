package com.capg.jobportal.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.UserInfoResponse;
import com.capg.jobportal.event.JobPostedEvent;
import com.capg.jobportal.service.EmailService;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobPostedListener
 * DESCRIPTION:
 * Consumes job posted events from RabbitMQ and sends email
 * notifications to the recruiter and all active job seekers.
 * ================================================================
 */
@Component
public class JobPostedListener {

    private static final Logger logger = LoggerFactory.getLogger(JobPostedListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthServiceClient authServiceClient;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleJobPosted(JobPostedEvent event) {
        logger.info("Received job posted event: {}", event.getTitle());

        try {
            if (event.getRecruiterId() != null) {
                try {
                    UserInfoResponse recruiter = authServiceClient.getUserInfo(event.getRecruiterId());
                    emailService.sendJobPostedConfirmation(recruiter.getEmail(), event);
                    logger.info("Confirmation email sent to recruiter: {}", recruiter.getEmail());
                } catch (Exception e) {
                    logger.warn("Failed to send confirmation email to recruiter: {}", e.getMessage());
                }
            }

            List<String> emails = authServiceClient.getJobSeekerEmails();

            if (emails != null && !emails.isEmpty()) {
                logger.info("Sending job alert to {} job seekers", emails.size());
                for (String email : emails) {
                    try {
                        emailService.sendJobAlert(email, event);
                        logger.debug("Job alert sent to: {}", email);
                    } catch (Exception e) {
                        logger.warn("Failed to send job alert to {}: {}", email, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error processing job posted event: {}", e.getMessage(), e);
        }
    }
}
