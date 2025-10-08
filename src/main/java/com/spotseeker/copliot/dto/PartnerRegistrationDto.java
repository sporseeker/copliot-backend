package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRegistrationDto {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String contactPerson;

    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private String address;
}
