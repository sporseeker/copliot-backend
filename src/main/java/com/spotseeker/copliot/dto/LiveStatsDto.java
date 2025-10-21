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
public class LiveStatsDto {
    private Integer totalAttendees;
    private Integer insideCount;
    private Integer toComeCount;
    private List<PackageAttendance> attendanceByPackage;
    private ScanInsights scanInsights;
    private List<FraudulentAlert> fraudulentAlerts;
    private AudienceDemographics audienceDemographics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PackageAttendance {
        private String packageId;
        private String packageName;
        private Integer totalTickets;
        private Integer insideCount;
        private Integer toComeCount;
        private Double completionPercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScanInsights {
        private List<TimelineData> timelineData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimelineData {
        private String time;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FraudulentAlert {
        private String type;
        private String title;
        private String description;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AudienceDemographics {
        private GenderBreakdown genderBreakdown;
        private AgeDistribution ageDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenderBreakdown {
        private Integer male;
        private Integer female;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgeDistribution {
        private Integer age18_25;
        private Integer age26_35;
        private Integer age36_45;
        private Integer age46_55;
        private Integer age56Plus;
    }
}
