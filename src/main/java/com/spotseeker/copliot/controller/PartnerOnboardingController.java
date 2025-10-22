package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.service.PartnerOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/partner")
@RequiredArgsConstructor
public class PartnerOnboardingController {

    private final PartnerOnboardingService partnerOnboardingService;

    @PostMapping("/company-profile")
    public ResponseEntity<Map<String, String>> saveCompanyProfile(
            Authentication authentication,
            @Valid @ModelAttribute CompanyProfileDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        partnerOnboardingService.saveCompanyProfile(userId, dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Company profile saved successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/organizer-info")
    public ResponseEntity<Map<String, String>> saveOrganizerInfo(
            Authentication authentication,
            @Valid @ModelAttribute OrganizerInfoDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        partnerOnboardingService.saveOrganizerInfo(userId, dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Organizer information saved successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/agreement")
    public ResponseEntity<Map<String, String>> saveAgreement(
            Authentication authentication,
            @Valid @ModelAttribute AgreementDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        partnerOnboardingService.saveAgreement(userId, dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Agreement accepted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/application-status")
    public ResponseEntity<ApplicationStatusDto> getApplicationStatus(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ApplicationStatusDto status = partnerOnboardingService.getApplicationStatus(userId);
        return ResponseEntity.ok(status);
    }
}
