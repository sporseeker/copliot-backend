package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileDto {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Business email is required")
    private String businessEmail;

    @NotBlank(message = "Registered address is required")
    private String registeredAddress;

    private Boolean hasBusinessRegistration;

    private MultipartFile businessRegistrationFile;

    private String instagramUrl;

    private String facebookUrl;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @NotBlank(message = "Branch is required")
    private String branch;
}

