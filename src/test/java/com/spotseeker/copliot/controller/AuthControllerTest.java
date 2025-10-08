package com.spotseeker.copliot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotseeker.copliot.dto.OtpRequestDto;
import com.spotseeker.copliot.dto.OtpVerifyDto;
import com.spotseeker.copliot.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRequestOtp_Success() throws Exception {
        OtpRequestDto request = new OtpRequestDto("+1234567890");

        mockMvc.perform(post("/api/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));
    }

    @Test
    void testRequestOtp_InvalidPhoneNumber() throws Exception {
        OtpRequestDto request = new OtpRequestDto("invalid");

        mockMvc.perform(post("/api/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyOtp_InvalidOtp() throws Exception {
        OtpVerifyDto request = new OtpVerifyDto("+1234567890", "999999");

        mockMvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testResendOtp_Success() throws Exception {
        OtpRequestDto request = new OtpRequestDto("+1234567890");

        mockMvc.perform(post("/api/auth/otp/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP resent successfully"));
    }
}
