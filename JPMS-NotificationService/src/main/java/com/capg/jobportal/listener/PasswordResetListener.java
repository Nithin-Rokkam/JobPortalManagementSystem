package com.capg.jobportal.listener;

import com.capg.jobportal.event.PasswordResetEvent;
import com.capg.jobportal.service.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetListener {

    private static final Logger logger = LogManager.getLogger(PasswordResetListener.class);
    private final EmailService emailService;

    public PasswordResetListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "password.reset.queue")
    public void handlePasswordReset(PasswordResetEvent event) {
        try {
            logger.info("Received password reset event for email: {}", event.getEmail());
            emailService.sendOtpEmail(event.getEmail(), event.getName(), event.getOtp());
            logger.info("Password reset email sent to: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("FAILED to send password reset email to {}. Cause: {}", event.getEmail(), e.getMessage());
            logger.error("Please check your EMAIL and EMAIL_PASSWORD (App Password) in the .env file.");
        }
    }
}
