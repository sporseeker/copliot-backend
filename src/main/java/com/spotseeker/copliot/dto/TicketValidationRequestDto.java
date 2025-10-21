package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketValidationRequestDto {

    @NotBlank(message = "QR data is required")
    private String qrData;

    @NotBlank(message = "Scanner ID is required")
    private String scannerId;
}

