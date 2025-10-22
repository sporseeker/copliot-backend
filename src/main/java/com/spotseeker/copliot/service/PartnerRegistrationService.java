package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.exception.BadRequestException;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.*;
import com.spotseeker.copliot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PartnerRegistrationService {

    private final PartnerRegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Step 1: Submit email for registration
     */
    @Transactional
    public PartnerRegistrationResponseDto submitEmail(PartnerRegistrationEmailDto request) {
        // Check if email already exists in users table
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Check if email already has a pending request
        if (registrationRequestRepository.existsByEmail(request.getEmail())) {
            PartnerRegistrationRequest existing = registrationRequestRepository
                    .findByEmail(request.getEmail())
                    .orElseThrow();

            if (existing.getStatus() == PartnerRegistrationRequest.RequestStatus.PENDING_APPROVAL) {
                throw new BadRequestException("Registration request already pending approval");
            }

            if (existing.getStatus() == PartnerRegistrationRequest.RequestStatus.APPROVED) {
                throw new BadRequestException("Email already approved. Please login.");
            }

            // Allow resubmission if rejected or incomplete
            existing.setStatus(PartnerRegistrationRequest.RequestStatus.EMAIL_SUBMITTED);
            existing.setEmailVerified(true);
            existing.setMobileVerified(false);
            existing.setOtpVerified(false);
            registrationRequestRepository.save(existing);

            return PartnerRegistrationResponseDto.builder()
                    .requestId(existing.getId())
                    .email(existing.getEmail())
                    .status(existing.getStatus().name())
                    .message("Email verified. Please proceed to submit mobile number.")
                    .requiresOtp(false)
                    .requiresApproval(false)
                    .build();
        }

        // Create new registration request
        PartnerRegistrationRequest registrationRequest = new PartnerRegistrationRequest();
        registrationRequest.setEmail(request.getEmail());
        registrationRequest.setEmailVerified(true);
        registrationRequest.setStatus(PartnerRegistrationRequest.RequestStatus.EMAIL_SUBMITTED);
        registrationRequest = registrationRequestRepository.save(registrationRequest);

        return PartnerRegistrationResponseDto.builder()
                .requestId(registrationRequest.getId())
                .email(registrationRequest.getEmail())
                .status(registrationRequest.getStatus().name())
                .message("Email verified. Please proceed to submit mobile number.")
                .requiresOtp(false)
                .requiresApproval(false)
                .build();
    }

    /**
     * Step 2: Submit mobile number and request OTP
     */
    @Transactional
    public PartnerRegistrationResponseDto submitMobile(PartnerRegistrationMobileDto request) {
        // Find registration request by email
        PartnerRegistrationRequest registrationRequest = registrationRequestRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found. Please start with email submission."));

        // Validate status
        if (registrationRequest.getStatus() == PartnerRegistrationRequest.RequestStatus.APPROVED) {
            throw new BadRequestException("Registration already approved. Please login.");
        }

        // Check if mobile already exists
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new BadRequestException("Mobile number already registered");
        }

        // Check if another registration request has this mobile
        if (registrationRequestRepository.existsByMobile(request.getMobile())) {
            PartnerRegistrationRequest existing = registrationRequestRepository
                    .findByMobile(request.getMobile())
                    .orElseThrow();
            if (!existing.getId().equals(registrationRequest.getId())) {
                throw new BadRequestException("Mobile number already in use by another registration request");
            }
        }

        // Update registration request
        registrationRequest.setMobile(request.getMobile());
        registrationRequest.setMobileVerified(true);
        registrationRequest.setStatus(PartnerRegistrationRequest.RequestStatus.MOBILE_SUBMITTED);
        registrationRequestRepository.save(registrationRequest);

        // Generate and send OTP
        String otpCode = generateOtpCode();

        // Delete any existing unverified OTPs
        otpRepository.deleteByPhoneNumberAndVerifiedFalse(request.getMobile());

        // Create and save OTP
        Otp otp = new Otp();
        otp.setPhoneNumber(request.getMobile());
        otp.setCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        otpRepository.save(otp);

        // Send OTP via SMS
        smsService.sendOtpSms(request.getMobile(), otpCode);

        return PartnerRegistrationResponseDto.builder()
                .requestId(registrationRequest.getId())
                .email(registrationRequest.getEmail())
                .mobile(registrationRequest.getMobile())
                .status(registrationRequest.getStatus().name())
                .message("OTP sent to mobile number. Please verify.")
                .requiresOtp(true)
                .requiresApproval(false)
                .build();
    }

    /**
     * Step 3: Verify OTP and submit for approval
     */
    @Transactional
    public PartnerRegistrationResponseDto verifyOtp(PartnerRegistrationOtpDto request) {
        // Find registration request
        PartnerRegistrationRequest registrationRequest = registrationRequestRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));

        // Validate mobile matches
        if (!request.getMobile().equals(registrationRequest.getMobile())) {
            throw new BadRequestException("Mobile number does not match registration request");
        }

        // Verify OTP
        Otp otp = otpRepository.findByPhoneNumberAndCodeAndVerifiedFalseAndExpiresAtAfter(
                        request.getMobile(), request.getOtp(), LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        // Mark OTP as verified
        otp.setVerified(true);
        otpRepository.save(otp);

        // Update registration request
        registrationRequest.setOtpVerified(true);
        registrationRequest.setStatus(PartnerRegistrationRequest.RequestStatus.PENDING_APPROVAL);
        registrationRequestRepository.save(registrationRequest);

        // Send confirmation email
        emailService.sendRegistrationConfirmation(registrationRequest.getEmail());

        return PartnerRegistrationResponseDto.builder()
                .requestId(registrationRequest.getId())
                .email(registrationRequest.getEmail())
                .mobile(registrationRequest.getMobile())
                .status(registrationRequest.getStatus().name())
                .message("OTP verified successfully. Your registration is pending admin approval.")
                .requiresOtp(false)
                .requiresApproval(true)
                .build();
    }

    /**
     * Admin: Get pending registration requests
     */
    public Page<PartnerRegistrationRequest> getPendingRequests(Pageable pageable) {
        return registrationRequestRepository.findByStatus(
                PartnerRegistrationRequest.RequestStatus.PENDING_APPROVAL,
                pageable
        );
    }

    /**
     * Admin: Get all registration requests
     */
    public Page<PartnerRegistrationRequest> getAllRequests(Pageable pageable) {
        return registrationRequestRepository.findAll(pageable);
    }

    /**
     * Admin: Approve registration request
     */
    @Transactional
    public PartnerRegistrationResponseDto approveRequest(Long requestId, Long adminId, String notes) {
        PartnerRegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));

        if (request.getStatus() != PartnerRegistrationRequest.RequestStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Request is not pending approval");
        }

        // Generate random password
        String generatedPassword = generateRandomPassword();

        // Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setUserType(User.UserType.PARTNER);
        user.setStatus(User.UserStatus.APPROVED);
        user.setMobileVerified(true);
        user.setProfileComplete(false);
        user = userRepository.save(user);

        // Create Partner profile
        Partner partner = new Partner();
        partner.setUser(user);
        partner.setBusinessEmail(request.getEmail());
        partner.setOrganizerMobile(request.getMobile());
        partnerRepository.save(partner);

        // Update registration request
        request.setStatus(PartnerRegistrationRequest.RequestStatus.APPROVED);
        request.setApprovedBy(adminId);
        request.setApprovedAt(LocalDateTime.now());
        request.setAdminNotes(notes);
        registrationRequestRepository.save(request);

        // Send credentials via email
        emailService.sendPartnerCredentials(request.getEmail(), generatedPassword);

        return PartnerRegistrationResponseDto.builder()
                .requestId(request.getId())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .status(request.getStatus().name())
                .message("Registration approved. Credentials sent to email.")
                .requiresOtp(false)
                .requiresApproval(false)
                .build();
    }

    /**
     * Admin: Reject registration request
     */
    @Transactional
    public PartnerRegistrationResponseDto rejectRequest(Long requestId, Long adminId, String reason, String notes) {
        PartnerRegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));

        if (request.getStatus() != PartnerRegistrationRequest.RequestStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Request is not pending approval");
        }

        // Update registration request
        request.setStatus(PartnerRegistrationRequest.RequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setAdminNotes(notes);
        request.setApprovedBy(adminId);
        request.setApprovedAt(LocalDateTime.now());
        registrationRequestRepository.save(request);

        // Send rejection email
        emailService.sendRejectionEmail(request.getEmail(), reason);

        return PartnerRegistrationResponseDto.builder()
                .requestId(request.getId())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .status(request.getStatus().name())
                .message("Registration request rejected.")
                .requiresOtp(false)
                .requiresApproval(false)
                .build();
    }

    private String generateOtpCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}

