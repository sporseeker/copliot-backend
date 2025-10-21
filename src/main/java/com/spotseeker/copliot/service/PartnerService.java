package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.AuthResponseDto;
import com.spotseeker.copliot.dto.PartnerLoginDto;
import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.dto.PartnerUpdateDto;
import com.spotseeker.copliot.exception.BadRequestException;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.exception.UnauthorizedException;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PartnerService(PartnerRepository partnerRepository, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.partnerRepository = partnerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Partner register(PartnerRegistrationDto dto, MultipartFile logo) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Create user account
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserType(User.UserType.PARTNER);
        user.setStatus(User.UserStatus.PENDING);
        user.setProfileComplete(false);
        user = userRepository.save(user);

        // Create partner profile
        Partner partner = new Partner();
        partner.setUser(user);
        partner.setOrganizationName(dto.getBusinessName());
        partner.setOrganizerName(dto.getContactPerson());
        partner.setOrganizerMobile(dto.getPhoneNumber());
        partner.setBusinessEmail(dto.getEmail());
        partner.setRegisteredAddress(dto.getAddress());

        if (logo != null && !logo.isEmpty()) {
            String logoPath = saveFile(logo);
            partner.setBusinessRegistrationFile(logoPath);
        }

        return partnerRepository.save(partner);
    }

    public AuthResponseDto login(PartnerLoginDto dto) {
        // Find user by email (username is now email)
        User user = userRepository.findByEmail(dto.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getUserType().name());

        // Get partner profile
        Partner partner = partnerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found"));

        return new AuthResponseDto(token, user);
    }

    public Partner getProfile(Long userId) {
        return partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
    }

    public Partner updateProfile(Long userId, PartnerUpdateDto dto, MultipartFile logo) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        if (dto.getBusinessName() != null) {
            partner.setOrganizationName(dto.getBusinessName());
        }
        if (dto.getContactPerson() != null) {
            partner.setOrganizerName(dto.getContactPerson());
        }
        if (dto.getPhoneNumber() != null) {
            partner.setOrganizerMobile(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null) {
            partner.setBusinessEmail(dto.getEmail());
        }
        if (dto.getAddress() != null) {
            partner.setRegisteredAddress(dto.getAddress());
        }

        if (logo != null && !logo.isEmpty()) {
            String logoPath = saveFile(logo);
            partner.setBusinessRegistrationFile(logoPath);
        }

        return partnerRepository.save(partner);
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
