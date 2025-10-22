package com.spotseeker.copliot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from.address}")
    private String fromEmail;

    @Value("${spring.mail.from.name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPartnerCredentials(String toEmail, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to SpotSeeker - Partner Account Approved");
            message.setText(buildCredentialsEmailBody(toEmail, password));

            mailSender.send(message);
            logger.info("Credentials email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send credentials email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendRegistrationConfirmation(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SpotSeeker Partner Registration - Pending Approval");
            message.setText(buildConfirmationEmailBody());

            mailSender.send(message);
            logger.info("Registration confirmation email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {}: {}", toEmail, e.getMessage());
            // Don't throw exception for confirmation email
        }
    }

    public void sendRejectionEmail(String toEmail, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SpotSeeker Partner Registration - Application Update");
            message.setText(buildRejectionEmailBody(reason));

            mailSender.send(message);
            logger.info("Rejection email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send rejection email to {}: {}", toEmail, e.getMessage());
            // Don't throw exception
        }
    }

    private String buildCredentialsEmailBody(String email, String password) {
        return String.format("""
                Dear Partner,
                
                Congratulations! Your partner registration request has been approved.
                
                You can now log in to the SpotSeeker Partner Portal using the following credentials:
                
                Email: %s
                Password: %s
                
                For security reasons, we recommend changing your password after your first login.
                
                Welcome to SpotSeeker!
                
                Best regards,
                SpotSeeker Team
                """, email, password);
    }

    private String buildConfirmationEmailBody() {
        return """
                Dear Partner,
                
                Thank you for submitting your partner registration request.
                
                Your application is currently under review by our team. You will receive an email
                notification once your application has been reviewed.
                
                This process typically takes 1-2 business days.
                
                Best regards,
                SpotSeeker Team
                """;
    }

    private String buildRejectionEmailBody(String reason) {
        return String.format("""
                Dear Applicant,
                
                Thank you for your interest in becoming a SpotSeeker partner.
                
                After careful review, we regret to inform you that we are unable to approve your
                partner registration at this time.
                
                Reason: %s
                
                If you have any questions or would like to reapply in the future, please contact
                our support team.
                
                Best regards,
                SpotSeeker Team
                """, reason != null ? reason : "Application did not meet our current requirements.");
    }
}
