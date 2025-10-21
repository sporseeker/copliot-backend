package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDto {

    @NotNull(message = "Agreement acceptance is required")
    private Boolean agreementAccepted;

    private String signatureData; // base64 string
}

