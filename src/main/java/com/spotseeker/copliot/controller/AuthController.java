package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.service.AuthService;
import com.spotseeker.copliot.service.OtpService;
import com.spotseeker.copliot.service.PartnerRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;
    private final AuthService authService;
    private final PartnerRegistrationService partnerRegistrationService;

    public AuthController(OtpService otpService, AuthService authService,
                         PartnerRegistrationService partnerRegistrationService) {
        this.otpService = otpService;
        this.authService = authService;
        this.partnerRegistrationService = partnerRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        LoginResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mobile")
    public ResponseEntity<VerifyMobileResponseDto> verifyMobile(@Valid @RequestBody VerifyMobileDto request) {
        VerifyMobileResponseDto response = authService.verifyMobile(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        RefreshTokenResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
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

    // New Partner Registration Endpoints

    @PostMapping("/partner/register/step1-email")
    public ResponseEntity<PartnerRegistrationResponseDto> registerPartnerStep1(
            @Valid @RequestBody PartnerRegistrationEmailDto request) {
        PartnerRegistrationResponseDto response = partnerRegistrationService.submitEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/register/step2-mobile")
    public ResponseEntity<PartnerRegistrationResponseDto> registerPartnerStep2(
            @Valid @RequestBody PartnerRegistrationMobileDto request) {
        PartnerRegistrationResponseDto response = partnerRegistrationService.submitMobile(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/register/step3-verify-otp")
    public ResponseEntity<PartnerRegistrationResponseDto> registerPartnerStep3(
            @Valid @RequestBody PartnerRegistrationOtpDto request) {
        PartnerRegistrationResponseDto response = partnerRegistrationService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/register")
    public ResponseEntity<LoginResponseDto> registerAdmin(@Valid @RequestBody RegisterRequestDto request) {
        LoginResponseDto response = authService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }
}
