package com.capg.jobportal.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.capg.jobportal.event.ApplicationStatusChangedEvent;
import com.capg.jobportal.service.EmailService;

@Component
public class ApplicationStatusChangedListener {

    private static final Logger logger = LogManager.getLogger(ApplicationStatusChangedListener.class);

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = "${rabbitmq.status.queue}")
    public void handleStatusChange(ApplicationStatusChangedEvent event) {
        logger.info("Received status change event for application [{}], new status: {}", 
                    event.getApplicationId(), event.getNewStatus());
        
        if ("SHORTLISTED".equals(event.getNewStatus())) {
            try {
                emailService.sendShortlistedEmail(event);
                logger.info("Shortlisted email sent successfully to {}", event.getSeekerEmail());
            } catch (Exception e) {
                logger.error("Failed to send shortlisted email: {}", e.getMessage());
            }
        } else if ("REJECTED".equals(event.getNewStatus())) {
            try {
                emailService.sendRejectedEmail(event);
                logger.info("Rejection email sent successfully to {}", event.getSeekerEmail());
            } catch (Exception e) {
                logger.error("Failed to send rejection email: {}", e.getMessage());
            }
        } else if ("SELECTED".equals(event.getNewStatus())) {
            try {
                emailService.sendSelectedEmail(event);
                logger.info("Selection email sent successfully to {}", event.getSeekerEmail());
            } catch (Exception e) {
                logger.error("Failed to send selection email: {}", e.getMessage());
            }
        }
    }
}
