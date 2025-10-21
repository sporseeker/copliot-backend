package com.spotseeker.copliot.repository;

import com.spotseeker.copliot.model.Ticket;
import com.spotseeker.copliot.model.TicketPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByQrData(String qrData);

    List<Ticket> findByTicketPackage(TicketPackage ticketPackage);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketPackage.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketPackage.event.id = :eventId AND t.isUsed = true")
    Long countUsedByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketPackage.id = :packageId AND t.isUsed = true")
    Long countUsedByPackageId(@Param("packageId") Long packageId);
}

