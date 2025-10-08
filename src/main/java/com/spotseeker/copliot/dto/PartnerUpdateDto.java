package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerUpdateDto {
    private String businessName;
    private String contactPerson;
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String address;
}
