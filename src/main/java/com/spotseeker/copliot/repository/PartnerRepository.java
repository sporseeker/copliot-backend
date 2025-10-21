package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.Partner;
import com.spotseeker.copliot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByUser(User user);
    Optional<Partner> findByUserId(Long userId);
}
