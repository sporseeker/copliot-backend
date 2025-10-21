package com.spotseeker.copliot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${sms.api.token}")
    private String smsApiToken;

    @Value("${sms.api.sender}")
    private String smsSender;

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    private final RestTemplate restTemplate;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send SMS using send.lk API
     * @param phoneNumber The recipient phone number (e.g., +94719000492)
     * @param message The message to send
     * @return true if SMS was sent successfully, false otherwise
     */
    public boolean sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            logger.info("SMS sending is disabled. Would have sent to {}: {}", phoneNumber, message);
            return true;
        }

        try {
            // Format phone number to ensure it has + prefix
            String formattedPhone = formatPhoneNumber(phoneNumber);

            // Build the URL with query parameters
            String url = UriComponentsBuilder.fromHttpUrl(smsApiUrl)
                    .queryParam("to", formattedPhone)
                    .queryParam("from", smsSender)
                    .queryParam("message", message)
                    .queryParam("token", smsApiToken)
                    .build()
                    .toUriString();

            logger.info("Sending SMS to {}", formattedPhone);

            // Make the GET request
            String response = restTemplate.getForObject(url, String.class);

            logger.info("SMS sent successfully to {}. Response: {}", formattedPhone, response);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send OTP SMS
     * @param phoneNumber The recipient phone number
     * @param otpCode The OTP code
     * @return true if SMS was sent successfully
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode) {
        String message = String.format("Your SPOTSEEKER verification code is: %s. Valid for 5 minutes. Do not share this code with anyone.", otpCode);
        return sendSms(phoneNumber, message);
    }

    /**
     * Format phone number to ensure it has the + prefix
     * @param phoneNumber The phone number to format
     * @return Formatted phone number with + prefix
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Remove any whitespace
        phoneNumber = phoneNumber.trim();

        // If it doesn't start with +, add it
        if (!phoneNumber.startsWith("+")) {
            // If it starts with 94 (Sri Lanka), add +
            if (phoneNumber.startsWith("94")) {
                phoneNumber = "+" + phoneNumber;
            }
            // If it starts with 0, replace with +94
            else if (phoneNumber.startsWith("0")) {
                phoneNumber = "+94" + phoneNumber.substring(1);
            }
            // Otherwise, assume it needs +94 prefix
            else {
                phoneNumber = "+94" + phoneNumber;
            }
        }

        return phoneNumber;
    }
}

