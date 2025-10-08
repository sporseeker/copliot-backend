package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String type = "Bearer";
    private Object user;

    public AuthResponseDto(String token, Object user) {
        this.token = token;
        this.user = user;
    }
}
