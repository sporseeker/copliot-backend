package com.spotseeker.copliot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateDto {

    @NotBlank(message = "Event name is required")
    private String name;

    @NotBlank(message = "Event date is required")
    private String date; // YYYY-MM-DD

    @NotBlank(message = "Start time is required")
    private String startTime; // HH:mm

    @NotBlank(message = "End time is required")
    private String endTime; // HH:mm

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotBlank(message = "Venue type is required")
    private String venueType; // outdoor, indoor, virtual

    @NotBlank(message = "Event type is required")
    private String eventType; // concert, conference, workshop, festival, meetup

    @NotBlank(message = "Event category is required")
    private String eventCategory; // music, technology, art, sports, food, business

    private String description;

    private String trailerVideoUrl;

    private String googleMapUrl;

    private String instagramUrl;

    private String facebookUrl;

    private List<MultipartFile> flyerImages;

    private String ticketCounterStartTime; // HH:mm

    private Boolean kokoPaymentEnabled = false;

    private String status = "draft"; // draft, pending
}

