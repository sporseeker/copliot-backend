package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerApprovalDto {
    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotBlank(message = "Action is required (approve/reject)")
    private String action;

    private String notes;

    private String rejectionReason;
}

