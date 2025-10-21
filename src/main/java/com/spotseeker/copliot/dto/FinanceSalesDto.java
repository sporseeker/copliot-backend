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
public class FinanceSalesDto {
    private BigDecimal totalRevenue;
    private List<SalesByPackage> salesByPackage;
    private List<PackageDetail> packageDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesByPackage {
        private String packageName;
        private BigDecimal revenue;
        private Double percentage;
        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PackageDetail {
        private String packageId;
        private String packageName;
        private BigDecimal totalRevenue;
        private Integer ticketsSold;
        private Integer totalTickets;
        private BigDecimal ticketPrice;
        private String startDate;
        private String endDate;
        private String countdown;
    }
}

