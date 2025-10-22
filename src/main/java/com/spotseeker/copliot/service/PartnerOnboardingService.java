package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.FileUpload;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PartnerOnboardingService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final S3FileService s3FileService;

    @Transactional
    public void saveCompanyProfile(Long userId, CompanyProfileDto dto) {
        Partner partner = getPartnerByUserId(userId);

        partner.setOrganizationName(dto.getOrganizationName());
        partner.setBusinessEmail(dto.getBusinessEmail());
        partner.setRegisteredAddress(dto.getRegisteredAddress());
        partner.setHasBusinessRegistration(dto.getHasBusinessRegistration());
        partner.setInstagramUrl(dto.getInstagramUrl());
        partner.setFacebookUrl(dto.getFacebookUrl());
        partner.setBankName(dto.getBankName());
        partner.setAccountNumber(dto.getAccountNumber());
        partner.setAccountHolderName(dto.getAccountHolderName());
        partner.setBranch(dto.getBranch());

        // Upload business registration file if provided
        if (dto.getBusinessRegistrationFile() != null && !dto.getBusinessRegistrationFile().isEmpty()) {
            FileUploadResponseDto uploadResponse = s3FileService.uploadFile(
                    dto.getBusinessRegistrationFile(),
                    FileUpload.FileType.DOCUMENT,
                    FileUpload.FilePurpose.COMPANY_REGISTRATION,
                    userId
            );
            partner.setBusinessRegistrationFile(uploadResponse.getUrl());
        }

        partner.setOnboardingStep(Partner.OnboardingStep.ORGANIZER_INFO);
        partnerRepository.save(partner);
    }

    @Transactional
    public void saveOrganizerInfo(Long userId, OrganizerInfoDto dto) {
        Partner partner = getPartnerByUserId(userId);

        partner.setOrganizerName(dto.getOrganizerName());
        partner.setOrganizerMobile(dto.getOrganizerMobile());
        partner.setOrganizerAddress(dto.getOrganizerAddress());
        partner.setOrganizerNic(dto.getOrganizerNic());
        partner.setIdType(Partner.IdType.valueOf(dto.getIdType().toUpperCase()));

        // Upload ID front file
        if (dto.getIdFrontFile() != null && !dto.getIdFrontFile().isEmpty()) {
            FileUploadResponseDto uploadResponse = s3FileService.uploadFile(
                    dto.getIdFrontFile(),
                    FileUpload.FileType.IMAGE,
                    FileUpload.FilePurpose.ORGANIZER_ID,
                    userId
            );
            partner.setIdFrontFile(uploadResponse.getUrl());
        }

        // Upload ID back file
        if (dto.getIdBackFile() != null && !dto.getIdBackFile().isEmpty()) {
            FileUploadResponseDto uploadResponse = s3FileService.uploadFile(
                    dto.getIdBackFile(),
                    FileUpload.FileType.IMAGE,
                    FileUpload.FilePurpose.ORGANIZER_ID,
                    userId
            );
            partner.setIdBackFile(uploadResponse.getUrl());
        }

        partner.setOnboardingStep(Partner.OnboardingStep.AGREEMENT);
        partnerRepository.save(partner);
    }

    @Transactional
    public void saveAgreement(Long userId, AgreementDto dto) {
        Partner partner = getPartnerByUserId(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        partner.setAgreementAccepted(dto.getAgreementAccepted());
        partner.setSignedAt(LocalDateTime.now());

        // Upload signature file to S3 if provided
        if (dto.getSignatureFile() != null && !dto.getSignatureFile().isEmpty()) {
            FileUploadResponseDto uploadResponse = s3FileService.uploadFile(
                    dto.getSignatureFile(),
                    FileUpload.FileType.IMAGE,
                    FileUpload.FilePurpose.AGREEMENT_SIGNATURE,
                    userId
            );
            partner.setSignatureFile(uploadResponse.getUrl());
        }

        partner.setOnboardingStep(Partner.OnboardingStep.COMPLETE);

        // Update user profile status
        user.setProfileComplete(true);
        // Note: User status remains PENDING until admin approval

        partnerRepository.save(partner);
        userRepository.save(user);
    }

    public ApplicationStatusDto getApplicationStatus(Long userId) {
        Partner partner = getPartnerByUserId(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ApplicationStatusDto.builder()
                .status(user.getStatus().name().toLowerCase())
                .currentStep(partner.getOnboardingStep().name().toLowerCase())
                .lastUpdated(partner.getUpdatedAt().toString())
                .build();
    }

    private Partner getPartnerByUserId(Long userId) {
        return partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));
    }
}
