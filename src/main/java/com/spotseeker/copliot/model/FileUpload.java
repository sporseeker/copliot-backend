package com.spotseeker.copliot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true)
    private String fileId;

    @Column(nullable = false)
    private String url;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(name = "purpose")
    @Enumerated(EnumType.STRING)
    private FilePurpose purpose;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum FileType {
        IMAGE, DOCUMENT, SIGNATURE
    }

    public enum FilePurpose {
        EVENT_FLYER, COMPANY_REGISTRATION, ORGANIZER_ID, AGREEMENT_SIGNATURE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
