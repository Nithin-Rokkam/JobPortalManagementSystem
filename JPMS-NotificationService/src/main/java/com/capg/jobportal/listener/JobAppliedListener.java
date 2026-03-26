package com.capg.jobportal.listener;

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
 * This listener class is responsible for consuming job application
 * events from RabbitMQ.
 *
 * It listens to the configured queue for JobAppliedEvent messages
 * and processes them asynchronously.
 *
 * On receiving an event:
 * 1. Fetches recruiter details from Auth Service.
 * 2. Sends an email notification to the recruiter about the job application.
 *
 * NOTE:
 * This class plays a key role in enabling event-driven communication
 * and decoupling between microservices.
 * ================================================================
 */
@Component
public class JobAppliedListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthServiceClient authServiceClient;

    @RabbitListener(queues = "${rabbitmq.applied.queue}")
    public void handleJobApplied(JobAppliedEvent event) {
        System.out.println("Received job applied event for job: " + event.getJobTitle());
        try {
            UserInfoResponse recruiter = authServiceClient.getUserInfo(event.getRecruiterId());
            emailService.sendApplicationAlert(recruiter.getEmail(), event);
            System.out.println("Application alert sent to recruiter: " + recruiter.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send application alert: " + e.getMessage());
        }
    }
}