package com.spotseeker.copliot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "resolved")
    private Boolean resolved = false;

    public enum AlertType {
        MULTIPLE_ENTRY, FAKE_QR, SUSPICIOUS_DEVICE
    }

    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
    }
}

