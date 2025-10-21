package com.spotseeker.copliot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.repository.PartnerRepository;
import com.spotseeker.copliot.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        partnerRepository.deleteAll();
        userRepository.deleteAll();
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
                .andExpect(jsonPath("$.organizationName").value("Test Business"));
    }

    @Test
    void testRegister_DuplicateEmail() throws Exception {
        // Create existing user and partner
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setMobile("+1234567890");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setUserType(User.UserType.PARTNER);
        existingUser.setStatus(User.UserStatus.PENDING);
        existingUser = userRepository.save(existingUser);

        Partner existing = new Partner();
        existing.setUser(existingUser);
        existing.setOrganizationName("Existing Business");
        existing.setOrganizerName("John Doe");
        existing.setOrganizerMobile("+1234567890");
        existing.setBusinessEmail("existing@example.com");
        existing.setRegisteredAddress("123 Main St");
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
                .param("email", "existing@example.com")
                .param("address", "456 Side St"))
                .andExpect(status().isBadRequest());
    }
}
