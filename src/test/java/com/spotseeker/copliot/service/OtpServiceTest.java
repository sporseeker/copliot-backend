package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.OtpRequestDto;
import com.spotseeker.copliot.dto.OtpVerifyDto;
import com.spotseeker.copliot.dto.AuthResponseDto;
import com.spotseeker.copliot.exception.BadRequestException;
import com.spotseeker.copliot.model.Otp;
import com.spotseeker.copliot.repository.OtpRepository;
import com.spotseeker.copliot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OtpServiceTest {

    @Autowired
    private OtpService otpService;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRequestOtp_Success() {
        OtpRequestDto request = new OtpRequestDto("+94719000492");

        otpService.requestOtp(request);

        assertTrue(otpRepository.findAll().stream()
                .anyMatch(otp -> otp.getPhoneNumber().equals("+94719000492")));
    }

    @Test
    void testVerifyOtp_ValidOtp() {
        String phoneNumber = "+94719000492";
        OtpRequestDto request = new OtpRequestDto(phoneNumber);
        otpService.requestOtp(request);

        Otp otp = otpRepository.findAll().stream()
                .filter(o -> o.getPhoneNumber().equals(phoneNumber))
                .findFirst()
                .orElseThrow();

        OtpVerifyDto verifyRequest = new OtpVerifyDto(phoneNumber, otp.getCode());
        AuthResponseDto response = otpService.verifyOtp(verifyRequest);

        assertNotNull(response.getToken());
        assertTrue(userRepository.findByMobile(phoneNumber).isPresent());
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        OtpVerifyDto request = new OtpVerifyDto("+94719000492", "999999");

        assertThrows(BadRequestException.class, () -> otpService.verifyOtp(request));
    }
}
