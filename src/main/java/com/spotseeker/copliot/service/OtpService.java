package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.AuthResponseDto;
import com.spotseeker.copliot.dto.OtpRequestDto;
import com.spotseeker.copliot.dto.OtpVerifyDto;
import com.spotseeker.copliot.exception.BadRequestException;
import com.spotseeker.copliot.model.Otp;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.OtpRepository;
import com.spotseeker.copliot.repository.UserRepository;
import com.spotseeker.copliot.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${otp.expiration}")
    private long otpExpirationMs;

    @Value("${otp.length}")
    private int otpLength;

    public OtpService(OtpRepository otpRepository, UserRepository userRepository,
                      JwtTokenProvider jwtTokenProvider) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void requestOtp(OtpRequestDto request) {
        // Delete any existing unverified OTPs for this phone number
        otpRepository.deleteByPhoneNumberAndVerifiedFalse(request.getPhoneNumber());

        // Generate OTP code
        String code = generateOtpCode();

        // Create and save OTP
        Otp otp = new Otp();
        otp.setPhoneNumber(request.getPhoneNumber());
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusSeconds(otpExpirationMs / 1000));
        otp.setVerified(false);

        otpRepository.save(otp);

        // In production, send OTP via SMS
        System.out.println("OTP for " + request.getPhoneNumber() + ": " + code);
    }

    @Transactional
    public AuthResponseDto verifyOtp(OtpVerifyDto request) {
        Otp otp = otpRepository.findByPhoneNumberAndCodeAndVerifiedFalseAndExpiresAtAfter(
                        request.getPhoneNumber(), request.getCode(), LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        // Mark OTP as verified
        otp.setVerified(true);
        otpRepository.save(otp);

        // Get or create user
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setPhoneNumber(request.getPhoneNumber());
                    return userRepository.save(newUser);
                });

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId().toString(), "user");

        return new AuthResponseDto(token, user);
    }

    @Transactional
    public void resendOtp(OtpRequestDto request) {
        requestOtp(request);
    }

    private String generateOtpCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
