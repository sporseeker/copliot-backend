package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByPhoneNumberAndCodeAndVerifiedFalseAndExpiresAtAfter(
            String phoneNumber, String code, LocalDateTime now);
    void deleteByPhoneNumberAndVerifiedFalse(String phoneNumber);
}
