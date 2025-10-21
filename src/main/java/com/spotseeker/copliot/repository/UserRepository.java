package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.model.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobile(String mobile);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
