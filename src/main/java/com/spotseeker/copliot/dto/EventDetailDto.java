package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetailDto {
    private String id;
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
    private List<String> flyerImages;
    private String ticketCounterStartTime;
    private Boolean kokoPaymentEnabled;
    private String status;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
}

