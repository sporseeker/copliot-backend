package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.PartnerLoginDto;
import com.spotseeker.copliot.dto.PartnerRegistrationDto;
import com.spotseeker.copliot.dto.AuthResponseDto;
import com.spotseeker.copliot.exception.BadRequestException;
import com.spotseeker.copliot.exception.UnauthorizedException;
import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PartnerServiceTest {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private PartnerRepository partnerRepository;

    @BeforeEach
    void setUp() {
        partnerRepository.deleteAll();
    }

    @Test
    void testRegister_Success() {
        PartnerRegistrationDto dto = new PartnerRegistrationDto(
                "testpartner", "password123", "Test Business",
                "John Doe", "+1234567890", "test@example.com", "123 Main St"
        );

        Partner partner = partnerService.register(dto, null);

        assertNotNull(partner.getId());
        assertEquals("testpartner", partner.getUsername());
        assertEquals("Test Business", partner.getBusinessName());
    }

    @Test
    void testRegister_DuplicateUsername() {
        // First registration
        PartnerRegistrationDto dto1 = new PartnerRegistrationDto(
                "testpartner", "password123", "Test Business",
                "John Doe", "+1234567890", "test@example.com", "123 Main St"
        );
        partnerService.register(dto1, null);

        // Second registration with same username
        PartnerRegistrationDto dto2 = new PartnerRegistrationDto(
                "testpartner", "password456", "Another Business",
                "Jane Doe", "+1987654320", "another@example.com", "456 Side St"
        );

        assertThrows(BadRequestException.class, () -> partnerService.register(dto2, null));
    }

    @Test
    void testLogin_Success() {
        PartnerRegistrationDto regDto = new PartnerRegistrationDto(
                "logintest", "password123", "Test Business",
                null, null, null, null
        );
        partnerService.register(regDto, null);

        PartnerLoginDto loginDto = new PartnerLoginDto("logintest", "password123");
        AuthResponseDto response = partnerService.login(loginDto);

        assertNotNull(response.getToken());
        assertNotNull(response.getUser());
    }

    @Test
    void testLogin_InvalidPassword() {
        PartnerRegistrationDto regDto = new PartnerRegistrationDto(
                "logintest", "password123", "Test Business",
                null, null, null, null
        );
        partnerService.register(regDto, null);

        PartnerLoginDto loginDto = new PartnerLoginDto("logintest", "wrongpassword");

        assertThrows(UnauthorizedException.class, () -> partnerService.login(loginDto));
    }
}
