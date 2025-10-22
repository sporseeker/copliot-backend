package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDto {

    @NotNull(message = "Agreement acceptance is required")
    private Boolean agreementAccepted;

    private MultipartFile signatureFile;
}
