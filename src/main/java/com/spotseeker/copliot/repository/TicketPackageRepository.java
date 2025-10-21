package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.Event;
import com.spotseeker.copliot.model.TicketPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketPackageRepository extends JpaRepository<TicketPackage, Long> {

    List<TicketPackage> findByEvent(Event event);

    Optional<TicketPackage> findByIdAndEvent(Long id, Event event);
}

