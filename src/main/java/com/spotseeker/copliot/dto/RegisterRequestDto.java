package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number")
    private String mobile;

    @NotBlank(message = "Password is required")
    private String password;

    private String userType = "partner";
}

