package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMobileDto {

    @NotBlank(message = "Mobile is required")
    private String mobile;

    @NotBlank(message = "OTP is required")
    private String otp;
}

