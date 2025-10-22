package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.PartnerRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRegistrationRequestRepository extends JpaRepository<PartnerRegistrationRequest, Long> {
    Optional<PartnerRegistrationRequest> findByEmail(String email);
    Optional<PartnerRegistrationRequest> findByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    Page<PartnerRegistrationRequest> findByStatus(PartnerRegistrationRequest.RequestStatus status, Pageable pageable);
}

