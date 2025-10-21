package com.spotseeker.copliot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_package_id", nullable = false)
    private TicketPackage ticketPackage;

    @Column(nullable = false, unique = true, name = "qr_data")
    private String qrData;

    @Column(name = "qr_image_url")
    private String qrImageUrl;

    @Column(name = "attendee_name")
    private String attendeeName;

    @Column(name = "attendee_email")
    private String attendeeEmail;

    @Column(name = "attendee_mobile")
    private String attendeeMobile;

    @Column(name = "is_used")
    private Boolean isUsed = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "scanner_id")
    private String scannerId;

    @Column(name = "purchase_type")
    @Enumerated(EnumType.STRING)
    private PurchaseType purchaseType = PurchaseType.ONLINE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PurchaseType {
        ONLINE, SPOTSEEKER_INVITE, SPECIAL_INVITE
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

