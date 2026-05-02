package com.capg.jobportal.listener;

import com.capg.jobportal.event.PasswordResetEvent;
import com.capg.jobportal.service.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: RegistrationOtpListener
 * DESCRIPTION:
 * Listens to the registration.otp.queue and sends the email
 * verification OTP to the newly registered user.
 * ================================================================
 */
@Component
public class RegistrationOtpListener {

    private static final Logger logger = LogManager.getLogger(RegistrationOtpListener.class);
    private final EmailService emailService;

    public RegistrationOtpListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "registration.otp.queue")
    public void handleRegistrationOtp(PasswordResetEvent event) {
        try {
            logger.info("Received registration OTP event for email: {}", event.getEmail());
            emailService.sendRegistrationOtpEmail(event.getEmail(), event.getName(), event.getOtp());
            logger.info("Registration OTP email sent to: {}", event.getEmail());
        } catch (Exception e) {
            logger.error("FAILED to send registration OTP email to {}. Cause: {}", event.getEmail(), e.getMessage());
            logger.error("Please check your EMAIL and EMAIL_PASSWORD (App Password) in the .env file.");
        }
    }
}
