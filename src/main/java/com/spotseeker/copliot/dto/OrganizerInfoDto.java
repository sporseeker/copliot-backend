package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerInfoDto {

    @NotBlank(message = "Organizer name is required")
    private String organizerName;

    @NotBlank(message = "Organizer mobile is required")
    private String organizerMobile;

    @NotBlank(message = "Organizer address is required")
    private String organizerAddress;

    @NotBlank(message = "Organizer NIC is required")
    private String organizerNic;

    @NotBlank(message = "ID type is required")
    private String idType; // nic, driving_license, passport

    private MultipartFile idFrontFile;

    private MultipartFile idBackFile;
}

