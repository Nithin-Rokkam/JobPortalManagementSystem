package com.capg.jobportal.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.capg.jobportal.client.AuthServiceClient;
import com.capg.jobportal.dto.UserInfoResponse;
import com.capg.jobportal.event.JobAppliedEvent;
import com.capg.jobportal.service.EmailService;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: JobAppliedListener
 * DESCRIPTION:
 * Consumes job application events from RabbitMQ and sends an
 * email notification to the recruiter about the new application.
 * ================================================================
 */
@Component
public class JobAppliedListener {

    private static final Logger logger = LoggerFactory.getLogger(JobAppliedListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthServiceClient authServiceClient;

    @RabbitListener(queues = "${rabbitmq.applied.queue}")
    public void handleJobApplied(JobAppliedEvent event) {
        logger.info("Received job applied event for job: {}", event.getJobTitle());
        try {
            UserInfoResponse recruiter = authServiceClient.getUserInfo(event.getRecruiterId());
            emailService.sendApplicationAlert(recruiter.getEmail(), event);
            logger.info("Application alert sent to recruiter: {}", recruiter.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send application alert: {}", e.getMessage(), e);
        }
    }
}
