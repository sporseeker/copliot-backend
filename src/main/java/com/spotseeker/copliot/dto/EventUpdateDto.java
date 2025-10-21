package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateDto {

    private String name;
    private String date;
    private String startTime;
    private String endTime;
    private String venue;
    private String venueType;
    private String eventType;
    private String eventCategory;
    private String description;
    private String trailerVideoUrl;
    private String googleMapUrl;
    private String instagramUrl;
    private String facebookUrl;
    private List<MultipartFile> flyerImages;
    private String ticketCounterStartTime;
    private Boolean kokoPaymentEnabled;
    private String status;
}

