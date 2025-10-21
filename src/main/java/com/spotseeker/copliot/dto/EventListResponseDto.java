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
public class EventListResponseDto {
    private List<EventSummaryDto> events;
    private Long total;
    private Integer page;
    private Integer limit;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventSummaryDto {
        private String id;
        private String name;
        private String date;
        private String startTime;
        private String endTime;
        private String venue;
        private String imageUrl;
        private String status;
        private String statusType; // success, warning, error
        private String category; // active, pending, inactive
        private Integer totalAttendees;
        private Double totalRevenue;
    }
}

