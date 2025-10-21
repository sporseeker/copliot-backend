package com.spotseeker.copliot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Company Profile
    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "business_email")
    private String businessEmail;

    @Column(name = "registered_address", columnDefinition = "TEXT")
    private String registeredAddress;

    @Column(name = "has_business_registration")
    private Boolean hasBusinessRegistration = false;

    @Column(name = "business_registration_file")
    private String businessRegistrationFile;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    // Bank Details
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "branch")
    private String branch;

    // Organizer Information
    @Column(name = "organizer_name")
    private String organizerName;

    @Column(name = "organizer_mobile")
    private String organizerMobile;

    @Column(name = "organizer_address", columnDefinition = "TEXT")
    private String organizerAddress;

    @Column(name = "organizer_nic")
    private String organizerNic;

    @Column(name = "id_type")
    @Enumerated(EnumType.STRING)
    private IdType idType;

    @Column(name = "id_front_file")
    private String idFrontFile;

    @Column(name = "id_back_file")
    private String idBackFile;

    // Partnership Agreement
    @Column(name = "agreement_accepted")
    private Boolean agreementAccepted = false;

    @Column(name = "signature_file")
    private String signatureFile;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    // Onboarding Progress
    @Column(name = "onboarding_step")
    @Enumerated(EnumType.STRING)
    private OnboardingStep onboardingStep = OnboardingStep.COMPANY_PROFILE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum IdType {
        NIC, DRIVING_LICENSE, PASSPORT
    }

    public enum OnboardingStep {
        COMPANY_PROFILE, ORGANIZER_INFO, AGREEMENT, COMPLETE
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
