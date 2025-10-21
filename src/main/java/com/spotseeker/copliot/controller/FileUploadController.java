package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.FileUploadResponseDto;
import com.spotseeker.copliot.model.FileUpload;
import com.spotseeker.copliot.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3FileService s3FileService;

    @PostMapping
    public ResponseEntity<FileUploadResponseDto> uploadFile(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("purpose") String purpose) {

        Long userId = Long.parseLong(authentication.getName());

        FileUpload.FileType fileType = FileUpload.FileType.valueOf(type.toUpperCase());
        FileUpload.FilePurpose filePurpose = FileUpload.FilePurpose.valueOf(purpose.toUpperCase());

        FileUploadResponseDto response = s3FileService.uploadFile(file, fileType, filePurpose, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileId) {
        s3FileService.deleteFile(fileId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "File deleted successfully");
        return ResponseEntity.ok(response);
    }
}

