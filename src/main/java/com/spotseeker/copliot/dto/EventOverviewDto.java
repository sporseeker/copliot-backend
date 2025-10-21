package com.spotseeker.copliot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOverviewDto {
    private Integer totalAttendees;
    private Integer totalTicketsSold;
    private BigDecimal totalRevenue;
    private List<AlertDto> alerts;
    private AttendanceBreakdown attendanceBreakdown;
    private SalesSummary salesSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertDto {
        private String type;
        private String title;
        private String message;
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceBreakdown {
        private Integer onlineTickets;
        private Integer spotseekerInvites;
        private Integer specialInvites;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesSummary {
        private BigDecimal totalAmount;
        private Integer ticketsSold;
        private Double completionPercentage;
    }
}

