package com.spotseeker.copliot.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.spotseeker.copliot.dto.FileUploadResponseDto;
import com.spotseeker.copliot.exception.FileUploadException;
import com.spotseeker.copliot.model.FileUpload;
import com.spotseeker.copliot.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileService {

    private final AmazonS3 amazonS3;
    private final FileUploadRepository fileUploadRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.enabled:true}")
    private boolean s3Enabled;

    public FileUploadResponseDto uploadFile(MultipartFile file, FileUpload.FileType fileType,
                                           FileUpload.FilePurpose purpose, Long userId) {
        try {
            String fileId = UUID.randomUUID().toString();
            String fileName = generateFilePathWithStructure(file.getOriginalFilename(), fileId, purpose, userId);

            if (s3Enabled) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());

                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        bucketName,
                        fileName,
                        file.getInputStream(),
                        metadata
                );

                amazonS3.putObject(putObjectRequest);
            }

            String fileUrl = s3Enabled ? amazonS3.getUrl(bucketName, fileName).toString() :
                            "/uploads/" + fileName;

            FileUpload fileUpload = new FileUpload();
            fileUpload.setFileId(fileId);
            fileUpload.setUrl(fileUrl);
            fileUpload.setFileName(file.getOriginalFilename());
            fileUpload.setFileSize(file.getSize());
            fileUpload.setFileType(fileType);
            fileUpload.setPurpose(purpose);
            fileUpload.setUploadedBy(userId);

            fileUploadRepository.save(fileUpload);

            return FileUploadResponseDto.builder()
                    .fileId(fileId)
                    .url(fileUrl)
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now().toString())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new FileUploadException("Failed to upload file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileId) {
        FileUpload fileUpload = fileUploadRepository.findByFileId(fileId)
                .orElseThrow(() -> new FileUploadException("File not found"));

        try {
            if (s3Enabled) {
                String fileName = extractFilePathFromUrl(fileUpload.getUrl());
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
                amazonS3.deleteObject(deleteObjectRequest);
            }

            fileUploadRepository.delete(fileUpload);
        } catch (Exception e) {
            log.error("Error deleting file from S3", e);
            throw new FileUploadException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Generates file path with proper folder structure:
     * Format: {purpose}/{year}/{month}/{userId}/{fileId}.{ext}
     * Example: partners/company-registration/2025/10/123/uuid.pdf
     */
    private String generateFilePathWithStructure(String originalFileName, String fileId,
                                                 FileUpload.FilePurpose purpose, Long userId) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Get current date for folder structure
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));

        // Build folder path based on purpose
        String folderPath = getFolderPathByPurpose(purpose);

        // Complete path: folder/year/month/userId/fileId.ext
        return String.format("%s/%s/%s/%s/%s%s", folderPath, year, month, userId, fileId, extension);
    }

    /**
     * Returns the base folder path for each file purpose
     */
    private String getFolderPathByPurpose(FileUpload.FilePurpose purpose) {
        return switch (purpose) {
            case COMPANY_REGISTRATION -> "partners/company-registration";
            case ORGANIZER_ID -> "partners/organizer-ids";
            case AGREEMENT_SIGNATURE -> "partners/agreements";
            case EVENT_FLYER -> "events/flyers";
        };
    }

    /**
     * Extracts the full file path from S3 URL
     */
    private String extractFilePathFromUrl(String url) {
        // Extract everything after the bucket URL
        String bucketUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, "ap-south-1");
        if (url.startsWith(bucketUrl)) {
            return url.substring(bucketUrl.length());
        }
        // Fallback to old method
        return url.substring(url.lastIndexOf("/") + 1);
    }

    // Deprecated - keeping for backward compatibility
    @Deprecated
    private String generateFileName(String originalFileName, String fileId) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return fileId + extension;
    }
}
