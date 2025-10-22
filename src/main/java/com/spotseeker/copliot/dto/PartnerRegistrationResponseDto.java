package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRegistrationResponseDto {
    private Long requestId;
    private String email;
    private String mobile;
    private String status;
    private String message;
    private Boolean requiresOtp;
    private Boolean requiresApproval;
}

