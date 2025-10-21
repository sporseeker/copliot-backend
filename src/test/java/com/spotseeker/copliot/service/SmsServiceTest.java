package com.spotseeker.copliot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "sms.enabled=false",
        "sms.api.url=https://send.lk/sms/send.php",
        "sms.api.token=test-token",
        "sms.api.sender=SPOTSEEKER"
})
class SmsServiceTest {

    @Autowired
    private SmsService smsService;

    @Test
    void testSendOtpSms_whenDisabled_shouldReturnTrue() {
        // Given
        String phoneNumber = "+94719000492";
        String otpCode = "123456";

        // When
        boolean result = smsService.sendOtpSms(phoneNumber, otpCode);

        // Then
        assertTrue(result, "SMS sending should succeed when disabled (mock mode)");
    }

    @Test
    void testSendSms_whenDisabled_shouldReturnTrue() {
        // Given
        String phoneNumber = "+94719000492";
        String message = "Test message";

        // When
        boolean result = smsService.sendSms(phoneNumber, message);

        // Then
        assertTrue(result, "SMS sending should succeed when disabled (mock mode)");
    }

    @Test
    void testSendOtpSms_withDifferentPhoneFormats() {
        // Test with + prefix
        assertTrue(smsService.sendOtpSms("+94719000492", "123456"));

        // Test without + prefix
        assertTrue(smsService.sendOtpSms("94719000492", "123456"));

        // Test with leading 0
        assertTrue(smsService.sendOtpSms("0719000492", "123456"));

        // Test with just the number
        assertTrue(smsService.sendOtpSms("719000492", "123456"));
    }
}

