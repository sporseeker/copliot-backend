package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.Event;
import com.spotseeker.copliot.model.Event.EventStatus;
import com.spotseeker.copliot.model.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByPartnerAndStatus(Partner partner, EventStatus status, Pageable pageable);

    Page<Event> findByPartner(Partner partner, Pageable pageable);

    List<Event> findByPartner(Partner partner);

    Optional<Event> findByIdAndPartner(Long id, Partner partner);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);
}

