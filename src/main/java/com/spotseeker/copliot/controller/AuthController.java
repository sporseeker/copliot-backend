package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.AuthResponseDto;
import com.spotseeker.copliot.dto.OtpRequestDto;
import com.spotseeker.copliot.dto.OtpVerifyDto;
import com.spotseeker.copliot.dto.PartnerLoginDto;
import com.spotseeker.copliot.service.OtpService;
import com.spotseeker.copliot.service.PartnerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;
    private final PartnerService partnerService;

    public AuthController(OtpService otpService, PartnerService partnerService) {
        this.otpService = otpService;
        this.partnerService = partnerService;
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        otpService.requestOtp(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        AuthResponseDto response = otpService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody OtpRequestDto request) {
        otpService.resendOtp(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP resent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/login")
    public ResponseEntity<AuthResponseDto> partnerLogin(@Valid @RequestBody PartnerLoginDto request) {
        AuthResponseDto response = partnerService.login(request);
        return ResponseEntity.ok(response);
    }
}
