package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponseDto {
    private String fileId;
    private String url;
    private String thumbnailUrl;
    private Long fileSize;
    private String uploadedAt;
}

