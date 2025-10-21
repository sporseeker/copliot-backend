package com.spotseeker.copliot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        partnerRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        MockMultipartFile logoFile = new MockMultipartFile(
            "logo",
            "test-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/partners/register")
                .file(logoFile)
                .param("username", "testpartner")
                .param("password", "password123")
                .param("businessName", "Test Business")
                .param("contactPerson", "John Doe")
                .param("phoneNumber", "+1234567890")
                .param("email", "test@example.com")
                .param("address", "123 Main St"))
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
        existing.setContactPerson("John Doe");
        existing.setPhoneNumber("+1234567890");
        existing.setEmail("existing@example.com");
        existing.setAddress("123 Main St");
        partnerRepository.save(existing);

        MockMultipartFile logoFile = new MockMultipartFile(
            "logo",
            "test-logo.png",
            MediaType.IMAGE_PNG_VALUE,
            "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/partners/register")
                .file(logoFile)
                .param("username", "testpartner")
                .param("password", "password456")
                .param("businessName", "Test Business")
                .param("contactPerson", "Jane Doe")
                .param("phoneNumber", "+1987654320")
                .param("email", "test@example.com")
                .param("address", "456 Side St"))
                .andExpect(status().isBadRequest());
    }
}
