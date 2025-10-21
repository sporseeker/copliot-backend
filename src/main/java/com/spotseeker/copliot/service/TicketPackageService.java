package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.*;
import com.spotseeker.copliot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketPackageService {

    private final TicketPackageRepository ticketPackageRepository;
    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;

    @Transactional
    public TicketPackageListDto.TicketPackageDto createTicketPackage(Long userId, Long eventId, TicketPackageCreateDto dto) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        TicketPackage ticketPackage = new TicketPackage();
        ticketPackage.setEvent(event);
        ticketPackage.setName(dto.getName());
        ticketPackage.setPrice(dto.getPrice());
        ticketPackage.setReleaseCount(dto.getReleaseCount());
        ticketPackage.setStartDateTime(LocalDateTime.parse(dto.getStartDateTime()));
        ticketPackage.setEndDateTime(LocalDateTime.parse(dto.getEndDateTime()));
        ticketPackage.setStatus(TicketPackage.TicketStatus.valueOf(dto.getStatus().toUpperCase()));
        ticketPackage.setSoldCount(0);

        ticketPackage = ticketPackageRepository.save(ticketPackage);
        return mapToDto(ticketPackage);
    }

    public TicketPackageListDto getTicketPackages(Long userId, Long eventId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<TicketPackage> packages = ticketPackageRepository.findByEvent(event);

        List<TicketPackageListDto.TicketPackageDto> packageDtos = packages.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return TicketPackageListDto.builder()
                .ticketPackages(packageDtos)
                .build();
    }

    @Transactional
    public TicketPackageListDto.TicketPackageDto updateTicketPackage(Long userId, Long eventId,
                                                                     Long packageId, TicketPackageUpdateDto dto) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        TicketPackage ticketPackage = ticketPackageRepository.findByIdAndEvent(packageId, event)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket package not found"));

        if (dto.getName() != null) ticketPackage.setName(dto.getName());
        if (dto.getPrice() != null) ticketPackage.setPrice(dto.getPrice());
        if (dto.getReleaseCount() != null) ticketPackage.setReleaseCount(dto.getReleaseCount());
        if (dto.getStartDateTime() != null) ticketPackage.setStartDateTime(LocalDateTime.parse(dto.getStartDateTime()));
        if (dto.getEndDateTime() != null) ticketPackage.setEndDateTime(LocalDateTime.parse(dto.getEndDateTime()));
        if (dto.getStatus() != null) ticketPackage.setStatus(TicketPackage.TicketStatus.valueOf(dto.getStatus().toUpperCase()));

        ticketPackage = ticketPackageRepository.save(ticketPackage);
        return mapToDto(ticketPackage);
    }

    @Transactional
    public void deleteTicketPackage(Long userId, Long eventId, Long packageId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        TicketPackage ticketPackage = ticketPackageRepository.findByIdAndEvent(packageId, event)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket package not found"));

        ticketPackageRepository.delete(ticketPackage);
    }

    private TicketPackageListDto.TicketPackageDto mapToDto(TicketPackage ticketPackage) {
        return TicketPackageListDto.TicketPackageDto.builder()
                .id(ticketPackage.getId().toString())
                .name(ticketPackage.getName())
                .price(ticketPackage.getPrice())
                .releaseCount(ticketPackage.getReleaseCount())
                .soldCount(ticketPackage.getSoldCount())
                .availableCount(ticketPackage.getAvailableCount())
                .startDateTime(ticketPackage.getStartDateTime().toString())
                .endDateTime(ticketPackage.getEndDateTime().toString())
                .status(ticketPackage.getStatus().name().toLowerCase())
                .build();
    }
}

