package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.model.RefreshToken;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.repository.UserRepository;
import com.spotseeker.copliot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;

    @Transactional
    public LoginResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already exists");
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            throw new BadCredentialsException("Mobile number already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(User.UserType.PARTNER);
        user.setStatus(User.UserStatus.PENDING);
        user.setProfileComplete(false);
        user.setMobileVerified(false);

        user = userRepository.save(user);

        // Create empty partner profile
        Partner partner = new Partner();
        partner.setUser(user);
        partnerRepository.save(partner);

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getUserType().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(LoginResponseDto.UserDto.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .userType(user.getUserType().name().toLowerCase())
                        .status(user.getStatus().name().toLowerCase())
                        .profileComplete(user.getProfileComplete())
                        .build())
                .build();
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getUserType().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(LoginResponseDto.UserDto.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .userType(user.getUserType().name().toLowerCase())
                        .status(user.getStatus().name().toLowerCase())
                        .profileComplete(user.getProfileComplete())
                        .build())
                .build();
    }

    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getUserType().name());

        return new RefreshTokenResponseDto(accessToken);
    }

    public VerifyMobileResponseDto verifyMobile(VerifyMobileDto request) {
        // Verify OTP using existing OTP service
        boolean verified = otpService.verifyOtpCode(request.getMobile(), request.getOtp());

        if (!verified) {
            throw new BadCredentialsException("Invalid OTP");
        }

        // Update user mobile verification status
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setMobileVerified(true);
        userRepository.save(user);

        // Generate temporary token
        String tempToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getUserType().name());

        return VerifyMobileResponseDto.builder()
                .verified(true)
                .tempToken(tempToken)
                .build();
    }
}