package com.spotseeker.copliot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegister_Success() throws Exception {
        mockMvc.perform(multipart("/api/partners/register")
                        .param("username", "testpartner")
                        .param("password", "password123")
                        .param("businessName", "Test Business")
                        .param("email", "test@example.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testpartner"))
                .andExpect(jsonPath("$.businessName").value("Test Business"));
    }

    @Test
    void testRegister_DuplicateUsername() throws Exception {
        // Create existing partner
        Partner existing = new Partner();
        existing.setUsername("testpartner");
        existing.setPassword(passwordEncoder.encode("password123"));
        existing.setBusinessName("Existing Business");
        partnerRepository.save(existing);

        mockMvc.perform(multipart("/api/partners/register")
                        .param("username", "testpartner")
                        .param("password", "password123")
                        .param("businessName", "Test Business"))
                .andExpect(status().isBadRequest());
    }
}
