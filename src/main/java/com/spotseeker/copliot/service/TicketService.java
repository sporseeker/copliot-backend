package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.TicketValidationRequestDto;
import com.spotseeker.copliot.dto.TicketValidationResponseDto;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.*;
import com.spotseeker.copliot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketPackageRepository ticketPackageRepository;
    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final FraudAlertRepository fraudAlertRepository;

    @Transactional
    public List<TicketResponseDto> generateTickets(Long userId, Long eventId, Long ticketPackageId, Integer quantity) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        TicketPackage ticketPackage = ticketPackageRepository.findByIdAndEvent(ticketPackageId, event)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket package not found"));

        if (ticketPackage.getAvailableCount() < quantity) {
            throw new IllegalStateException("Not enough tickets available");
        }

        List<TicketResponseDto> tickets = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            String qrData = UUID.randomUUID().toString();

            Ticket ticket = new Ticket();
            ticket.setTicketPackage(ticketPackage);
            ticket.setQrData(qrData);
            ticket.setQrImageUrl(generateQrImageUrl(qrData));
            ticket.setIsUsed(false);
            ticket.setPurchaseType(Ticket.PurchaseType.ONLINE);

            ticket = ticketRepository.save(ticket);

            tickets.add(new TicketResponseDto(
                    ticket.getId().toString(),
                    qrData,
                    ticket.getQrImageUrl()
            ));
        }

        // Update sold count
        ticketPackage.setSoldCount(ticketPackage.getSoldCount() + quantity);
        ticketPackageRepository.save(ticketPackage);

        return tickets;
    }

    @Transactional
    public TicketValidationResponseDto validateTicket(Long eventId, TicketValidationRequestDto request) {
        Ticket ticket = ticketRepository.findByQrData(request.getQrData())
                .orElse(null);

        if (ticket == null) {
            // Log fraud alert
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

            logFraudAlert(event, null, FraudAlert.AlertType.FAKE_QR,
                    "Invalid QR code scanned", request.getScannerId());

            return TicketValidationResponseDto.builder()
                    .valid(false)
                    .message("Invalid ticket QR code")
                    .build();
        }

        if (!ticket.getTicketPackage().getEvent().getId().equals(eventId)) {
            return TicketValidationResponseDto.builder()
                    .valid(false)
                    .message("Ticket not valid for this event")
                    .build();
        }

        if (ticket.getIsUsed()) {
            // Log fraud alert for multiple entry attempt
            logFraudAlert(ticket.getTicketPackage().getEvent(), ticket,
                    FraudAlert.AlertType.MULTIPLE_ENTRY,
                    "Ticket already used", request.getScannerId());

            return TicketValidationResponseDto.builder()
                    .valid(false)
                    .ticketId(ticket.getId().toString())
                    .packageName(ticket.getTicketPackage().getName())
                    .attendeeName(ticket.getAttendeeName())
                    .alreadyUsed(true)
                    .usedAt(ticket.getUsedAt().toString())
                    .message("Ticket already used")
                    .build();
        }

        // Mark ticket as used
        ticket.setIsUsed(true);
        ticket.setUsedAt(LocalDateTime.now());
        ticket.setScannerId(request.getScannerId());
        ticketRepository.save(ticket);

        return TicketValidationResponseDto.builder()
                .valid(true)
                .ticketId(ticket.getId().toString())
                .packageName(ticket.getTicketPackage().getName())
                .attendeeName(ticket.getAttendeeName())
                .alreadyUsed(false)
                .message("Ticket validated successfully")
                .build();
    }

    private void logFraudAlert(Event event, Ticket ticket, FraudAlert.AlertType type,
                               String description, String deviceId) {
        FraudAlert alert = new FraudAlert();
        alert.setEvent(event);
        alert.setTicket(ticket);
        alert.setType(type);
        alert.setTitle(formatAlertTitle(type));
        alert.setDescription(description);
        alert.setDeviceId(deviceId);
        alert.setResolved(false);

        fraudAlertRepository.save(alert);
    }

    private String formatAlertTitle(FraudAlert.AlertType type) {
        return switch (type) {
            case MULTIPLE_ENTRY -> "Multiple Entry Attempt";
            case FAKE_QR -> "Fake QR Code Detected";
            case SUSPICIOUS_DEVICE -> "Suspicious Device Activity";
        };
    }

    private String generateQrImageUrl(String qrData) {
        // In production, you would generate actual QR code image
        return "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + qrData;
    }

    public static class TicketResponseDto {
        public String ticketId;
        public String qrData;
        public String qrImageUrl;

        public TicketResponseDto(String ticketId, String qrData, String qrImageUrl) {
            this.ticketId = ticketId;
            this.qrData = qrData;
            this.qrImageUrl = qrImageUrl;
        }
    }
}

