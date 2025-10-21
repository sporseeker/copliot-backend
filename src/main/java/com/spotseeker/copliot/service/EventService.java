package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.*;
import com.spotseeker.copliot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final TicketRepository ticketRepository;
    private final S3FileService s3FileService;

    @Transactional
    public EventDetailDto createEvent(Long userId, EventCreateDto dto) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = new Event();
        event.setPartner(partner);
        event.setName(dto.getName());
        event.setDate(LocalDate.parse(dto.getDate()));
        event.setStartTime(LocalTime.parse(dto.getStartTime()));
        event.setEndTime(LocalTime.parse(dto.getEndTime()));
        event.setVenue(dto.getVenue());
        event.setVenueType(Event.VenueType.valueOf(dto.getVenueType().toUpperCase()));
        event.setEventType(Event.EventType.valueOf(dto.getEventType().toUpperCase()));
        event.setEventCategory(Event.EventCategory.valueOf(dto.getEventCategory().toUpperCase()));
        event.setDescription(dto.getDescription());
        event.setTrailerVideoUrl(dto.getTrailerVideoUrl());
        event.setGoogleMapUrl(dto.getGoogleMapUrl());
        event.setInstagramUrl(dto.getInstagramUrl());
        event.setFacebookUrl(dto.getFacebookUrl());
        event.setKokoPaymentEnabled(dto.getKokoPaymentEnabled());
        event.setStatus(Event.EventStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getTicketCounterStartTime() != null) {
            event.setTicketCounterStartTime(LocalTime.parse(dto.getTicketCounterStartTime()));
        }

        // Upload flyer images
        List<String> flyerUrls = new ArrayList<>();
        if (dto.getFlyerImages() != null) {
            for (MultipartFile file : dto.getFlyerImages()) {
                FileUploadResponseDto upload = s3FileService.uploadFile(
                        file,
                        FileUpload.FileType.IMAGE,
                        FileUpload.FilePurpose.EVENT_FLYER,
                        userId
                );
                flyerUrls.add(upload.getUrl());
            }
        }
        event.setFlyerImages(flyerUrls);

        event = eventRepository.save(event);
        return mapToDetailDto(event);
    }

    public EventListResponseDto getEvents(Long userId, String status, Integer page, Integer limit) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Pageable pageable = PageRequest.of(page != null ? page : 0, limit != null ? limit : 10);
        Page<Event> eventPage;

        if (status != null && !status.equals("all")) {
            Event.EventStatus eventStatus = Event.EventStatus.valueOf(status.toUpperCase());
            eventPage = eventRepository.findByPartnerAndStatus(partner, eventStatus, pageable);
        } else {
            eventPage = eventRepository.findByPartner(partner, pageable);
        }

        List<EventListResponseDto.EventSummaryDto> events = eventPage.getContent().stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return EventListResponseDto.builder()
                .events(events)
                .total(eventPage.getTotalElements())
                .page(page != null ? page : 0)
                .limit(limit != null ? limit : 10)
                .build();
    }

    public EventDetailDto getEventById(Long userId, Long eventId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return mapToDetailDto(event);
    }

    @Transactional
    public EventDetailDto updateEvent(Long userId, Long eventId, EventUpdateDto dto) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (dto.getName() != null) event.setName(dto.getName());
        if (dto.getDate() != null) event.setDate(LocalDate.parse(dto.getDate()));
        if (dto.getStartTime() != null) event.setStartTime(LocalTime.parse(dto.getStartTime()));
        if (dto.getEndTime() != null) event.setEndTime(LocalTime.parse(dto.getEndTime()));
        if (dto.getVenue() != null) event.setVenue(dto.getVenue());
        if (dto.getVenueType() != null) event.setVenueType(Event.VenueType.valueOf(dto.getVenueType().toUpperCase()));
        if (dto.getEventType() != null) event.setEventType(Event.EventType.valueOf(dto.getEventType().toUpperCase()));
        if (dto.getEventCategory() != null) event.setEventCategory(Event.EventCategory.valueOf(dto.getEventCategory().toUpperCase()));
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getTrailerVideoUrl() != null) event.setTrailerVideoUrl(dto.getTrailerVideoUrl());
        if (dto.getGoogleMapUrl() != null) event.setGoogleMapUrl(dto.getGoogleMapUrl());
        if (dto.getInstagramUrl() != null) event.setInstagramUrl(dto.getInstagramUrl());
        if (dto.getFacebookUrl() != null) event.setFacebookUrl(dto.getFacebookUrl());
        if (dto.getKokoPaymentEnabled() != null) event.setKokoPaymentEnabled(dto.getKokoPaymentEnabled());
        if (dto.getStatus() != null) event.setStatus(Event.EventStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getFlyerImages() != null && !dto.getFlyerImages().isEmpty()) {
            List<String> flyerUrls = new ArrayList<>();
            for (MultipartFile file : dto.getFlyerImages()) {
                FileUploadResponseDto upload = s3FileService.uploadFile(
                        file,
                        FileUpload.FileType.IMAGE,
                        FileUpload.FilePurpose.EVENT_FLYER,
                        userId
                );
                flyerUrls.add(upload.getUrl());
            }
            event.setFlyerImages(flyerUrls);
        }

        event = eventRepository.save(event);
        return mapToDetailDto(event);
    }

    @Transactional
    public void deleteEvent(Long userId, Long eventId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventRepository.delete(event);
    }

    private EventListResponseDto.EventSummaryDto mapToSummaryDto(Event event) {
        Long totalAttendees = ticketRepository.countByEventId(event.getId());

        String statusType = switch (event.getStatus()) {
            case APPROVED, ACTIVE -> "success";
            case PENDING -> "warning";
            case CANCELLED -> "error";
            default -> "info";
        };

        String category = switch (event.getStatus()) {
            case ACTIVE, APPROVED -> "active";
            case PENDING, DRAFT -> "pending";
            default -> "inactive";
        };

        return EventListResponseDto.EventSummaryDto.builder()
                .id(event.getId().toString())
                .name(event.getName())
                .date(event.getDate().toString())
                .startTime(event.getStartTime().toString())
                .endTime(event.getEndTime().toString())
                .venue(event.getVenue())
                .imageUrl(event.getFlyerImages().isEmpty() ? null : event.getFlyerImages().get(0))
                .status(event.getStatus().name().toLowerCase())
                .statusType(statusType)
                .category(category)
                .totalAttendees(totalAttendees.intValue())
                .totalRevenue(0.0) // TODO: Calculate from tickets
                .build();
    }

    private EventDetailDto mapToDetailDto(Event event) {
        return EventDetailDto.builder()
                .id(event.getId().toString())
                .name(event.getName())
                .date(event.getDate().toString())
                .startTime(event.getStartTime().toString())
                .endTime(event.getEndTime().toString())
                .venue(event.getVenue())
                .venueType(event.getVenueType().name().toLowerCase())
                .eventType(event.getEventType().name().toLowerCase())
                .eventCategory(event.getEventCategory().name().toLowerCase())
                .description(event.getDescription())
                .trailerVideoUrl(event.getTrailerVideoUrl())
                .googleMapUrl(event.getGoogleMapUrl())
                .instagramUrl(event.getInstagramUrl())
                .facebookUrl(event.getFacebookUrl())
                .flyerImages(event.getFlyerImages())
                .ticketCounterStartTime(event.getTicketCounterStartTime() != null ? event.getTicketCounterStartTime().toString() : null)
                .kokoPaymentEnabled(event.getKokoPaymentEnabled())
                .status(event.getStatus().name().toLowerCase())
                .createdBy(event.getPartner().getOrganizationName())
                .createdAt(event.getCreatedAt().toString())
                .updatedAt(event.getUpdatedAt().toString())
                .build();
    }
}
