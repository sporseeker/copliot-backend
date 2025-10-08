package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.AuthResponseDto;
import com.spotseeker.copliot.dto.PartnerLoginDto;
import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.dto.PartnerUpdateDto;
import com.spotseeker.copliot.exception.BadRequestException;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.exception.UnauthorizedException;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PartnerService(PartnerRepository partnerRepository, PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.partnerRepository = partnerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Partner register(PartnerRegistrationDto dto, MultipartFile logo) {
        if (partnerRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        Partner partner = new Partner();
        partner.setUsername(dto.getUsername());
        partner.setPassword(passwordEncoder.encode(dto.getPassword()));
        partner.setBusinessName(dto.getBusinessName());
        partner.setContactPerson(dto.getContactPerson());
        partner.setPhoneNumber(dto.getPhoneNumber());
        partner.setEmail(dto.getEmail());
        partner.setAddress(dto.getAddress());
        partner.setIsActive(true);

        if (logo != null && !logo.isEmpty()) {
            String logoPath = saveFile(logo);
            partner.setLogoPath(logoPath);
        }

        return partnerRepository.save(partner);
    }

    public AuthResponseDto login(PartnerLoginDto dto) {
        Partner partner = partnerRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), partner.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!partner.getIsActive()) {
            throw new UnauthorizedException("Account is not active");
        }

        String token = jwtTokenProvider.generateToken(partner.getId().toString(), "partner");

        // Don't send password in response
        partner.setPassword(null);

        return new AuthResponseDto(token, partner);
    }

    public Partner getProfile(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        partner.setPassword(null);
        return partner;
    }

    public Partner updateProfile(Long partnerId, PartnerUpdateDto dto, MultipartFile logo) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (dto.getBusinessName() != null) {
            partner.setBusinessName(dto.getBusinessName());
        }
        if (dto.getContactPerson() != null) {
            partner.setContactPerson(dto.getContactPerson());
        }
        if (dto.getPhoneNumber() != null) {
            partner.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null) {
            partner.setEmail(dto.getEmail());
        }
        if (dto.getAddress() != null) {
            partner.setAddress(dto.getAddress());
        }

        if (logo != null && !logo.isEmpty()) {
            String logoPath = saveFile(logo);
            partner.setLogoPath(logoPath);
        }

        Partner updatedPartner = partnerRepository.save(partner);
        updatedPartner.setPassword(null);
        return updatedPartner;
    }

    private String saveFile(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
