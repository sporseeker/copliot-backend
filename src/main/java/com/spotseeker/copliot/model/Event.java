package com.spotseeker.copliot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String venue;

    @Column(name = "venue_type")
    @Enumerated(EnumType.STRING)
    private VenueType venueType;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "event_category")
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "trailer_video_url")
    private String trailerVideoUrl;

    @Column(name = "google_map_url")
    private String googleMapUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @ElementCollection
    @CollectionTable(name = "event_flyer_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    private List<String> flyerImages = new ArrayList<>();

    @Column(name = "ticket_counter_start_time")
    private LocalTime ticketCounterStartTime;

    @Column(name = "koko_payment_enabled")
    private Boolean kokoPaymentEnabled = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum VenueType {
        OUTDOOR, INDOOR, VIRTUAL
    }

    public enum EventType {
        CONCERT, CONFERENCE, WORKSHOP, FESTIVAL, MEETUP
    }

    public enum EventCategory {
        EDM, FAMILY, TAMIL_DJ, HALOWEEN, AIR_EXPERIENCE
    }

    public enum EventStatus {
        DRAFT, PENDING, APPROVED, ACTIVE, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

