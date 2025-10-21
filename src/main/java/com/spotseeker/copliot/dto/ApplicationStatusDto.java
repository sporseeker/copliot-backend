package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusDto {
    private String status; // pending, approved, rejected
    private String currentStep; // company_profile, organizer_info, agreement
    private String lastUpdated;
}
