package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.dto.PartnerUpdateDto;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.service.PartnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Partner> register(
            @Valid @ModelAttribute PartnerRegistrationDto dto,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        Partner partner = partnerService.register(dto, logo);
        return ResponseEntity.status(HttpStatus.CREATED).body(partner);
    }

    @GetMapping("/profile")
    public ResponseEntity<Partner> getProfile(Authentication authentication) {
        Long partnerId = Long.parseLong(authentication.getName());
        Partner partner = partnerService.getProfile(partnerId);
        return ResponseEntity.ok(partner);
    }

    @PutMapping("/profile")
    public ResponseEntity<Partner> updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute PartnerUpdateDto dto,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        Long partnerId = Long.parseLong(authentication.getName());
        Partner partner = partnerService.updateProfile(partnerId, dto, logo);
        return ResponseEntity.ok(partner);
    }
}
